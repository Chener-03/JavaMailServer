package xyz.chener.jms.core.imap

import io.netty.channel.ChannelHandlerContext
import xyz.chener.jms.core.base.BaseDispatchHandle
import xyz.chener.jms.core.base.CommandHandleManager

class ImapDispatchHandler(commandHandleManager: CommandHandleManager): BaseDispatchHandle<ImapClient, ImapResponse>(commandHandleManager) {
    override fun channelReadString(ctx: ChannelHandlerContext, msg: String) {
        TODO("Not yet implemented")
    }

    override fun getResp(client: ImapClient, source: String): ImapResponse? {
        TODO("Not yet implemented")
    }

/*
OK IMAP4 ready
a CAPABILITY
* CAPABILITY IMAP4rev1 XLIST SPECIAL-USE LITERAL+ STARTTLS APPENDLIMIT=71680000 XAPPLEPUSHSERVICE UIDPLUS X-CM-EXT-1 SASL-IR AUTH=PLAIN AUTH=LOGIN AUTH=XOAUTH2 ID STARTTLS
a OK completed
b v
b BAD invalid command
c starttls
c OK completed
* */


    override fun processResp(resp: ImapResponse?, ctx: ChannelHandlerContext) {
        if (resp?.source != null){
            ctx.channel().writeAndFlush(resp.source)
        }else {
            if (resp?.content?.isNotEmpty() == true){
                ctx.channel().writeAndFlush(resp.content)
            }
            ctx.channel().writeAndFlush(resp?.buildEndString())
            if (resp?.postMessage?.isNotEmpty() == true){
                ctx.channel().writeAndFlush(resp.postMessage)
            }
        }

        resp?.doLast?.invoke()
        if (resp?.kickClient == true){
            ctx.channel().close()
        }
    }
}