package xyz.chener.jms.core.smtp.handle.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus
import java.util.*

class AuthLoginHandle: MessageHandler {

    private val log: Logger = LoggerFactory.getLogger(AuthLoginHandle::class.java)


    data class AccountCache(var username:String?,var password:String?)

    // 存状态
    val STATUS_KEY = "xyz.chener.jms.core.smtp.handle.impl.AuthLoginHandle.STATUS"
    val STATUS_USERNAME = "USERNAME"    // 等待输入用户名
    val STATUS_PASSWORD = "PASSWORD"    // 等待输入密码

    // 存数据
    val DATA_KEY = "xyz.chener.jms.core.smtp.handle.impl.AuthLoginHandle.DATA"

    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {

        // 必须先发送HELO
        if (session.sessionCache[HeloHandler.HELLO_KEY] == null){
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false," Error: send HELO/EHLO first")
        }


        // 进入登录状态 handle设置为当前  存入状态key
        if (session.sessionCache[STATUS_KEY] == null) {
            session.sessionCache[STATUS_KEY] = STATUS_USERNAME
            session.keepHandle = this
            return SmtpResponse(SmtpResponseStatus.WaitUserInputMessage,false,Base64.getEncoder().encodeToString("username:".encodeToByteArray()))
        }

        // 状态key变为密码
        if (session.sessionCache[STATUS_KEY] == STATUS_USERNAME) {
            session.sessionCache[DATA_KEY] = AccountCache(command?.source,null)
            session.sessionCache[STATUS_KEY] = STATUS_PASSWORD
            return SmtpResponse(SmtpResponseStatus.WaitUserInputMessage,false,Base64.getEncoder().encodeToString("Password:".encodeToByteArray()))
        }

        // 状态key 和 handle 变为null  结束登录流程
        if (session.sessionCache[STATUS_KEY] == STATUS_PASSWORD){
            session.keepHandle = null
            session.sessionCache.remove(STATUS_KEY)

            val ac = session.sessionCache.remove(DATA_KEY)
            if (ac is AccountCache){
                ac.password = command?.source

                var username:String? = null
                var password:String? = null

                try {
                    username = String(Base64.getDecoder().decode(ac.username))
                    password = String(Base64.getDecoder().decode(ac.password))
                }catch (_:Exception){}


                if (username == null || password == null){
                    return SmtpResponse(SmtpResponseStatus.UserLoginFail,false,"Error: authentication failed  0x1")
                }

                // 解析用户名
                val parseEmailAddr = CommonUtils.parseEmailAddr(username) ?: return SmtpResponse(SmtpResponseStatus.UserLoginFail,false,"Error: authentication failed  0x2")

                // 检查用户名和域名是否和当前域匹配
                if (!CommonUtils.checkDomain(parseEmailAddr.domain) || parseEmailAddr.username.isBlank() || parseEmailAddr.domain != session.properties.domain){
                    return SmtpResponse(SmtpResponseStatus.UserLoginFail,false,"Error: authentication failed  0x3")
                }

                // 登录
                if (session.properties.authService.doLogin(parseEmailAddr.username,password)){
                    session.username = parseEmailAddr.username
                    return SmtpResponse(SmtpResponseStatus.UserLoginSuccess,false,"Authentication successful")
                }
            }
        }

        return SmtpResponse(SmtpResponseStatus.UserLoginFail,false,"Error: authentication failed  0x4")
    }

    override fun canProcess(command:CommandData?): Boolean {
        return command?.command!= null && command.param != null
                && command.command == "AUTH" && command.param == "LOGIN"
    }
}