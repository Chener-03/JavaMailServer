package xyz.chener.jms


import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslProvider
import xyz.chener.jms.ss.*
import java.io.File
import java.util.*


class ApplicationMain {
    companion object {


        @Throws(java.lang.Exception::class)
        private fun createSSLContext(): SslContext {
            return SslContextBuilder.forServer(File("./cert.pem"), File("./key.pem"))
                .sslProvider(SslProvider.JDK)
                .build()
        }



        @JvmStatic
        fun main(args: Array<String>) {


            val bossGroup: EventLoopGroup = NioEventLoopGroup()
            val workerGroup: EventLoopGroup = NioEventLoopGroup()
            try {
                val b = ServerBootstrap()
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        @Throws(Exception::class)
                        public override fun initChannel(ch: SocketChannel) {
                            val pipeline = ch.pipeline()
                            pipeline.addLast(LineBasedFrameDecoder(10000))
                            pipeline.addLast(StringDecoder())
                            pipeline.addLast(StringEncoder())
                            pipeline.addLast(object:ChannelInboundHandlerAdapter(){
                                override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                    if (msg is String && msg == "STARTTLS"){
                                        val sslContext = createSSLContext()
                                        val engine = sslContext.newEngine(ctx.alloc())
                                        engine.useClientMode = false
                                        ctx.pipeline().addFirst("ssl", SslHandler(engine))
                                        ctx.pipeline().remove(this)
                                        println("start ssl")
                                        return
                                    }
                                    super.channelRead(ctx, msg);
                                }
                            })
                            pipeline.addLast(object:ChannelInboundHandlerAdapter(){
                                override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                    if (msg is String){
                                        println(msg)
                                    }
                                }
                            })
                        }
                    })

                val f: ChannelFuture = b.bind(808).sync()
                f.channel().closeFuture().sync()
            } finally {
                bossGroup.shutdownGracefully()
                workerGroup.shutdownGracefully()
            }


/*            println("PID is ${ProcessHandle.current().pid()}")

            Thread.ofPlatform().start {
                val p = SmtpServerProperties(25,System.getProperty("domain"),1024*1024*10,1000*20,"v1.0", authService = MbpAuthRepo()
                    , mailService = DefaultMailServiceImpl(DefaultAsyncSmtpMailSenderImpl(),MbpMailRepositoryImpl()))
                val server = SmtpServer(p)
                server.start()
            }

            Thread.ofPlatform().start {
                val p = Pop3ServerProperties(110,1024*1024*10,1000*20, mailRepository = MbpMailRepositoryImpl(), authPop3Service = MbpAuthRepo())
                val server = Pop3Server(p)
                server.start()
            }*/

        }
    }
}















