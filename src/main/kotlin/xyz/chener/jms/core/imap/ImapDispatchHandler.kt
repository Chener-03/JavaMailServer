package xyz.chener.jms.core.imap

import io.netty.channel.ChannelHandlerContext
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

class ImapDispatchHandler(val imapServerProperties: ImapServerProperties)
    : BaseDispatchHandle<ImapClient, ImapResponse>(commandHandleManager = CommandHandleManager(imapServerProperties.messageHandle)) {



    companion object{
        const val CONNECT_INIT_COMMAND = "WELCOME_CONNECT_IMAP"
    }


    init {

        Thread.ofVirtual().start {
            while (true){
                val now = System.currentTimeMillis()
                clients.forEach { (k, v) ->
                    if (now - v.lastActiveTime > imapServerProperties.timeout){
                        processResp(ImapResponse(kickClient = true, content = "* Timeout"),v.ctx)
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

        val imapCmd = CommandHandleManager.getImapCmd(source) ?: return ImapResponse(kickClient = false, content = "* BAD invalid command")

        return commandHandleManager.getHandle(imapCmd).handleImap(client,imapCmd)
    }

/*
OK IMAP4 ready
a CAPABILITY
* CAPABILITY IMAP4rev1 XLIST SPECIAL-USE LITERAL+ STARTTLS APPENDLIMIT=71680000 XAPPLEPUSHSERVICE UIDPLUS X-CM-EXT-1 SASL-IR AUTH=PLAIN AUTH=LOGIN AUTH=XOAUTH2 ID STARTTLS
a OK completed
b v
b BAD invalid command
c starttls
c OK completed
* */


    override fun processResp(resp: ImapResponse?, ctx: ChannelHandlerContext) {
        if (resp?.source != null){
            ctx.channel().writeAndFlush(resp.source)
        }else {
            if (resp?.content?.isNotEmpty() == true){
                ctx.channel().writeAndFlush(resp.content)
            }

            if (resp?.success != null){
                ctx.channel().writeAndFlush(resp.buildEndString())
            }

            if (resp?.postMessage?.isNotEmpty() == true){
                ctx.channel().writeAndFlush(resp.postMessage)
            }
        }

        resp?.doLast?.invoke()
        if (resp?.kickClient == true){
            ctx.channel().close()
        }
    }
}