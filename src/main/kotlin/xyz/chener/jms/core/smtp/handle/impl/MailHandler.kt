package xyz.chener.jms.core.smtp.handle.impl

import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus


class MailHandler : MessageHandler {

    companion object {
        const val MAIL_KEY_ADDR = "xyz.chener.jms.core.smtp.handle.impl.MailHandler.MAIL_ADDR"
        const val MAIL_KEY_SIZE = "xyz.chener.jms.core.smtp.handle.impl.MailHandler.MAIL_SIZE"
    }


    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        if (session.sessionCache[HeloHandler.HELLO_KEY] == null){
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false,"Error: send HELO/EHLO first")
        }

        if (command?.param == null) {
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax")
        }

        // MAIL FROM:<chener0354@163.com> [SIZE=123456]  ---->  chener0354@163.com
        if (!command.param.trim().startsWith("FROM:")) {
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax")
        }

        var emailAddressString : String? = null
        var emailSize : Int? = null

        command.param.trim().split(" ").forEachIndexed{ _, s ->
            runCatching {
                if (s.startsWith("FROM:")){
                    emailAddressString = s.substring(s.indexOf("<")+1,s.indexOf(">"))
                }
                if (s.startsWith("[SIZE=")){
                    emailSize = s.substring(s.indexOf("[SIZE=")+1,s.indexOf("]")).toInt()
                }
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

        if (!CommonUtils.checkDomain(parseEmailAddr.domain) || parseEmailAddr.username.isBlank()){
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax")
        }

        // 如果登录过 代表是发送给其它 from必须是登录的用户
        if (session.username != null && (parseEmailAddr.username != session.username || parseEmailAddr.domain != session.properties.domain)){
            return SmtpResponse(SmtpResponseStatus.MailboxNameNotAllowed,false,"Mail from must equal authorized user")
        }

        session.sessionCache[MAIL_KEY_ADDR] = emailAddressString!!
        emailSize?.let {
            session.sessionCache[MAIL_KEY_SIZE] = emailSize!!
        }

        return SmtpResponse(SmtpResponseStatus.Ok,false,"Mail OK")
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "MAIL"
    }
}