package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData

class AuthPop3Handler : MessageHandler{

    companion object {
        const val USERNAME_COMMAND_KEY = "xyz.chener.jms.core.pop3.handle.impl.AuthPop3Handler.USERNAME"
    }

    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        if(command?.command?.trim() == "AUTH" && command.param == null){
            return Pop3Response(true,"methods supported:\r\nLOGIN\r\n.",false)
        }


        if (command?.command?.trim() == "USER" && command.param != null) {
            CommonUtils.parseEmailAddr(command.param) ?: return Pop3Response(false,"username is not a vaild email",false)
            session.sessionCache[USERNAME_COMMAND_KEY] = command.param
            return Pop3Response(true,"+OK",false)
        }

        if (command?.command?.trim() == "PASS"  && command.param != null) {
            // PASS 前必须先 USER
            session.sessionCache[USERNAME_COMMAND_KEY] ?: return Pop3Response(false,"invalid command",false)

            val usernameEmailStr = session.sessionCache.remove(USERNAME_COMMAND_KEY) as String
            val usernameEmail = CommonUtils.parseEmailAddr(usernameEmailStr)!!
            val password = command.param

            val doLogin = session.properties.authPop3Service?.doLogin(usernameEmail.username, password)

            if (doLogin == true){
                session.username = usernameEmail.username
                return Pop3Response(true,"+OK",false)
            }
            return Pop3Response(false,"login fail",true)
        }

        return Pop3Response(false,"invalid command",false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && (command.command.trim() == "USER" || command.command.trim() == "PASS" || command.command.trim() == "AUTH")
    }
}