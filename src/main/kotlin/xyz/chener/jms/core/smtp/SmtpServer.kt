package xyz.chener.jms.core.smtp

import io.netty.channel.ChannelPipeline
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.BaseStringNettyService
import xyz.chener.jms.core.smtp.entity.SmtpServerProperties


class SmtpServer(private val serverProperties: SmtpServerProperties) : BaseStringNettyService(port = serverProperties.port, maxLineSize = serverProperties.fileMaxSize) {

    private val log: Logger = LoggerFactory.getLogger(SmtpServer::class.java)


    override fun addCustomChannelHandler(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(SmtpDispatchHandle(serverProperties))
    }

    override fun onServerStart(success: Boolean, errMsg: String?) {
        log.info("smtp server start ${if (success) "success" else "fail"}")
    }

}