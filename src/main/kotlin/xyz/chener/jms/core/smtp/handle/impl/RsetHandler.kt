package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus

class RsetHandler : MessageHandler {
    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        session.sessionCache.remove(MailHandler.MAIL_KEY_ADDR)
        session.sessionCache.remove(MailHandler.MAIL_KEY_SIZE)
        session.sessionCache.remove(RcptHandler.RCPT_KEY_DATA)

        return SmtpResponse(SmtpResponseStatus.Ok,false,"OK Rset")
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "RSET"
    }
}