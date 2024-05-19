package xyz.chener.jms


import io.netty.channel.oio.OioEventLoopGroup
import xyz.chener.jms.common.StringBioClient
import xyz.chener.jms.core.imap.ImapServer
import xyz.chener.jms.core.imap.entity.ImapServerProperties
import xyz.chener.jms.core.pop3.Pop3Server
import xyz.chener.jms.core.pop3.entity.Pop3ServerProperties
import xyz.chener.jms.core.smtp.SmtpServer
import xyz.chener.jms.core.smtp.entity.SmtpServerProperties
import xyz.chener.jms.core.smtp.entity.UserEmail
import xyz.chener.jms.core.smtp.service.MailSendResultListener
import xyz.chener.jms.core.smtp.service.impl.DefaultAsyncNettySmtpMailSenderImpl
import xyz.chener.jms.core.smtp.service.impl.DefaultAsyncSmtpMailSenderImpl
import xyz.chener.jms.core.smtp.service.impl.DefaultMailServiceImpl
import xyz.chener.jms.ss.MbpAuthRepo
import xyz.chener.jms.ss.MbpMailRepositoryImpl
import java.util.Date


class ApplicationMain {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {


/*            Thread.ofPlatform().start {
                val p = SmtpServerProperties(25,System.getProperty("domain"),1024*1024*10,1000*20,"v1.1", authService = MbpAuthRepo()
                    , mailService = DefaultMailServiceImpl(DefaultAsyncNettySmtpMailSenderImpl(),MbpMailRepositoryImpl())
                )
                val server = SmtpServer(p)
                server.start()
            }

            Thread.ofPlatform().start {
                val p = Pop3ServerProperties(110,1024*1024*10,1000*20, mailRepository = MbpMailRepositoryImpl(), authPop3Service = MbpAuthRepo())
                val server = Pop3Server(p)
                server.start()
            }*/

            Thread.ofPlatform().start{
                val p = ImapServerProperties(143,1024*1024*10,1000*20)
                val server = ImapServer(p)
                server.start()
            }


        }
    }
}















