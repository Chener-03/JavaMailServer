package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus

class RcptHandler: MessageHandler {

    companion object {
        const val RCPT_KEY_DATA = "xyz.chener.jms.core.smtp.handle.impl.RcptHandler.DATA"
    }


    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        if (session.sessionCache[HeloHandler.HELLO_KEY] == null){
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false,"Error: send HELO/EHLO first")
        }

        if (session.sessionCache[MailHandler.MAIL_KEY_ADDR] == null || session.sessionCache[MailHandler.MAIL_KEY_ADDR].toString().isBlank()) {
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false,"Error: send MAIL first")
        }

        if (command?.param == null) {
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax")
        }


        var emailAddressString : String? = null
        runCatching {
            if (command.param.startsWith("TO:")){
                emailAddressString = command.param.substring(command.param.indexOf("<")+1,command.param.indexOf(">"))
            }
        }
        if (emailAddressString.isNullOrBlank()){
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax")
        }


        val parseEmailAddr = CommonUtils.parseEmailAddr(emailAddressString!!) ?: return SmtpResponse(
            SmtpResponseStatus.CommandUnrecognized,
            false,
            " Error: bad syntax"
        )


        // 未登录只能给自己域发消息
        if (session.username == null && parseEmailAddr.domain != session.properties.domain){
            return SmtpResponse(SmtpResponseStatus.MailboxUnavailable,false,"No login must to this domain")
        }

        // 未登录 检测自己域的用户是否存在
        if (session.username == null && !session.properties.authService.userCheck(parseEmailAddr.username)) {
            return SmtpResponse(SmtpResponseStatus.MailboxUnavailable,false,"User not found: ${parseEmailAddr.toEmailAddress()}")
        }


        if (session.sessionCache[RCPT_KEY_DATA] == null || session.sessionCache[RCPT_KEY_DATA] !is ArrayList<*>){
            session.sessionCache[RCPT_KEY_DATA] = ArrayList<String>()
        }

        val rcptList  = session.sessionCache[RCPT_KEY_DATA] as ArrayList<String>
        rcptList.add(emailAddressString!!)

        return SmtpResponse(SmtpResponseStatus.Ok,false,"OK Rcpt")
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "RCPT"
    }
}