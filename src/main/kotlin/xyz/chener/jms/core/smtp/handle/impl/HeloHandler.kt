package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus
import java.util.*

class HeloHandler: MessageHandler {
    companion object {
        const val HELLO_KEY = "xyz.chener.jms.core.smtp.handle.impl.HeloHandler.HELLO"
    }


    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        session.sessionCache[HELLO_KEY] = true

        if (command?.command == "EHLO"){
            val pre = StringBuilder()
            pre.append("250-mail\r\n")
            pre.append("250-AUTH LOGIN\r\n")
            pre.append("250-AUTH=LOGIN\r\n")
            if (SslHandler.support()) pre.append("250-STARTTLS\r\n")
            pre.append("250-coremail ${UUID.randomUUID()}")
            return SmtpResponse(SmtpResponseStatus.Ok,false,"8BITMIME", perMessage = pre.toString())
        }

        return SmtpResponse(SmtpResponseStatus.Ok,false,"OK")
    }

    override fun canProcess(command:CommandData?): Boolean {
        return command?.command != null && match(command.command)
    }

    private fun match(str:String):Boolean{
        arrayListOf("HELO","EHLO").forEach{
            if (it == str) {
                return@match true
            }
        }
        return false
    }

}