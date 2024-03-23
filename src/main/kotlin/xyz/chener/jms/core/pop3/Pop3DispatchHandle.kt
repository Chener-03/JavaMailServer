package xyz.chener.jms.core.pop3

import io.netty.channel.ChannelHandlerContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.BaseDispatchHandle
import xyz.chener.jms.core.base.CommandHandleManager
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.pop3.entity.Pop3ServerProperties
import xyz.chener.jms.core.smtp.entity.CommandData
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class Pop3DispatchHandle(private val pop3ServerProperties: Pop3ServerProperties)
    : BaseDispatchHandle<Pop3Clinet,Pop3Response>(commandHandleManager = CommandHandleManager(pop3ServerProperties.messageHandle)) {

    private val log: Logger = LoggerFactory.getLogger(Pop3DispatchHandle::class.java)


    companion object{
        const val CONNECT_INIT_COMMAND = "WELCOME_CONNECT_POP3"
    }


    init {

        Thread.ofVirtual().start {
            while (true){
                val now = System.currentTimeMillis()
                clients.forEach { (k, v) ->
                    if (now - v.lastActiveTime > pop3ServerProperties.timeout){
                        processResp(Pop3Response(false,"TIME OUT",true),v.ctx)
                    }
                }
                Thread.sleep(1000)
            }
        }
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        val remoteAddress = ctx.channel().remoteAddress()

        val client = Pop3Clinet(
            ctx.channel().id().asLongText()
            ,System.currentTimeMillis()
            ,ctx
            , if (remoteAddress is InetSocketAddress) remoteAddress.address.hostAddress else ""
            ,ArrayList()
            ,ReentrantLock()
            ,pop3ServerProperties
        )

        println("pop3 ip:${client.ipAddress}")
        clients[ctx.channel().id().asLongText()] = client

        val response = syncGetResp(client, CommandData(CONNECT_INIT_COMMAND,null, CONNECT_INIT_COMMAND))
        processResp(response,ctx)
    }



    override fun channelReadString(ctx: ChannelHandlerContext, msg: String) {
        val c = clients[ctx.channel().id().asLongText()]!!

        c.lastActiveTime = System.currentTimeMillis()
        c.msgLines.add(msg)
        println(msg)

        val resp = getResp(c, msg)
        processResp(resp,ctx)
    }



    override fun getResp(client: Pop3Clinet,source:String):Pop3Response?{
        if (client.keepHandle != null){
            return syncGetResp(client, CommandData(null,null,source))
        }

        val cmdPair = CommandHandleManager.spiltByFirstSpace(source)
        log.info("cmd:${cmdPair?.first}   msg:${cmdPair?.second}   source:$source")
        return syncGetResp(client, CommandData(cmdPair?.first,cmdPair?.second,source))
    }

    private fun syncGetResp(client: Pop3Clinet, cmd:CommandData):Pop3Response?{
        return if (client.lock.tryLock(1, TimeUnit.MINUTES)) {
            try {
                if (client.keepHandle != null){
                    client.keepHandle!!.handlePop3(client, cmd)
                }else{
                    commandHandleManager.getHandle(cmd).handlePop3(client, cmd)
                }
            }finally {
                client.lock.unlock()
            }
        }else{
            Pop3Response(false,"TIME OUT",true)
        }
    }



    override fun processResp(resp: Pop3Response?, ctx: ChannelHandlerContext){
        if (resp?.source != null){
            ctx.channel().writeAndFlush(resp.source)
        }
        else if (resp?.success != null || resp?.msg != null){
            if (resp.getStatusText() != null){
                println("${resp.getStatusText()} ${resp.msg}")
                ctx.channel().writeAndFlush("${resp.getStatusText()} ${resp.msg}")
            }else{
                ctx.channel().writeAndFlush("${resp.msg}")
            }
        }

        if (resp?.kickClient == true){
            ctx.channel().close()
        }
    }

}