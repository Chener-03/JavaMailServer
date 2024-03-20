package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus

class VrfySmtpHandler : MessageHandler {
    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        if (command?.param == null) {
            return SmtpResponse(SmtpResponseStatus.SyntaxError, false,"vrfy param error")
        }

        val parseEmailAddr = CommonUtils.parseEmailAddr(command.param)
        parseEmailAddr?:return SmtpResponse(SmtpResponseStatus.SyntaxError, false,"vrfy param error")

        val b = session.properties.authService.userCheck(parseEmailAddr.username) && parseEmailAddr.domain == session.properties.domain

        return if (b){
            SmtpResponse(SmtpResponseStatus.Ok, false,"Ok")
        }else{
            SmtpResponse(SmtpResponseStatus.MailboxUnavailable, false,"No such user here")
        }
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "VRFY"
    }
}