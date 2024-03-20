package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.SmtpDispatchHandle
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus

class WelcomeHandle: MessageHandler {


    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        return SmtpResponse(
            SmtpResponseStatus.ServiceReady,false,"${session.properties.domain} SMTP SERVER ${session.properties.version}"
        )
    }

    override fun canProcess(command:CommandData?): Boolean {
        return command?.command != null && command.command.trim() == SmtpDispatchHandle.CONNECT_INIT_COMMAND
    }


}