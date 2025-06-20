package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData

class ImapSelectHandle:MessageHandler {


    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse {

        /**
         * a001 SELECT INBOX
         * * 61 EXISTS
         * * 0 RECENT
         * * OK [UIDVALIDITY 1] UIDs valid
         * * FLAGS (\Answered \Seen \Deleted \Draft \Flagged)
         * * OK [PERMANENTFLAGS (\Answered \Seen \Deleted \Draft \Flagged)] Limited
         * a001 OK [READ-WRITE] SELECT completed
         */
        if (command?.param.isNullOrEmpty()){
            return ImapResponse(
                success = false,
                message = "Bad command sequence (BAD)",
                uid = command?.uid,
                kickClient = false,
            )
        }

        if(session.username == null){
            return ImapResponse(
                success = false,
                message = "Not authenticated (NO)",
                uid = command?.uid,
                kickClient = false,
            )
        }

        val folder = session.properties.mailRepository!!.selectOneFolder(session.username!!, command.param.trim())

        if (folder == null){
            return ImapResponse(
                success = false,
                message = "No such folder (NO)",
                uid = command.uid,
                kickClient = false,
            )
        }

        val emails = session.properties.mailRepository!!.selectListByFolder(session.username!!, folder.fid!!)

        val exits = emails.size
        val recent = emails.filter { it.flags?.contains("\\Recent") == true }.size

        val sb = StringBuilder()

        sb.append("* $exits EXISTS\r\n")
        sb.append("* $recent RECENT\r\n")
        sb.append("* OK [UIDVALIDITY 1] UIDs valid\r\n")
        sb.append("* FLAGS (\\Answered \\Seen \\Deleted \\Draft \\Flagged)\r\n")
        sb.append("* OK [PERMANENTFLAGS (\\Answered \\Seen \\Deleted \\Draft \\Flagged)] Limited\r\n")
        sb.append("${command.uid} OK [READ-WRITE] SELECT completed\r\n")

        session.currentFolder = CommonUtils.decodeModifiedUTF7(command.param.trim())

        return ImapResponse(
            isRawData = true,
            rawContent = sb.toString(),
            kickClient = false,
            uid = command.uid)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command == "SELECT" || command?.command == "EXAMINE"
    }
}