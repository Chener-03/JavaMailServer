package xyz.chener.jms.common

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import kotlin.jvm.Throws


class StringBioClient(private val host:String, private val port :Int) {

    companion object {
        val bossGroup: EventLoopGroup = NioEventLoopGroup(2)

        init {
            Runtime.getRuntime().addShutdownHook(Thread {
                bossGroup.shutdownGracefully()
            })
        }
    }

    private val bs = Bootstrap()

    private var channel: Channel? = null

    val queue = LinkedBlockingQueue<String>()

    init {
        bs.group(bossGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_KEEPALIVE,true)
            .handler(object:ChannelInitializer<SocketChannel>(){
                override fun initChannel(ch: SocketChannel) {
                    val pipeline = ch.pipeline()
                    pipeline.addLast(LineBasedFrameDecoder(1024 * 1024))
                    pipeline.addLast(StringDecoder())
                    pipeline.addLast(StringEncoder())
                    pipeline.addLast(object:ChannelInboundHandlerAdapter(){
                        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                            if (msg is String){
                                queue.add(msg)
                            }
                        }
                    })
                }
            })
        val channelFuture = bs.connect(host, port).sync()
        channel = channelFuture.channel()
    }

    fun ssl(ignoredSsl:Boolean = false){
        val contextBuilder = SslContextBuilder.forClient()
            .sslProvider(SslProvider.JDK)
        if (ignoredSsl){
            contextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE)
        }
        channel?.pipeline()?.forEach {
            if (it.key == "ssl_handler") {
                return@ssl
            }
        }
        channel?.pipeline()?.addFirst("ssl_handler",io.netty.handler.ssl.SslHandler(contextBuilder.build().newEngine(channel?.alloc())))
    }

    fun isConnected():Boolean{
        return channel?.isActive == true
    }

    @Throws(InterruptedException::class)
    fun readLine():String{
        return queue.take()
    }

    fun readLine(ms:Long):String?{
        return queue.poll(ms,java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    fun readAllLines(ms: Long):String{
        val sb = StringBuilder()
        while (true) {
            val line = readLine(ms) ?: break
            sb.append(line).append("\r\n")
        }
        return sb.toString()
    }


    fun writeLine(line:String){
        channel?.writeAndFlush("$line\r\n")
    }


    fun close(){
        channel?.close()
        queue.clear()
    }

}