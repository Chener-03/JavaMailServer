package xyz.chener.jms.core.smtp.entity

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.service.AuthService
import xyz.chener.jms.core.smtp.service.MailService
import xyz.chener.jms.core.smtp.service.impl.DefaultAsyncSmtpMailSenderImpl
import xyz.chener.jms.core.smtp.service.impl.DefaultAuthServiceImpl
import xyz.chener.jms.core.smtp.service.impl.DefaultMailServiceImpl

data class SmtpServerProperties(
    // 端口
    val port:Int,

    // 域名
    val domain:String,

    // 文件最大大小
    val fileMaxSize:Int,

    // 连接超时时间
    val timeout:Int,

    // 服务器版本
    val version:String,

    // 消息处理器
    var messageHandle:List<MessageHandler> = MessageHandler.defaultSmtpHandleChain(),

    // 登录用户处理器
    var authService: AuthService = DefaultAuthServiceImpl(),

    // smtp 邮件处理器
    var mailService: MailService = DefaultMailServiceImpl(DefaultAsyncSmtpMailSenderImpl(),null)
)
