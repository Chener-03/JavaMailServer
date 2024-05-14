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
                    val c = StringBioClient(domain, 25)
                    var line = c.readLine()
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(line)?.first == "220", "无法连接到 ${mx.domain}:25")
                    c.writeLine("EHLO ${InetAddress.getLocalHost().hostName}")
                    line = c.readAllLines(1000)
                    if (line.contains("STARTTLS")){
                        c.writeLine("STARTTLS")
                        line = c.readLine()
                        CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(line)?.first == "220", "STARTTLS 失败: $line")
                        // 开启ssl
                        c.ssl()
                    }

                }.onFailure { ex->

                }

            }
        }


    }
}