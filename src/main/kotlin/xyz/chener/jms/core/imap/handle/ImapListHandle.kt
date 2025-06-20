package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData

class ImapListHandle:MessageHandler {
    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "LIST"
    }

    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse? {

        val split = command?.param?.split(" ")
        if (split?.size != 2){
            return ImapResponse (
                success = false,
                message = "Invalid command",
                kickClient = false,
                uid = command?.uid
            )
        }

        val reference = split[0]
        val mailbox = split[1]
        val path = reference +  mailbox

        if (session.username == null){
            return ImapResponse (
                success = false,
                message = "Not authenticated",
                kickClient = false,
                uid = command.uid
            )
        }

        if (session.properties.mailRepository == null){
            return ImapResponse (
                success = false,
                message = "Not support",
                kickClient = false,
                uid = command.uid
            )
        }

        val dirs = session.properties.mailRepository!!.imapListDirectory(session.username!!)

        val sb = StringBuilder()
        dirs.forEach {
            sb.append("* LIST (${it.tag?:""}) \"/\" \"${CommonUtils.encodeModifiedUTF7(it.path!!)}\"").append("\r\n")
        }
        sb.append("${command.uid} OK LIST Completed")
        return ImapResponse (
            rawContent = sb.toString(),
            isRawData = true,
            kickClient = false
        )
    }
}