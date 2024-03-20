package xyz.chener.jms.core.pop3.handle.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class QuitPop3Handler : MessageHandler{

    private val log: Logger = LoggerFactory.getLogger(QuitPop3Handler::class.java)

    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {

        if(session.username != null && session.sessionCache[DelePop3Handler.DELETE_DATA_KEY] is ArrayList<*>){
            val deleteIndex = session.sessionCache.remove(DelePop3Handler.DELETE_DATA_KEY) as ArrayList<Int>
            log.info("delete index: $deleteIndex")
            session.properties.mailRepository?.deleteEmail(session.username!!, deleteIndex)
        }

        return Pop3Response(true, "bye", true)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "QUIT"
    }
}