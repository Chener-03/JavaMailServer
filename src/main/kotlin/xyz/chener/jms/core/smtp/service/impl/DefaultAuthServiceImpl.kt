package xyz.chener.jms.core.smtp.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.smtp.service.AuthService

class DefaultAuthServiceImpl : AuthService {

    private val log: Logger = LoggerFactory.getLogger(DefaultAuthServiceImpl::class.java)

    override fun doLogin(username: String?, password: String?):Boolean {
        log.info("login in : {}  {}",username,password)
        return true
    }

    override fun userCheck(username: String): Boolean {
        return true
    }
}