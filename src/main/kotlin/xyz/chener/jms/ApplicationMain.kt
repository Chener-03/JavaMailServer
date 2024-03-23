package xyz.chener.jms


import com.baomidou.mybatisplus.extension.kotlin.KtQueryChainWrapper
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import xyz.chener.jms.core.pop3.Pop3Server
import xyz.chener.jms.core.pop3.entity.Pop3ServerProperties
import xyz.chener.jms.core.smtp.SmtpServer
import xyz.chener.jms.core.smtp.entity.SmtpServerProperties
import xyz.chener.jms.core.smtp.service.impl.DefaultAsyncSmtpMailSenderImpl
import xyz.chener.jms.core.smtp.service.impl.DefaultMailServiceImpl
import xyz.chener.jms.ss.*
import xyz.chener.jms.ss.entity.EmailInfo
import xyz.chener.jms.ss.mapper.EmailInfoMapper
import java.io.ByteArrayInputStream
import java.util.*


class ApplicationMain {
    companion object {



        @JvmStatic
        fun main(args: Array<String>) {

            println("PID is ${ProcessHandle.current().pid()}")

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
            }

        }
    }
}