package xyz.chener.jms.core.imap.service

interface AuthImapService {
    fun doLogin(username:String?,password:String?):Boolean
}