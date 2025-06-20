package xyz.chener.jms.core.base

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPipeline
import io.netty.channel.IoEventLoopGroup
import io.netty.channel.IoHandler
import io.netty.channel.IoHandlerFactory
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.MultithreadEventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoEvent
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import xyz.chener.jms.common.StringCRLFHandler

abstract class BaseStringNettyService(val port:Int,val maxLineSize:Int) {

    protected var bossGroup :IoEventLoopGroup ? = null

    protected var workGroup :IoEventLoopGroup ? = null

    protected var channel : Channel? = null

    abstract fun addCustomChannelHandler(channelPipeline: ChannelPipeline)

    abstract fun onServerStart(success:Boolean,errMsg:String?)

    fun start(){

        bossGroup = MultiThreadIoEventLoopGroup(
            1,
            Thread.ofPlatform().factory(),
            if (KQueue.isAvailable()) {
                KQueueIoHandler.newFactory()
            } else if (Epoll.isAvailable()) {
                EpollIoHandler.newFactory()
            } else {
                NioIoHandler.newFactory()
            }
        )

        workGroup = MultiThreadIoEventLoopGroup(
            10,
            Thread.ofVirtual().factory(),
            if (KQueue.isAvailable()) {
                KQueueIoHandler.newFactory()
            } else if (Epoll.isAvailable()) {
                EpollIoHandler.newFactory()
            } else {
                NioIoHandler.newFactory()
            }
        )

        val chanelClass = if (KQueue.isAvailable()) {
            KQueueServerSocketChannel::class.java
        } else if (Epoll.isAvailable()) {
            EpollServerSocketChannel::class.java
        } else {
            NioServerSocketChannel::class.java
        }

        try {
            val bootstrap : ServerBootstrap = ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(chanelClass)
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