package xyz.chener.jms.core.smtp.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.common.StringBioClient
import xyz.chener.jms.core.base.CommandHandleManager
import xyz.chener.jms.core.smtp.entity.UserEmail
import xyz.chener.jms.core.smtp.service.MailSendResultListener
import xyz.chener.jms.core.smtp.service.SmtpMailSender
import java.net.InetAddress

class DefaultAsyncNettySmtpMailSenderImpl : SmtpMailSender {
    private val log: Logger = LoggerFactory.getLogger(DefaultAsyncNettySmtpMailSenderImpl::class.java)

    override fun sendMail(
        from: String,
        toList: List<UserEmail>,
        content: String,
        resultCallBack: MailSendResultListener?
    ) {

        // domain -> List<UserEmail>
        val toGroup = toList.groupBy {
            CommonUtils.parseEmailAddr(it.to)?.domain
        }.filter { it.key != null }.toMap() as Map<String, List<UserEmail>>

        val excludeRcptEmailUUID:MutableList<String> = mutableListOf()

        toGroup.forEach { (domain, userEmailToList) ->
            Thread.ofVirtual().start {
                val mxs = CommonUtils.getMx(domain)
                if (mxs.isEmpty()){
                    log.info("[未找到 $domain 的 MX 记录")
                    userEmailToList.forEach { ue ->
                        resultCallBack?.invoke(ue.uuid,false,"未找到 $domain 的 MX 记录")
                    }
                    return@start
                }

                val mx = mxs[0]

                runCatching {
                    val c = StringBioClient(mx.domain, 25)
                    var line = c.readLine()
                    println(line)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(line)?.first == "220", "无法连接到 ${mx.domain}:25")
                    println("EHLO ${InetAddress.getLocalHost().hostName}")
                    c.writeLine("EHLO ${InetAddress.getLocalHost().hostName}")
                    line = c.readAllLines(1000)
                    println(line)
                    if (line.contains("STARTTLS")){
                        c.writeLine("STARTTLS")
                        println("STARTTLS")
                        line = c.readLine()
                        println(line)
                        CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(line)?.first == "220", "STARTTLS 失败: $line")
                        // 开启ssl
                        c.ssl(true)
                    }
                    c.writeLine("MAIL FROM:<$from>")
                    println("MAIL FROM:<$from>")
                    line = c.readLine()
                    println(line)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(line)?.first == "250", "MAIL FROM 失败: $line")


                    userEmailToList.forEach {
                        c.writeLine("RCPT TO:<${it.to}>")
                        println("RCPT TO:<${it.to}>")
                        line = c.readLine()
                        println(line)
                        if (CommandHandleManager.spiltByFirstSpace(line)?.first != "250") {
                            log.error("RCPT TO 失败: $line")
                            excludeRcptEmailUUID.add(it.uuid)
                            resultCallBack?.invoke(it.uuid,false,"RCPT TO 失败: $line")
                        }
                    }


                    c.writeLine("DATA")
                    println("DATA")
                    line = c.readLine()
                    println(line)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(line)?.first == "354", "DATA 失败: $line")

                    c.writeLine(content)
                    c.writeLine("\r\n.")
                    line = c.readLine()
                    println(line)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(line)?.first == "250", "DATA End 失败: $line")

                    c.writeLine("QUIT")
                    c.close()

                    userEmailToList.filter { !excludeRcptEmailUUID.contains(it.uuid) }.forEach {
                        resultCallBack?.invoke(it.uuid,true,null)
                    }

                }.onFailure { ex->
                    log.error("发送邮件失败:",ex)
                    userEmailToList.filter { !excludeRcptEmailUUID.contains(it.uuid) }.forEach {
                        resultCallBack?.invoke(it.uuid,false,ex.message)
                    }
                }
            }
        }


    }
}