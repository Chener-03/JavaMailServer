package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData

class ImapNoopHandle:MessageHandler {
    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "NOOP"
    }

    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse? {
        return ImapResponse(
            success = true,
            message = "completed",
            kickClient = false,
            uid = command?.uid)
    }
}