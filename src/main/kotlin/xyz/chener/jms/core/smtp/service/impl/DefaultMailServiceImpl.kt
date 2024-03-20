package xyz.chener.jms.core.smtp.service.impl

import org.apache.james.jspf.core.exceptions.SPFErrorConstants
import org.apache.james.jspf.executor.SPFResult
import org.apache.james.jspf.impl.DefaultSPF
import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.smtp.entity.*
import xyz.chener.jms.core.smtp.service.MailService
import xyz.chener.jms.core.smtp.service.SmtpMailSender
import xyz.chener.jms.repositorys.MailRepository

class DefaultMailServiceImpl(val mailSender: SmtpMailSender,var mailRepository: MailRepository?): MailService {


    private val defaultSPF = DefaultSPF(CommonUtils.DNS_SERVER)

    override fun sendMail(from: String, to: List<String>, content: String, context: SmtpServiceContext): SmtpResponse {
        val fromEmailAddr = CommonUtils.parseEmailAddr(from)

        if (from.isBlank() || to.isEmpty() || content.isBlank() || fromEmailAddr == null || context.clientIpAddr == null){
            return SmtpResponse(SmtpResponseStatus.TransactionFailed,false,"Syntax error")
        }

        // from 不是本域用户 校验SPF
        if (fromEmailAddr.domain != context.domain){
            val checkSPF: SPFResult = defaultSPF.checkSPF(context.clientIpAddr, from, fromEmailAddr.domain)
            if (checkSPF.result == SPFErrorConstants.FAIL_CONV || checkSPF.result == SPFErrorConstants.PERM_ERROR_CONV || checkSPF.result == SPFErrorConstants.SOFTFAIL_CONV){
                return SmtpResponse(SmtpResponseStatus.TransactionFailed,false,"SPF check failed")
            }
        }

        // from是本域用户 必须登录 二次校验
        if (fromEmailAddr.domain == context.domain && context.username != fromEmailAddr.username){
            return SmtpResponse(SmtpResponseStatus.TransactionFailed,false,"User not equals")
        }



        // 处理 From 是本域的邮件 肯定是发送 因为不允许代理
        if (fromEmailAddr.domain == context.domain){

            val toGroups = to.groupBy {
                CommonUtils.parseEmailAddr(it)?.domain
            }.filter { it.key != null}.toMap() as Map<String, List<String>>


            // 过滤出来需要发送给外部的
            toGroups.filter { it.key != context.domain }.toMap().let {
                if (it.isNotEmpty()){
                    // 保存发件列表
                    it.forEach{ (domain, tolist) ->
                        val userSendEmailList = tolist.map { toaddr ->
                            return@map UserEmail(
                                id = null,
                                uuid = CommonUtils.uuid(),
                                from = from,
                                to = toaddr,
                                content = content,
                                username = context.username,
                                createTime = java.util.Date(),
                                type = EmailType.SEND.code,
                                state = EmailState.SEND_SUCCESS.code,
                                isDel = false
                            )
                        }.toList()
                        // save to db
                        mailRepository?.save(userSendEmailList)
                        mailSender.sendMail(from,userSendEmailList,content){mailUUID,success,errorMsg->
                            mailRepository?.updateMailSendState(mailUUID,success,errorMsg)
                        }
                    }
                }
            }

            // 本域互发的
            toGroups.filter { it.key == context.domain }.toMap().let {
                if (it.isNotEmpty()){
                    println("发送本域内部邮件:")
                    println("登录用户: ${context.username}")
                    println("From: $from")
                    println("To: $it")
                    println("Content: $content")

                    it.forEach { (domain, tolist) ->
                        // 这里邮件会有两种状态 一个是发件人是发送的记录 一个是收件人是收件记录
                        // 先处理发件人的  肯定是成功 先不考虑用户个人过滤问题  rcpt时会校验是否存在
                        val userSendEmailList = tolist.map { toaddr ->
                            return@map UserEmail(
                                id = null,
                                uuid = CommonUtils.uuid(),
                                from = from,
                                to = toaddr,
                                content = content,
                                username = context.username,    //发件人归属是登录用户
                                createTime = java.util.Date(),
                                type = EmailType.SEND.code,
                                state = EmailState.SEND_SUCCESS.code,
                                isDel = false
                            )
                        }.toList()
                        // save to db
                        mailRepository?.save(userSendEmailList)

                        // 处理接收的
                        val userRecvEmailList = tolist.map { toaddr ->
                            return@map UserEmail(
                                id = null,
                                uuid = CommonUtils.uuid(),
                                from = from,
                                to = toaddr,
                                content = content,
                                username = CommonUtils.parseEmailAddr(toaddr)?.username,
                                createTime = java.util.Date(),
                                type = EmailType.RECEIVE.code,
                                state = EmailState.UNREAD.code,
                                isDel = false
                            )
                        }.toList()
                        // save to db
                        mailRepository?.save(userRecvEmailList)

                    }

                }
            }
        }



        // 处理 From 其它域的邮件  这种邮件rcpt肯定是本域 且为接受外部域邮件
        if (fromEmailAddr.domain != context.domain){
            println("接收外部邮件:")
            println("From: $from")
            println("To: $to")
            println("Content: $content")

            // 邮件归属为 接收用户 且为未读
            val userEmailList = to.map {
                return@map UserEmail(
                    id = null,
                    uuid = CommonUtils.uuid(),
                    from = from,
                    to = it,
                    content = content,
                    username = CommonUtils.parseEmailAddr(it)?.username,
                    createTime = java.util.Date(),
                    type = EmailType.RECEIVE.code,
                    state = EmailState.UNREAD.code,
                    isDel = false
                )
            }.toList()

            // save to db
            mailRepository?.save(userEmailList)
        }

        return SmtpResponse(SmtpResponseStatus.Ok,true,"Mail send success")
    }
}