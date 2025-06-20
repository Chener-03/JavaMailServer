package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData

class ImapOpFolderHandle:MessageHandler {


    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse {
        if (command?.param.isNullOrEmpty()){
            return ImapResponse(
                success = false,
                message = "Bad command sequence (BAD)",
                uid = command?.uid,
                kickClient = false,
            )
        }

        val size = command.param.split(" ").filter { it.isNotBlank() }.size
        if (command.command == "CREATE" || command.command == "DELETE"){
            if (size != 1){
                return ImapResponse(
                    success = false,
                    message = "Bad command sequence (BAD)",
                    uid = command.uid,
                    kickClient = false,
                )
            }
        } else {
            if (size!= 2){
                return ImapResponse(
                    success = false,
                    message = "Bad command sequence (BAD)",
                    uid = command.uid,
                    kickClient = false,
                )
            }
        }

        if(session.username == null){
            return ImapResponse(
                success = false,
                message = "Not authenticated (NO)",
                uid = command.uid,
                kickClient = false,
            )
        }


        val dirs = session.properties.mailRepository!!.imapListDirectory(session.username!!)

        if (command.command == "CREATE") {
            dirs.find { it.path == CommonUtils.decodeModifiedUTF7(command.param) }?.let {
                return ImapResponse(
                    success = false,
                    message = "Folder already exists (BAD)",
                    uid = command.uid,
                    kickClient = false,
                )
            }
            val createFolder = session.properties.mailRepository!!.createFolder(
                session.username!!,
                CommonUtils.decodeModifiedUTF7(command.param)
            )
            if (createFolder == null) {
                return ImapResponse(
                    success = false,
                    message = "Failed to create folder (BAD)",
                    uid = command.uid,
                    kickClient = false,
                )
            }
            return ImapResponse(
                success = true,
                message = "OK",
                uid = command.uid,
                kickClient = false,
            )
        }


        if (command.command == "DELETE") {


        }

        return ImapResponse(
            success = false,
            message = "error command",
            uid = command.uid,
            kickClient = false,
        )
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command == "CREATE" || command?.command == "DELETE" || command?.command == "RENAME"
    }
}