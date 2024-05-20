package xyz.chener.jms.core.imap.entity

import io.netty.channel.ChannelHandlerContext

class ImapClient(
    var channelId:String,

    // 上次响应时间
    var lastActiveTime:Long,

    var ctx: ChannelHandlerContext,

    // ip addr
    var ipAddress: String?,


    val properties: ImapServerProperties,

    var username:String? = null
)