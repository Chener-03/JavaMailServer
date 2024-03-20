package xyz.chener.jms.core.pop3.entity

import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.service.AuthPop3Service
import xyz.chener.jms.core.pop3.service.impl.DefaultAuthPop3ServiceImpl
import xyz.chener.jms.repositorys.MailRepository

data class Pop3ServerProperties(

    // 110
    var port:Int,

    // 文件最大大小
    var fileMaxSize:Int,

    // 连接超时时间
    var timeout:Int,

    // 消息处理器
    var messageHandle:List<MessageHandler> = MessageHandler.defaultPop3HandleChain(),

    // 认证服务
    var authPop3Service: AuthPop3Service? = DefaultAuthPop3ServiceImpl(),

    var mailRepository: MailRepository? = null,
)