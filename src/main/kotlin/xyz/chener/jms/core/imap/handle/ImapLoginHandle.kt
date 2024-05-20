package xyz.chener.jms.core.imap.handle

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.EmailAddress

class ImapLoginHandle:MessageHandler {
    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "LOGIN"
    }

    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse {

        if (session.username != null) {
            return ImapResponse(
                success = false,
                message = "already login",
                kickClient = false,
                uid = command?.uid)
        }

        val pm = command?.param?.split(" ")
        if (pm == null || pm.size < 2 ){
            return ImapResponse(
                success = false,
                message = "invalid param",
                kickClient = false,
                uid = command?.uid)
        }

        val ea = CommonUtils.parseEmailAddr(pm[0])
            ?: return ImapResponse(
                success = false,
                message = "invalid email address",
                kickClient = false,
                uid = command.uid)



        if (session.properties.authImapService?.login(ea.username,pm[1]) == true){
            session.username = ea.username
            return ImapResponse(
                success = true,
                message = "completed",
                kickClient = false,
                uid = command.uid
            )
        }

        return ImapResponse(
            success = false,
            message = "unauthorized",
            kickClient = false,
            uid = command.uid
        )
    }
}