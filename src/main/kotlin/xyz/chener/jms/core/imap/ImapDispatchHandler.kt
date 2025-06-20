package xyz.chener.jms.core.imap

import io.netty.channel.ChannelHandlerContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.BaseDispatchHandle
import xyz.chener.jms.core.base.CommandHandleManager
import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.imap.entity.ImapServerProperties
import xyz.chener.jms.core.pop3.Pop3DispatchHandle
import xyz.chener.jms.core.pop3.Pop3DispatchHandle.Companion
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.smtp.entity.CommandData
import java.net.InetSocketAddress
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.log

class ImapDispatchHandler(val imapServerProperties: ImapServerProperties)
    : BaseDispatchHandle<ImapClient, ImapResponse>(commandHandleManager = CommandHandleManager(imapServerProperties.messageHandle)) {

    private val log : Logger = LoggerFactory.getLogger(ImapDispatchHandler::class.java)


    companion object{
        const val CONNECT_INIT_COMMAND = "WELCOME_CONNECT_IMAP"
    }


    init {
        Thread.ofVirtual().start {
            while (true){
                val now = System.currentTimeMillis()
                clients.forEach { (k, v) ->
                    if (now - v.lastActiveTime > imapServerProperties.timeout){
                        processResp(ImapResponse(kickClient = true, success = false ,message = "* Timeout"),v.ctx)
                    }
                }
                Thread.sleep(1000)
            }
        }
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        val remoteAddress = ctx.channel().remoteAddress()

        val client = ImapClient(
            ctx.channel().id().asLongText()
            ,System.currentTimeMillis()
            ,ctx
            , if (remoteAddress is InetSocketAddress) remoteAddress.address.hostAddress else "",
            imapServerProperties
        )

        clients[ctx.channel().id().asLongText()] = client

        val response = getResp(client, "$CONNECT_INIT_COMMAND $CONNECT_INIT_COMMAND")
        processResp(response,ctx)
    }

    override fun channelReadString(ctx: ChannelHandlerContext, msg: String) {
        val c = clients[ctx.channel().id().asLongText()]!!

        c.lastActiveTime = System.currentTimeMillis()
        println(msg)

        val resp = getResp(c, msg)
        processResp(resp,ctx)
    }

    override fun getResp(client: ImapClient, source: String): ImapResponse? {

        val imapCmd = CommandHandleManager.getImapCmd(source) ?:
        return ImapResponse(kickClient = false, success = false, message = "Parse command error")

        try {
            return commandHandleManager.getHandle(imapCmd).handleImap(client,imapCmd)
        }catch (ex:Exception){
            log.error("Handle command error",ex)
            return ImapResponse(kickClient = false, success = false, message = "Handle command error")
        }
    }


    override fun processResp(resp: ImapResponse?, ctx: ChannelHandlerContext) {
        if (resp == null) {
            return
        }

        if (resp.isRawData){
            ctx.channel().writeAndFlush(resp.rawContent)
        }else {
            ctx.channel().writeAndFlush(resp.buildEndString())
        }

        resp.doLast?.invoke()

        if (resp.kickClient){
            ctx.channel().close()
        }
    }
}