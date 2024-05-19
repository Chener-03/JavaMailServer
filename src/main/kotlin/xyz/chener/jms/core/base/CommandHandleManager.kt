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

        fun getImapCmd(str: String): CommandData?{
            runCatching {
                val firstBlank = str.indexOf(" ")
                val secondBlank = str.indexOf(" ",firstBlank+1)
                val uid = str.substring(0,firstBlank)

                val command = if (secondBlank == -1){
                    str.substring(firstBlank+1)
                }else{
                    str.substring(firstBlank+1,secondBlank)
                }

                var param:String? = null
                if (secondBlank > -1)
                    param = str.substring(secondBlank+1)
                if (uid.isEmpty() || command.isEmpty())
                    return null
                return CommandData(command,param,str,uid)
            }
            return null
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