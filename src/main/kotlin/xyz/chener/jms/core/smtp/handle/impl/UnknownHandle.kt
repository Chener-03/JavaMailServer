package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus

class UnknownHandle : MessageHandler {
    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        return SmtpResponse(SmtpResponseStatus.CommandNotImplemented,false,"Command Not Implemented")
    }

    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        return Pop3Response(false,"invalid command",false)
    }

    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse {
        return ImapResponse(uid = command?.uid,success = false,message = "invalid command",kickClient = false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return true
    }
}