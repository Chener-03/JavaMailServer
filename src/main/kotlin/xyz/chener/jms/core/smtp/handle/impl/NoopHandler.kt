package xyz.chener.jms.core.smtp.handle.impl


import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus

class NoopHandler: MessageHandler {
    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        return SmtpResponse(SmtpResponseStatus.Ok,false,"OK")
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "NOOP"
    }
}