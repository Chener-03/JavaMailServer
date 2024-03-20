package xyz.chener.jms.common

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.util.CharsetUtil

class StringCRLFHandler : ChannelOutboundHandlerAdapter() {
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val response = msg.toString() + "\r\n"
        val byteBuf = Unpooled.copiedBuffer(response, CharsetUtil.UTF_8)
        ctx.write(byteBuf, promise)
    }
}