package xyz.chener.jms.core.smtp.service

interface AuthService {

    fun doLogin(username:String?,password:String?):Boolean

    fun userCheck(username:String):Boolean

}