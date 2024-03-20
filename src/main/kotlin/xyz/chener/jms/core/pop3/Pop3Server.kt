package xyz.chener.jms.core.pop3

import io.netty.channel.ChannelPipeline
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.BaseStringNettyService
import xyz.chener.jms.core.pop3.entity.Pop3ServerProperties

class Pop3Server(private val pop3ServerProperties: Pop3ServerProperties)
    :BaseStringNettyService(port = pop3ServerProperties.port, maxLineSize = pop3ServerProperties.fileMaxSize) {


    private val log: Logger = LoggerFactory.getLogger(Pop3Server::class.java)


    override fun addCustomChannelHandler(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(Pop3DispatchHandle(pop3ServerProperties))
    }

    override fun onServerStart(success: Boolean, errMsg: String?) {
        log.info("pop3 server start ${if (success) "success" else "fail"}")
    }
}