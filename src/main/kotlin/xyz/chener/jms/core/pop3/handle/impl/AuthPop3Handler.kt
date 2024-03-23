package xyz.chener.jms.core.pop3.handle.impl

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData
import java.util.Base64

class AuthPop3Handler : MessageHandler{

    companion object {
        const val USERNAME_COMMAND_KEY = "xyz.chener.jms.core.pop3.handle.impl.AuthPop3Handler.USERNAME"
        const val AUTH_LOGIN_COMMAND_KEY = "xyz.chener.jms.core.pop3.handle.impl.AuthPop3Handler.AUTH.LOGIN.USERNAME"
    }

    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response {
        // AUTH LOGIN 登录处理
        if (session.keepHandle != null){
            if (session.sessionCache[AUTH_LOGIN_COMMAND_KEY] == null){
                // username
                var un = ""
                runCatching {
                    un = String(Base64.getDecoder().decode(command?.source?.trim() ?: ""))
                }
                session.sessionCache[AUTH_LOGIN_COMMAND_KEY] = un
                return Pop3Response(true,null,false,source = "+ ${Base64.getEncoder().encodeToString("Password:".toByteArray())}")
            }else{
                // password
                session.keepHandle = null
                val unObj = session.sessionCache.remove(AUTH_LOGIN_COMMAND_KEY)
                val unStr = unObj?.toString() ?: ""
                var pw = ""
                runCatching {
                    pw = String(Base64.getDecoder().decode(command?.source?.trim() ?: ""))
                }
                val usernameEmail = CommonUtils.parseEmailAddr(unStr) ?: return Pop3Response(false,"username is not a vaild email",false)

                return doLogin(session,usernameEmail.username,pw)
            }
        }

        if(command?.command?.trim() == "AUTH" && command.param?.trim() == "LOGIN"){
            session.keepHandle = this
            return Pop3Response(true,null,false, source = "+ ${Base64.getEncoder().encodeToString("Username:".toByteArray())}")
        }


        if(command?.command?.trim() == "AUTH" && command.param == null){
            return Pop3Response(true,"methods supported:\r\nLOGIN\r\n.",false)
        }


        // USER 命令和 PASS 命令登录处理
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

            return doLogin(session,usernameEmail.username,password)
        }

        return Pop3Response(false,"invalid command",false)
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && (command.command.trim() == "USER" || command.command.trim() == "PASS" || command.command.trim() == "AUTH")
    }

    private fun doLogin(session: Pop3Clinet,username:String,password:String):Pop3Response{
        val logRes = session.properties.authPop3Service?.doLogin(username, password)

        if (logRes == true){
            session.username =  username
            return Pop3Response(true,"+OK",false)
        }
        return Pop3Response(false,"login fail",true)
    }
}