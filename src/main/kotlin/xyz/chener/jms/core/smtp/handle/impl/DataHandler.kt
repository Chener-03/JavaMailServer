package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.*

class DataHandler: MessageHandler {

    companion object{
        const val DATA_KEY = "xyz.chener.jms.core.smtp.handle.impl.DataHandler.DATA"
    }

    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse? {
        if (session.sessionCache[HeloHandler.HELLO_KEY] == null){
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false,"Error: send HELO/EHLO first")
        }

        if (session.sessionCache[MailHandler.MAIL_KEY_ADDR] == null || session.sessionCache[MailHandler.MAIL_KEY_ADDR].toString().isBlank()) {
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false,"Error: send MAIL first")
        }

        if (session.sessionCache[RcptHandler.RCPT_KEY_DATA] == null  || session.sessionCache[RcptHandler.RCPT_KEY_DATA] !is ArrayList<*>
            || (session.sessionCache[RcptHandler.RCPT_KEY_DATA] as ArrayList<*>).isEmpty()) {
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false,"Error: send Rcpt first")
        }

        if (session.keepHandle == null){
            session.keepHandle = this
            session.sessionCache[DATA_KEY] = StringBuffer()
            return SmtpResponse(SmtpResponseStatus.StartMailInput,false,"End data with <CR><LF>.<CR><LF>")
        }

        if (command?.source == "."){
            session.keepHandle = null
            val data = session.sessionCache[DATA_KEY] as StringBuffer
            session.sessionCache.remove(DATA_KEY)

            return session.properties.mailService.sendMail(
                session.sessionCache[MailHandler.MAIL_KEY_ADDR].toString(),
                session.sessionCache[RcptHandler.RCPT_KEY_DATA] as List<String>,
                data.toString(),
                SmtpServiceContext(session.ipAddress,session.username,session.properties.domain)
            )
        }

        val data = session.sessionCache[DATA_KEY] as StringBuffer
        data.append(if (command?.source == null) "" else command.source).append("\r\n")
        return null
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "DATA"
    }
}