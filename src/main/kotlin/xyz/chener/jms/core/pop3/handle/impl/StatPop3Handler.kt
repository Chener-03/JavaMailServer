package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class StatPop3Handler : MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        if (session.username.isNullOrBlank()){
            return Pop3Response(false,"please login first",false)
        }

        /**
         * STAT
         * +OK 10 238773
         */

        var emailCount = 0
        var emailSize = 0

        session.properties.mailRepository?.let {
            it.getEmailCountAndSize(session.username!!).let { (count,size)->
                emailCount = count
                emailSize = size
            }
        }

        return Pop3Response(true, "$emailCount $emailSize", false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "STAT"
    }
}