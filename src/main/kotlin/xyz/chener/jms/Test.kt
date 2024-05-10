package xyz.chener.jms

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.util.InsecureTrustManagerFactory


class Test {
    companion object {


        @JvmStatic
        fun main(args: Array<String>) {
            val group: EventLoopGroup = NioEventLoopGroup()
            try {
                val b: Bootstrap = Bootstrap()
                b.group(group)
                    .channel(NioSocketChannel::class.java)
                    .handler(object : ChannelInitializer<SocketChannel>() {
                        @Throws(Exception::class)
                        public override fun initChannel(ch: SocketChannel) {
                            val pipeline: ChannelPipeline = ch.pipeline()
                            pipeline.addLast(LineBasedFrameDecoder(10000))
                            pipeline.addLast(StringDecoder())
                            pipeline.addLast(StringEncoder())
                            pipeline.addLast(object : ChannelInboundHandlerAdapter(){
                                override fun channelActive(ctx: ChannelHandlerContext) {
                                    ctx.writeAndFlush("STARTTLS\n")
                                    run {
                                        val sslContext = SslContextBuilder.forClient()
                                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                            .build()
                                        val engine = sslContext.newEngine(ctx.alloc())
                                        ctx.pipeline().addFirst("ssl", SslHandler(engine))
                                        ctx.pipeline().remove(this)
                                        println("start ssl")
                                        Thread.ofVirtual().start{
                                            while (true){
                                                Thread.sleep(1000)
                                                ctx.writeAndFlush("123 \n")
                                            }
                                        }
                                        return
                                    }
                                }

                                override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                    if (msg is String){
                                        println(msg)
                                    }
                                }
                            })
                        }
                    })

                val f: ChannelFuture = b.connect("127.0.0.1", 808).sync()
                f.channel().closeFuture().sync()
            } finally {
                group.shutdownGracefully()
            }
        }

    }
}