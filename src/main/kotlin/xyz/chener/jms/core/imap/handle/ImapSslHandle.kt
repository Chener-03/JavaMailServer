package xyz.chener.jms.core.imap.handle

import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.common.ChannelContextHolder
import xyz.chener.jms.core.base.MessageHandler
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.pop3.handle.impl.SslPop3Handler
import xyz.chener.jms.core.pop3.handle.impl.SslPop3Handler.Companion
import xyz.chener.jms.core.smtp.entity.CommandData
import java.io.File
import java.nio.file.Files

class ImapSslHandle:MessageHandler {

    companion object {
        const val SSL_PIPELINE_HANDLE_NAME = "SslHandler"

        private val log: Logger = LoggerFactory.getLogger(ImapSslHandle::class.java)

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

    override fun handleImap(session: ImapClient, command: CommandData?): ImapResponse? {
        val ssl = createSSLContext() ?: return ImapResponse(success = false,message = "Not support TLS.", uid = command?.uid, kickClient = false)
        val context = ChannelContextHolder.get() ?: return ImapResponse(success = false, message = "Unknown context", uid = command?.uid, kickClient = false)

        val engine = ssl.newEngine(context.alloc())
        engine.useClientMode = false
        return ImapResponse(success = true, message = "completed", uid = command?.uid, kickClient = false){
            context.pipeline().addFirst(SslPop3Handler.SSL_PIPELINE_HANDLE_NAME, SslHandler(engine))
        }
    }

    override fun canProcess(command: CommandData?): Boolean {
        return command?.command != null && command.command.trim() == "STARTTLS"
    }
}