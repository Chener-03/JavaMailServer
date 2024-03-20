package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class RsetPop3Handler: MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        session.sessionCache.remove(DelePop3Handler.DELETE_DATA_KEY)
        return Pop3Response(true, "reset success", false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "RSET"
    }
}