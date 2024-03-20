package xyz.chener.jms.core.base

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPipeline
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import xyz.chener.jms.common.StringCRLFHandler

abstract class BaseStringNettyService(val port:Int,val maxLineSize:Int) {

    protected val bossGroup = NioEventLoopGroup(1)

    protected val workGroup = NioEventLoopGroup(5)

    protected var channel : Channel? = null

    abstract fun addCustomChannelHandler(channelPipeline: ChannelPipeline)

    abstract fun onServerStart(success:Boolean,errMsg:String?)

    fun start(){
        try {
            val bootstrap : ServerBootstrap = ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(socketChannel: SocketChannel) {
                        val pipeline = socketChannel.pipeline()
                        pipeline.addLast(LineBasedFrameDecoder(maxLineSize))
                        pipeline.addLast(StringDecoder())
                        pipeline.addLast(StringEncoder())
                        pipeline.addLast(StringCRLFHandler())
                        addCustomChannelHandler(pipeline)
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            val future = bootstrap.bind(port).sync()
            channel = future.channel()
            onServerStart(true,null)
            channel?.closeFuture()?.sync()
        }catch (ex:Exception){
            onServerStart(false,ex.message)
        }
    }

}