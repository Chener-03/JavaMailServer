package xyz.chener.jms.core.smtp.handle.impl

import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.common.ChannelContextHolder
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.entity.SmtpResponseStatus
import java.io.File
import java.nio.file.Files

class SslHandler:MessageHandler {

    companion object {
        const val SSL_PIPELINE_HANDLE_NAME = "SslHandler"

        private val log: Logger = LoggerFactory.getLogger(SslHandler::class.java)

        fun support():Boolean {
            val certPath = System.getProperty("ssl.cert")
            val keyPath = System.getProperty("ssl.key")
            if (certPath.isNullOrEmpty() || keyPath.isNullOrEmpty()){
                return false
            }
            if (!Files.exists(java.nio.file.Path.of(certPath)) || !Files.exists(java.nio.file.Path.of(keyPath))){
                return false
            }
            return createSSLContext() != null
        }

        private fun createSSLContext(): SslContext? {
            return try {
                SslContextBuilder.forServer(File(System.getProperty("ssl.cert")), File(System.getProperty("ssl.key")))
                    .sslProvider(SslProvider.JDK)
                    .build()
            }catch (e:Exception){
                log.warn("create ssl context error",e)
                null
            }
        }
    }


    override fun handleSmtp(session: SmtpClient, command: CommandData?): SmtpResponse? {
        if (!support()){
            return SmtpResponse(SmtpResponseStatus.CommandNotImplemented,true,"Not support TLS.")
        }
        val context = ChannelContextHolder.get() ?: return SmtpResponse(SmtpResponseStatus.CommandUnrecognized,true,"Unknown context.")

        if (context.pipeline().names().contains(SSL_PIPELINE_HANDLE_NAME)) {
            log.warn("TLS already started.")
            return SmtpResponse(SmtpResponseStatus.ServiceReady,false,"Ready to start TLS.")
        }

        val sslContext = createSSLContext()!!
        val engine = sslContext.newEngine(context.alloc())
        engine.useClientMode = false

        return SmtpResponse(SmtpResponseStatus.ServiceReady,false,"Ready to start TLS."){
            context.pipeline().addFirst(SSL_PIPELINE_HANDLE_NAME, io.netty.handler.ssl.SslHandler(engine))
        }
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "STARTTLS"
    }
}