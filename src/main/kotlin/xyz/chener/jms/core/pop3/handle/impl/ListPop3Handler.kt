package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class ListPop3Handler:MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response? {
        if (session.username.isNullOrBlank()){
            return Pop3Response(false,"please login first",false)
        }

        /**
         * LIST
         * +OK 10 238773
         * 1 2478
         * 2 40697
         * 3 92914
         * 4 2492
         * 5 62177
         * 6 763
         * 7 771
         * 8 28463
         * 9 4008
         * 10 4010
         * .
         */


        var count = 0
        var size = 0
        var list = mutableListOf<Pair<Int,Int>>()

        session.properties.mailRepository?.let {
            it.getEmailCountAndSize(session.username!!,command?.param?.trim()?.toIntOrNull()).let { (c,s)->
                count = c
                size = s
            }
            it.getEmailIdAndSizeList(session.username!!,command?.param?.trim()?.toIntOrNull()).let { lst ->
                list = lst.toMutableList()
            }
        }


        val sb = StringBuilder()
        sb.append("$count $size\r\n")
        list.forEach {
            sb.append("${it.first} ${it.second}\r\n")
        }
        sb.append(".")


        return Pop3Response(true, sb.toString(), false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "LIST"
    }
}