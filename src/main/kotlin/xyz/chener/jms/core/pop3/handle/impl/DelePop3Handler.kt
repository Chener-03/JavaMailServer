package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class DelePop3Handler:MessageHandler {

    companion object {
        const val DELETE_DATA_KEY = "xyz.chener.jms.core.pop3.handle.impl.DelePop3Handler.DATA"
    }

    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        if (session.username.isNullOrBlank()){
            return Pop3Response(false,"please login first",false)
        }

        if (command?.param == null || command.param.toIntOrNull() == null){
            return Pop3Response(false,"invalid command",false)
        }

        if (session.sessionCache[DELETE_DATA_KEY] == null) {
            session.sessionCache[DELETE_DATA_KEY] = ArrayList<Int>()
        }

        val deleteList = session.sessionCache[DELETE_DATA_KEY] as ArrayList<Int>
        deleteList.add(command.param.toInt())

        return Pop3Response(true, "add delete list success", false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "DELE"
    }
}