package xyz.chener.jms.core.base

import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.handle.impl.UnknownHandle


class CommandHandleManager (val messageHandlerChain: List<MessageHandler>) {

    companion object{
        var unknownHandle: MessageHandler = UnknownHandle()

        fun spiltByFirstSpace(str: String): Pair<String?,String?>?{
            try {
                val index = str.indexOf(" ")
                if (index > -1){
                    return Pair(str.substring(0,index),str.substring(index+1))
                }
                return Pair(str,null)
            }catch (_:Exception){
                return null
            }
        }
    }


    fun getHandle(command: CommandData): MessageHandler {
        messageHandlerChain.forEach {
            if (it.canProcess(command))
                return@getHandle it;
        }
        return unknownHandle
    }


}