package xyz.chener.jms.ss

import com.baomidou.mybatisplus.extension.kotlin.KtQueryChainWrapper
import xyz.chener.jms.core.pop3.service.AuthPop3Service
import xyz.chener.jms.core.smtp.service.AuthService
import xyz.chener.jms.ss.entity.UserInfo
import xyz.chener.jms.ss.mapper.UserInfoMapper

class MbpAuthRepo : AuthPop3Service,AuthService {
    override fun doLogin(username: String?, password: String?): Boolean {
        val userInfoMapper = SessionUtils.instance.getMapper(UserInfoMapper::class.java)
        val count = KtQueryChainWrapper(userInfoMapper, UserInfo::class.java)
            .eq(UserInfo::username, username)
            .eq(UserInfo::password, password)
            .count()
        return count > 0
    }

    override fun userCheck(username: String): Boolean {
        val userInfoMapper = SessionUtils.instance.getMapper(UserInfoMapper::class.java)
        return KtQueryChainWrapper(userInfoMapper, UserInfo::class.java)
            .eq(UserInfo::username, username)
            .exists()
    }
}