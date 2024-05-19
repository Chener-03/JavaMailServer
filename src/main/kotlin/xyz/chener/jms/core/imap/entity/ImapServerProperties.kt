package xyz.chener.jms.core.imap.entity

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.service.AuthImapService
import xyz.chener.jms.repositorys.MailRepository

class ImapServerProperties(
    // 143
    var port:Int,

    // 文件最大大小
    var fileMaxSize:Int,

    // 连接超时时间
    var timeout:Int,

    // 消息处理器
    var messageHandle:List<MessageHandler> = MessageHandler.defaultImapHandleChain(),

    var authImapService: AuthImapService? = null,

    var mailRepository: MailRepository? = null,
)