package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData

class ImapCapaHandle:MessageHandler {


    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse {

        val list = arrayListOf("CAPABILITY", "IMAP4rev1")
        if (ImapSslHandle.support()) {
            list.add("STARTTLS")
        }

        val res = "* ${list.joinToString(" ")}\r\n" +
                "${command?.uid} OK CAPABILITY completed"

        return ImapResponse(
            isRawData = true,
            rawContent = res,
            kickClient = false,
            uid = command?.uid)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command == "CAPABILITY"
    }
}