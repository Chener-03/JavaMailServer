package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class RetrPop3Handler:MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response? {
        if (session.username.isNullOrBlank()){
            return Pop3Response(false,"please login first",false)
        }

        if (command?.param == null || command.param.toIntOrNull() == null){
            return Pop3Response(false,"invalid command",false)
        }

        session.properties.mailRepository?.let {
            val email = it.getEmailById(session.username!!, command.param.toInt())
            if (email?.content != null){
                return Pop3Response(true, "${email.content} \r\n.",false)
            }
        }

        return Pop3Response(true, "EMAIL NOT FOUND! \r\n.", false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "RETR"
    }
}