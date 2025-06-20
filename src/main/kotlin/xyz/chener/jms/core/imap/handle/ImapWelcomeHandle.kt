package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.ImapDispatchHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData


class ImapWelcomeHandle:MessageHandler {

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command == ImapDispatchHandler.CONNECT_INIT_COMMAND
    }

    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse {
        return ImapResponse(
            success = true,
            message = "IMAP4 ready",
            kickClient = false)
    }
}