package xyz.chener.jms.core.smtp

import io.netty.channel.ChannelHandlerContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.base.BaseDispatchHandle
import xyz.chener.jms.core.base.CommandHandleManager
import xyz.chener.jms.core.smtp.entity.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

open class SmtpDispatchHandle(val serverProperties: SmtpServerProperties)
    : BaseDispatchHandle<SmtpClient,SmtpResponse>(commandHandleManager = CommandHandleManager(serverProperties.messageHandle)) {

    companion object{
        const val CONNECT_INIT_COMMAND = "WELCOME_CONNECT_SMTP"
    }

    private val log: Logger = LoggerFactory.getLogger(SmtpDispatchHandle::class.java)



    init {
        Thread.ofVirtual().start {
            while (true){
                val now = System.currentTimeMillis()
                clients.forEach { (k, v) ->
                    if (now - v.lastActiveTime > serverProperties.timeout){
                        processResp(SmtpResponse(SmtpResponseStatus.ServiceNotAvailable,true,"TIME OUT"),v.ctx)
                    }
                }
                Thread.sleep(1000)
            }
        }
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        val remoteAddress = ctx.channel().remoteAddress()

        val client = SmtpClient(ctx.channel().id().asLongText()
            ,System.currentTimeMillis()
            ,ctx
            , if (remoteAddress is InetSocketAddress) remoteAddress.address.hostAddress else ""
            ,ArrayList()
            ,ReentrantLock()
            ,serverProperties
        )
        log.info("smtp ip:${client.ipAddress}")
        clients[ctx.channel().id().asLongText()] = client
        val response = syncGetResp(client, CommandData(CONNECT_INIT_COMMAND,null,CONNECT_INIT_COMMAND))
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

    override fun getResp(client: SmtpClient, source: String): SmtpResponse? {
        if (client.keepHandle != null){
            return syncGetResp(client, CommandData(null,null,source))
        }
        val cmdPair = CommandHandleManager.spiltByFirstSpace(source)
        log.info("cmd:${cmdPair?.first}   msg:${cmdPair?.second}   source:$source")
        return syncGetResp(client, CommandData(cmdPair?.first,cmdPair?.second,source))
    }



    private fun syncGetResp(client: SmtpClient, cmd:CommandData):SmtpResponse?{
        return if (client.lock.tryLock(1,TimeUnit.MINUTES)) {
            try {
                if (client.keepHandle != null){
                    client.keepHandle!!.handleSmtp(client, cmd)
                }else{
                    commandHandleManager.getHandle(cmd).handleSmtp(client, cmd)
                }
            }finally {
                client.lock.unlock()
            }
        }else{
            SmtpResponse(SmtpResponseStatus.ServiceNotAvailable,true,"TIME OUT")
        }
    }

    override fun processResp(resp:SmtpResponse?, ctx: ChannelHandlerContext){

        resp?.perMessage?.let {
            ctx.channel().writeAndFlush("${resp.perMessage}")
        }
        resp?.status?.let{
            ctx.channel().writeAndFlush("${resp.status.code} ${resp.message}")
        }
        resp?.postMessage?.let {
            ctx.channel().writeAndFlush("${resp.postMessage}")
        }
        resp?.doLast?.run()
        if (resp?.kickClient == true){
            ctx.channel().close()
        }
    }

}