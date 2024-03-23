package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class TopPop3Handler:MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        if (session.username.isNullOrBlank()){
            return Pop3Response(false,"please login first",false)
        }

        val split = command?.param?.split(" ")
        if (split?.size != 2){
            return Pop3Response(false,"invalid command",false)
        }

        val index = split[0].toIntOrNull()
        val lines = split[1].toIntOrNull()
        if (index == null || lines == null){
            return Pop3Response(false,"invalid command",false)
        }

        val email = session.properties.mailRepository?.getEmailTopByIndex(session.username!!,index,lines)

        return Pop3Response(true,"${email?.first} octets\r\n${email?.second}\r\n.",false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "TOP"
    }
}