package xyz.chener.jms.core.pop3.handle.impl

import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.common.ChannelContextHolder
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.handle.impl.SslHandler.Companion
import java.io.File
import java.nio.file.Files

class SslPop3Handler : MessageHandler {

    companion object {
        const val SSL_PIPELINE_HANDLE_NAME = "SslHandler"

        private val log: Logger = LoggerFactory.getLogger(SslPop3Handler::class.java)

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


    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "STLS"
    }

    override fun handlePop3(session: Pop3Clinet, command: CommandData?): Pop3Response? {
        val sslContext = createSSLContext() ?: return Pop3Response(false,"Not support TLS.",false)

        val context = ChannelContextHolder.get()
        if (context == null){
            log.warn("Unknown context.")
            return Pop3Response(true,"Unknown context.",false)
        }

        if (context.pipeline().names().contains(SSL_PIPELINE_HANDLE_NAME)) {
            log.warn("TLS already started.")
            return Pop3Response(true,"TLS already started.",false)
        }

        val engine = sslContext.newEngine(context.alloc())
        engine.useClientMode = false
        return Pop3Response(true,"Ready to start TLS.",false){
            context.pipeline().addFirst(SSL_PIPELINE_HANDLE_NAME, SslHandler(engine))
        }
    }
}