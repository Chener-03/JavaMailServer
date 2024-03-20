package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class NoopPop3Handler:MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        return Pop3Response(true,"+OK",false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "NOOP"
    }
}