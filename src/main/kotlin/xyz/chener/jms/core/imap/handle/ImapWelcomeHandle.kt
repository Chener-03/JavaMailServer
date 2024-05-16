package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.ImapClient
import xyz.chener.jms.core.imap.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData

class ImapWelcomeHandle:MessageHandler {
    override fun canProcess(command: CommandData?): Boolean {
        return super.canProcess(command)
    }

    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse? {
        return super.handleImap(session, command)
    }
}