package xyz.chener.jms.core.smtp.service

import xyz.chener.jms.core.smtp.entity.UserEmail


typealias MailSendResultListener = (mailUUID: String, success: Boolean, errorMsg: String?) -> Unit

interface SmtpMailSender {

    fun sendMail(from: String, toList: List<UserEmail>, content: String, resultCallBack: MailSendResultListener?)

}

