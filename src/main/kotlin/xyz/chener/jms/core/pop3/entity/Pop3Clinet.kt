package xyz.chener.jms.core.pop3.entity

import io.netty.channel.ChannelHandlerContext
import xyz.chener.jms.core.base.MessageHandler
import java.util.concurrent.locks.ReentrantLock

data class Pop3Clinet (
    var channelId:String,

    // 上次响应时间
    var lastActiveTime:Long,

    var ctx: ChannelHandlerContext,

    // ip addr
    var ipAddress: String?,

    // 历史记录
    var msgLines:MutableList<String>,

    // 对于单个client 每次只能有一个处理函数处理
    val lock: ReentrantLock,

    // 服务器信息
    val properties: Pop3ServerProperties,

    // 登录用户
    var username:String? = null,

    // 处理器数据缓存
    val sessionCache:MutableMap<String,Any> = HashMap(),

    // 当前是否被某处理器保持会话
    var keepHandle: MessageHandler? = null,
)