package xyz.chener.jms.core.imap

import io.netty.channel.ChannelPipeline
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.BaseStringNettyService
import xyz.chener.jms.core.imap.entity.ImapServerProperties

class ImapServer(private val imapServerProperties: ImapServerProperties) : BaseStringNettyService(imapServerProperties.port,imapServerProperties.fileMaxSize) {

    private val log: Logger = LoggerFactory.getLogger(ImapServer::class.java)

    override fun addCustomChannelHandler(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(ImapDispatchHandler(imapServerProperties))
    }

    override fun onServerStart(success: Boolean, errMsg: String?) {

    }
}