package xyz.chener.jms.core.smtp.service

import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpServiceContext

interface MailService {

    fun sendMail(from: String, to: List<String>, content: String,context: SmtpServiceContext):SmtpResponse
}