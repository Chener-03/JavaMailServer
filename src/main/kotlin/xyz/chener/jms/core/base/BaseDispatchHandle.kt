package xyz.chener.jms.core.base

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

abstract class BaseDispatchHandle<CLIENT,RESPONSE>(protected var commandHandleManager: CommandHandleManager)
    : ChannelInboundHandlerAdapter() {

    protected val clients : MutableMap<String, CLIENT> = HashMap()


    override fun channelInactive(ctx: ChannelHandlerContext) {
        clients.remove(ctx.channel().id().asLongText())
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any){
        if (msg is String && clients[ctx.channel().id().asLongText()] != null){
            channelReadString(ctx,msg)
        }
    }

    abstract fun channelReadString(ctx: ChannelHandlerContext, msg: String)

    abstract  fun getResp(client: CLIENT, source:String):RESPONSE?

    abstract fun processResp(resp: RESPONSE?, ctx: ChannelHandlerContext)

}