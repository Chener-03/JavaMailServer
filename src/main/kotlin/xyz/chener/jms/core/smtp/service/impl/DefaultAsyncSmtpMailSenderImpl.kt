package xyz.chener.jms.core.smtp.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.CommandHandleManager
import xyz.chener.jms.core.smtp.entity.UserEmail
import xyz.chener.jms.core.smtp.service.MailSendResultListener
import xyz.chener.jms.core.smtp.service.SmtpMailSender
import java.net.InetAddress
import java.net.Socket

@Deprecated(message = "Deprecated", replaceWith = ReplaceWith("DefaultAsyncNettySmtpMailSenderImpl"))
class DefaultAsyncSmtpMailSenderImpl: SmtpMailSender {

    private val log: Logger = LoggerFactory.getLogger(DefaultAsyncSmtpMailSenderImpl::class.java)


    override fun sendMail(from: String, toList: List<UserEmail>, content: String, resultCallBack: MailSendResultListener?) {

        val toGroup = toList.groupBy {
            CommonUtils.parseEmailAddr(it.to)?.domain
        }.filter { it.key != null }.toMap() as Map<String, List<UserEmail>>

        val excludeRcptEmailUUID:MutableList<String> = mutableListOf()

        toGroup.forEach { (domain, userEmailToList) ->

            Thread.ofVirtual().name("sendMail-$domain").start {
                val mxs = CommonUtils.getMx(domain)
                if (mxs.isEmpty()){
                    log.info("[未找到 $domain 的 MX 记录")
                    userEmailToList.forEach { ue ->
                        resultCallBack?.invoke(ue.uuid,false,"未找到 $domain 的 MX 记录")
                    }
                    return@start
                }

                val mx = mxs[0]
                try {

                    val socket = Socket(mx.domain, 25)
                    socket.soTimeout = 1000 * 10

                    val writer = socket.getOutputStream().bufferedWriter()
                    val reader = socket.getInputStream().bufferedReader()
                    var response = reader.readLine()
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(response)?.first == "220", "无法连接到 ${mx.domain}:25")

                    writer.write("EHLO ${InetAddress.getLocalHost().hostName}\r\n")
                    writer.flush()
                    response = reader.readLine()
                    while (response.startsWith("250-")){
                        response = reader.readLine()
                    }
                    println(response)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(response)?.first == "250", "HELO 失败: $response")

                    writer.write("MAIL FROM:<$from>\r\n")
                    writer.flush()
                    response = reader.readLine()
                    println(response)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(response)?.first == "250", "MAIL FROM 失败: $response")


                    userEmailToList.forEach {
                        writer.write("RCPT TO:<${it.to}>\r\n")
                        writer.flush()
                        response = reader.readLine()
                        println(response)
                        if (CommandHandleManager.spiltByFirstSpace(response)?.first != "250") {
                            log.error("RCPT TO 失败: $response")
                            excludeRcptEmailUUID.add(it.uuid)
                            resultCallBack?.invoke(it.uuid,false,"RCPT TO 失败: $response")
                        }
                    }

                    writer.write("DATA\r\n")
                    writer.flush()
                    response = reader.readLine()
                    println(response)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(response)?.first == "354", "DATA Start 失败: $response")

                    writer.write(content)
                    writer.flush()
                    writer.write("\r\n.\r\n")
                    writer.flush()
                    response = reader.readLine()
                    println(response)
                    CommonUtils.AssertState(CommandHandleManager.spiltByFirstSpace(response)?.first == "250", "DATA End 失败: $response")

                    writer.write("QUIT\r\n")
                    writer.flush()
                    socket.close()

                    userEmailToList.filter { !excludeRcptEmailUUID.contains(it.uuid) }.forEach {
                        resultCallBack?.invoke(it.uuid,true,null)
                    }

                }catch (ex:Exception){
                    log.error("发送邮件失败:",ex)
                    userEmailToList.filter { !excludeRcptEmailUUID.contains(it.uuid) }.forEach {
                        resultCallBack?.invoke(it.uuid,false,ex.message)
                    }
                }
            }
        }
    }
}