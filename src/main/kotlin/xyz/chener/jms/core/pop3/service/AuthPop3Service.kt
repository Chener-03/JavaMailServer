package xyz.chener.jms.core.pop3.service

interface AuthPop3Service {

    fun doLogin(username:String?,password:String?):Boolean

}