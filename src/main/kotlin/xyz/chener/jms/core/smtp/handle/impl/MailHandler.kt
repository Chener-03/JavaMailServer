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
    }


    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse {
        if (session.sessionCache[HeloHandler.HELLO_KEY] == null){
            return SmtpResponse(SmtpResponseStatus.BadCommandSequence,false,"Error: send HELO/EHLO first")
        }

        if (command?.param == null) {
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax 0x59612")
        }

        // MAIL FROM:<chener0354@163.com> [SIZE=123456]  ---->  chener0354@163.com
        if (!command.param.trim().startsWith("FROM:")) {
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax 0x1265437")
        }

        var emailAddressString : String? = null
        var emailSize : Int? = null


        if (command.param.trim().startsWith("FROM:")) {
            runCatching {
                val left = command.param.trim().indexOf("<", 0, false)
                val right = command.param.trim().indexOf(">", left, false)
                emailAddressString = command.param.substring(left + 1, right)
            }
            runCatching {
                val left = command.param.trim().indexOf("[SIZE=", 0, false)
                val right = command.param.trim().indexOf("]", left, false)
                emailSize = command.param.substring(left + 6, right).toInt()
            }
        }

        emailSize?.let {
            if (it > session.properties.fileMaxSize){
                return SmtpResponse(SmtpResponseStatus.ExceededStorageAllocation,false,"Error: message size exceeds fixed maximum message size [${session.properties.fileMaxSize}]")
            }
        }


        if (emailAddressString.isNullOrBlank()){
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax 0x749613")
        }

        val parseEmailAddr = CommonUtils.parseEmailAddr(emailAddressString!!) ?: return SmtpResponse(
            SmtpResponseStatus.CommandUnrecognized,
            false,
            " Error: bad syntax 0x7146574"
        )

        if (!CommonUtils.checkDomain(parseEmailAddr.domain) || parseEmailAddr.username.isBlank()){
            return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,false,"Error: bad syntax 0x159761")
        }

        // 如果登录过 代表是发送给其它 from必须是登录的用户
        if (session.username != null && (parseEmailAddr.username != session.username || parseEmailAddr.domain != session.properties.domain)){
            return SmtpResponse(SmtpResponseStatus.MailboxNameNotAllowed,false,"Mail from must equal authorized user")
        }

        session.sessionCache[MAIL_KEY_ADDR] = emailAddressString!!

        return SmtpResponse(SmtpResponseStatus.Ok,false,"Mail OK")
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "MAIL"
    }
}