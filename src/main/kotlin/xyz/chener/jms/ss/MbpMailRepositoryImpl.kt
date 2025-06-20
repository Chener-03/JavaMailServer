package xyz.chener.jms.ss

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.toolkit.BeanUtils
import com.baomidou.mybatisplus.extension.kotlin.KtQueryChainWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateChainWrapper
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import xyz.chener.jms.common.CommonUtils
import xyz.chener.jms.core.imap.entity.ImapEmails
import xyz.chener.jms.core.imap.entity.ImapFolder
import xyz.chener.jms.core.smtp.entity.EmailState
import xyz.chener.jms.core.smtp.entity.EmailType
import xyz.chener.jms.core.smtp.entity.UserEmail
import xyz.chener.jms.repositorys.MailRepository
import xyz.chener.jms.ss.entity.EmailInfo
import xyz.chener.jms.ss.entity.Emails
import xyz.chener.jms.ss.entity.Folder
import xyz.chener.jms.ss.mapper.EmailInfoMapper
import xyz.chener.jms.ss.mapper.EmailsMapper
import xyz.chener.jms.ss.mapper.FolderMapper
import java.io.ByteArrayInputStream
import java.util.*
import java.util.regex.Pattern

open class MbpMailRepositoryImpl : MailRepository {

    val session: Session = Session.getDefaultInstance(Properties())

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

    override fun getEmailTopByIndex(username: String, index: Int, lines: Int): Pair<Int?,String?>? {
        val aid = relativeIdToAbsoluteId(username, index)
        val emailInfoMapper = SessionUtils.instance.getMapper(EmailInfoMapper::class.java)
        val e = emailInfoMapper.selectById(aid)

        return Pair(e.content?.length, findTitleBody(e.content,lines))
    }



    private fun findContentSubject(content:String?):String?{
        content?:return null
        try {
            val message = MimeMessage(session, ByteArrayInputStream(content.toByteArray()))
            return message.subject
        }catch (ex:Exception){
            ex.printStackTrace()
            return null
        }
    }


    private fun findTitleBody(content: String?,bodyLine:Int = 0): String? {
        content ?: return null
        try {
            val message = MimeMessage(session, ByteArrayInputStream(content.toByteArray()))
            val sb = StringBuilder()
            message.allHeaderLines.asIterator().forEach {
                sb.append(it).append("\r\n")
            }

            if (bodyLine>0){
                String(message.rawInputStream.readAllBytes()).lines().forEachIndexed { index, s ->
                    if (index<bodyLine){
                        sb.append(s).append("\r\n")
                    }
                }
            }

            return sb.toString()
        }catch (ex:Exception){
            ex.printStackTrace()
            return null
        }
    }




    override fun imapListDirectory( username: String): List<ImapFolder> {
        val mapper = SessionUtils.instance.getMapper(FolderMapper::class.java)
        val defaultList = mapper.selectList(
            KtQueryWrapper<Folder>(Folder::class.java)
                .eq(Folder::default, true)
        )


        val userList = mapper.selectList(
            KtQueryWrapper<Folder>(Folder::class.java)
                .eq(Folder::default, false).eq(Folder::username, username)
        )

        return ArrayList<ImapFolder>().let {
            it.addAll(defaultList.map {x->
                ImapFolder(x.folderPath, x.tag, x.default,x.id)
            }.toList())
            it.addAll(userList.map {x->
                ImapFolder(x.folderPath, x.tag, x.default,x.id)
            }.toList())
            it
        }
    }


    override fun selectOneFolder(username: String, folder: String ): ImapFolder? {
        val fm = SessionUtils.instance.getMapper(FolderMapper::class.java)

        KtQueryChainWrapper<Folder>(fm)
            .isNull(Folder::username)
            .eq(Folder::folderPath, CommonUtils.decodeModifiedUTF7(folder))
            .eq(Folder::default, true)
           .one()?.let {
                return ImapFolder(it.folderPath, it.tag, it.default,it.id)
            }

        KtQueryChainWrapper<Folder>(fm)
           .eq(Folder::username, username)
           .eq(Folder::folderPath, CommonUtils.decodeModifiedUTF7(folder))
           .eq(Folder::default, false)
           .one()?.let {
                return ImapFolder(it.folderPath, it.tag, it.default,it.id)
            }
        return null
    }


    override fun selectListByFolder(username: String, fid: Int): List<ImapEmails> {
        val em = SessionUtils.instance.getMapper(EmailsMapper::class.java)

        KtQueryChainWrapper<Emails>(em)
            .eq(Emails::username, username)
            .eq(Emails::folderId, fid)
            .orderByDesc(Emails::id)
            .list().map {
                return@map ImapEmails().apply {
                    org.springframework.beans.BeanUtils.copyProperties(it, this)
                }
            }.let {
                return it
            }
    }


    override fun createFolder( username: String, folder: String ): ImapFolder? {
        val fm = SessionUtils.instance.getMapper(FolderMapper::class.java)

        if (KtQueryChainWrapper<Folder>(fm)
                .isNull(Folder::username)
                .eq(Folder::folderPath, folder)
                .eq(Folder::default, true)
                .exists() ) {
            return null
        }

        if (KtQueryChainWrapper<Folder>(fm)
               .eq(Folder::username, username)
               .eq(Folder::folderPath, folder)
               .eq(Folder::default, false)
               .exists() ) {
            return null
        }

        val f = Folder().apply {
            this.folderPath = folder
            this.username = username
            this.default = false
        }

        fm.insert(f)
        return ImapFolder(f.folderPath, f.tag, f.default,f.id)
    }
}