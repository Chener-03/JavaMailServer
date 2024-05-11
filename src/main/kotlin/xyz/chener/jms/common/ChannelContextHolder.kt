package xyz.chener.jms.common

import io.netty.channel.ChannelHandlerContext

class ChannelContextHolder {
    companion object {
        private val channelContext = InheritableThreadLocal<ChannelHandlerContext>()

        fun set(channelContext: ChannelHandlerContext) {
            this.channelContext.set(channelContext)
        }

        fun get(): ChannelHandlerContext? {
            return this.channelContext.get()
        }

        fun remove() {
            this.channelContext.remove()
        }
    }
}