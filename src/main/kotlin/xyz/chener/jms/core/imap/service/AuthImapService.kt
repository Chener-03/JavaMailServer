package xyz.chener.jms.core.imap.service

interface AuthImapService {
    fun login(username:String,password:String):Boolean
}