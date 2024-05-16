package xyz.chener.jms.core.imap

import io.netty.channel.ChannelPipeline
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.BaseStringNettyService

class ImapServer(port:Int, maxLenSize:Int) : BaseStringNettyService(port,maxLenSize) {

    private val log: Logger = LoggerFactory.getLogger(ImapServer::class.java)

    override fun addCustomChannelHandler(channelPipeline: ChannelPipeline) {

    }

    override fun onServerStart(success: Boolean, errMsg: String?) {

    }
}