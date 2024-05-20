package xyz.chener.jms.core.base

import xyz.chener.jms.core.imap.entity.ImapClient
import xyz.chener.jms.core.imap.entity.ImapResponse
import xyz.chener.jms.core.imap.handle.*
import xyz.chener.jms.core.pop3.entity.Pop3Clinet
import xyz.chener.jms.core.pop3.entity.Pop3Response
import xyz.chener.jms.core.pop3.handle.impl.*
import xyz.chener.jms.core.smtp.entity.CommandData
import xyz.chener.jms.core.smtp.entity.SmtpClient
import xyz.chener.jms.core.smtp.entity.SmtpResponse
import xyz.chener.jms.core.smtp.handle.impl.*

interface MessageHandler {

    fun handleSmtp(session:SmtpClient, command: CommandData?): SmtpResponse? {
        return null
    }

    fun handlePop3(session:Pop3Clinet, command: CommandData?): Pop3Response? {
        return null
    }

    fun handleImap(session: ImapClient, command: CommandData?): ImapResponse? {
        return null
    }

    fun canProcess(command:CommandData?): Boolean {
        return false
    }

    companion object {
        fun defaultSmtpHandleChain():List<MessageHandler>{
            ArrayList<MessageHandler>().let {
                it.add(WelcomeHandle())
                it.add(HeloHandler())
                it.add(AuthLoginHandle())
                it.add(NoopHandler())
                it.add(MailHandler())
                it.add(RcptHandler())
                it.add(DataHandler())
                it.add(RsetHandler())
                it.add(VrfySmtpHandler())
                it.add(ExpnSmtpHandler())
                it.add(SslHandler())
                it.add(QuitHandler())
                return it
            }
        }

        fun defaultPop3HandleChain():List<MessageHandler>{
            ArrayList<MessageHandler>().let {
                it.add(WelcomePop3Handler())
                it.add(AuthPop3Handler())
                it.add(DelePop3Handler())
                it.add(ListPop3Handler())
                it.add(NoopPop3Handler())
                it.add(QuitPop3Handler())
                it.add(RetrPop3Handler())
                it.add(RsetPop3Handler())
                it.add(StatPop3Handler())
                it.add(UidlPop3Handler())
                it.add(TopPop3Handler())
                it.add(SslPop3Handler())
                it.add(CapaPop3Handler())
                return it
            }
        }


        fun defaultImapHandleChain():List<MessageHandler>{
            ArrayList<MessageHandler>().let {
                it.add(ImapWelcomeHandle())
                it.add(ImapCapaHandle())
                it.add(ImapSslHandle())
                it.add(ImapNoopHandle())
                it.add(ImapLoginHandle())
                return it
            }
        }

    }

}