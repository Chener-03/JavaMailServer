package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class CapaPop3Handler:MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response? {
        val sb = StringBuilder()
        sb.append("Capability list follows\r\n")
        sb.append("TOP\r\n")
        sb.append("USER\r\n")
        sb.append("UTF8\r\n")
        sb.append("ID\r\n")
        sb.append("UIDL\r\n")
        sb.append(".")
        return Pop3Response(true,sb.toString(),false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "CAPA"
    }
}