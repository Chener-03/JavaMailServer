package xyz.chener.jms


import io.netty.channel.oio.OioEventLoopGroup
import xyz.chener.jms.common.StringBioClient
import xyz.chener.jms.core.pop3.Pop3Server
import xyz.chener.jms.core.pop3.entity.Pop3ServerProperties
import xyz.chener.jms.core.smtp.SmtpServer
import xyz.chener.jms.core.smtp.entity.SmtpServerProperties
import xyz.chener.jms.core.smtp.service.impl.DefaultAsyncSmtpMailSenderImpl
import xyz.chener.jms.core.smtp.service.impl.DefaultMailServiceImpl
import xyz.chener.jms.ss.MbpAuthRepo
import xyz.chener.jms.ss.MbpMailRepositoryImpl


class ApplicationMain {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val c = StringBioClient("smtp.163.com", 25)

            val readLine = c.readAllLines(1000)
            c.writeLine("EHLO A")
            val readLine2 = c.readAllLines(1000)
            println("PID is ${ProcessHandle.current().pid()}")

            c.close()
            return


            Thread.ofPlatform().start {
                val p = SmtpServerProperties(25,System.getProperty("domain"),1024*1024*10,1000*20,"v1.1", authService = MbpAuthRepo()
                    , mailService = DefaultMailServiceImpl(DefaultAsyncSmtpMailSenderImpl(),MbpMailRepositoryImpl())
                )
                val server = SmtpServer(p)
                server.start()
            }

            Thread.ofPlatform().start {
                val p = Pop3ServerProperties(110,1024*1024*10,1000*20, mailRepository = MbpMailRepositoryImpl(), authPop3Service = MbpAuthRepo())
                val server = Pop3Server(p)
                server.start()
            }

        }
    }
}















