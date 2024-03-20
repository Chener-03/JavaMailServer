package xyz.chener.jms.ss

import com.baomidou.mybatisplus.core.toolkit.BeanUtils
import com.baomidou.mybatisplus.extension.kotlin.KtQueryChainWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateChainWrapper
import xyz.chener.jms.core.smtp.entity.EmailState
import xyz.chener.jms.core.smtp.entity.EmailType
import xyz.chener.jms.core.smtp.entity.UserEmail
import xyz.chener.jms.repositorys.MailRepository
import xyz.chener.jms.ss.entity.EmailInfo
import xyz.chener.jms.ss.mapper.EmailInfoMapper
import java.util.*
import java.util.regex.Pattern

open class MbpMailRepositoryImpl : MailRepository {

    private fun relativeIdToAbsoluteId(username: String, id: Int?): Int? {
        id ?: return null
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)
        KtQueryChainWrapper(emailInfoMapper,EmailInfo::class.java)
            .select(EmailInfo::id)
            .eq(EmailInfo::username, username)
            .eq(EmailInfo::type,EmailType.RECEIVE.code)
            .orderByDesc(EmailInfo::id)
            .last("limit ${id-1},1").one()?.let {
                return it.id!!
            }
        return -1
    }

    override fun save(mailList: List<UserEmail>) {
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)
        mailList.forEach {
            val em = EmailInfo.createByUserEmail(it)
            if (em.subject.isNullOrEmpty()){
                em.subject = findContentSubject(em.content)
            }
            emailInfoMapper.insert(em)
        }
    }

    override fun updateMailSendState(uuid: String, success: Boolean, errorMsg: String?) {
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)

        KtUpdateChainWrapper(emailInfoMapper,EmailInfo::class.java)
            .set(EmailInfo::state, if (success) EmailState.SEND_SUCCESS.code else EmailState.SEND_FAILED.code)
            .set(EmailInfo::sendfailmsg, errorMsg)
            .eq(EmailInfo::uuid, uuid)
            .update()
    }

    override fun getEmailCountAndSize(username: String, id: Int?): Pair<Int, Int> {
        val aid = relativeIdToAbsoluteId(username, id)
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)
        val countAndSize = emailInfoMapper.getCountAndSize(username, aid)
        return Pair(countAndSize["ct"].toString().toInt(), countAndSize["sz"].toString().toInt())
    }

    override fun getEmailUidList(username: String, id: Int?): List<Pair<Int, String>> {
        val aid = relativeIdToAbsoluteId(username, id)
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)

        return KtQueryChainWrapper(emailInfoMapper,EmailInfo::class.java)
            .select(EmailInfo::id, EmailInfo::uuid)
            .eq(EmailInfo::username, username)
            .eq(EmailInfo::type,EmailType.RECEIVE.code)
            .eq(aid != null, EmailInfo::id, aid)
            .orderByDesc(EmailInfo::id)
            .list().mapIndexed { index,it->
                Pair(index+1, it.uuid!!)
            }.toList()
    }

    override fun getEmailIdAndSizeList(username: String, id: Int?): List<Pair<Int, Int>> {
        val aid = relativeIdToAbsoluteId(username, id)
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)

        return emailInfoMapper.getIdAndSize(username, aid).mapIndexed {index,it->
            Pair(index+1, it["sz"].toString().toInt())
        }
    }

    override fun deleteEmail(username: String, id: List<Int>) {
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)
        emailInfoMapper.deleteBatchIds(id.map {
            relativeIdToAbsoluteId(username, it)!!
        })
    }

    override fun getEmailById(username: String, id: Int): UserEmail? {
        val aid = relativeIdToAbsoluteId(username, id)
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)
        emailInfoMapper.selectById(aid)?.let {
            return EmailInfo.toUserEmail(it)
        }
        return null
    }

    override fun getEmailTopByIndex(username: String, index: Int, lines: Int): String? {
        val aid = relativeIdToAbsoluteId(username, index)
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)
        val e = emailInfoMapper.selectById(aid)
        return findTitleBody(e?.content)
    }


    private fun findContentSubject(content:String?):String?{
        content?:return null
        val pattern = Pattern.compile("Subject: (.*)")
        val matcher = pattern.matcher(content)
        if (matcher.find()){
            return base64ToString(matcher.group(1))
        }
        return null
    }

    private fun base64ToString(str:String):String{
        // =?utf-8?B?6YKu5Lu2MQ==?=
        val pattern = Pattern.compile("=\\?utf-8\\?B\\?(.*)\\?=")
        val matcher = pattern.matcher(str)
        if (matcher.find()){
            val decode = Base64.getDecoder().decode(matcher.group(1))
            return String(decode)
        }
        return str
    }

    private fun findTitleBody(content: String?): String? {
        if (content == null) return null
        val i = content.indexOf("\n\n")
        return if (i == -1) content else content.substring(0,i + 2)
    }

}