package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData


class UidlPop3Handler:MessageHandler {
    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response? {

        if (session.username.isNullOrBlank()){
            return Pop3Response(false,"please login first",false)
        }

        /**
         * UIDL  参数 n
         * +OK 10 238773
         * 1 xtbBZQiNoWV4H8NafAAAsm
         * 2 1tbiRBaToWVOBnnRdwAAsS
         * 3 1tbiJxWUoWXAkrpRigAAsR
         * 4 1tbiRB2foWVOBzyndgAAsm
         * 5 1tbiJwuhoWXAk4DXNwABs7
         * 6 1tbiRAGjoWVOB38QNwABsy
         * 7 1tbiJxSjoWXAk6HLpAAAsJ
         * 8 1tbiRAijoWVOB3-zNgAAsY
         * 9 xtbB0AukoWWXwYRKKAAAsL
         * 10 1tbiJxCkoWXAk6byPQAAst
         * .
         */
        var count = 0
        var size = 0
        var list = mutableListOf<Pair<Int,String>>()

        session.properties.mailRepository?.let {
            it.getEmailCountAndSize(session.username!!,command?.param?.trim()?.toIntOrNull()).let { (c,s)->
                count = c
                size = s
            }
            it.getEmailUidList(session.username!!,command?.param?.trim()?.toIntOrNull()).let { lst ->
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
        return command?.command != null && command.command.trim() == "UIDL"
    }
}