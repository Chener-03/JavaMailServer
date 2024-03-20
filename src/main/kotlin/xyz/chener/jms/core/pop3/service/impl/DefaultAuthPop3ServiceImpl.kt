package xyz.chener.jms.core.pop3.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.chener.jms.core.pop3.service.AuthPop3Service

class DefaultAuthPop3ServiceImpl: AuthPop3Service {

    private val log: Logger = LoggerFactory.getLogger(DefaultAuthPop3ServiceImpl::class.java)


    override fun doLogin(username: String?, password: String?): Boolean {
        log.info("Pop3 Login  username:$username password:$password")
        return true
    }
}