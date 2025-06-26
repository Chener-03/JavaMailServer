package xyz.chener.jms.repositorys

import xyz.chener.jms.core.imap.entity.ImapEmails
import xyz.chener.jms.core.imap.entity.ImapFolder
import xyz.chener.jms.core.smtp.entity.UserEmail

interface MailRepository {

    // smtp

    fun save(mailList:List<UserEmail>)

    fun updateMailSendState(uuid: String, success: Boolean, errorMsg: String?)


    // pop3 support

    fun getEmailCountAndSize(username: String,id:Int? = null): Pair<Int,Int>

    fun getEmailUidList(username: String,id:Int? = null): List<Pair<Int,String>>

    fun getEmailIdAndSizeList(username: String,id:Int? = null): List<Pair<Int,Int>>

    fun deleteEmail(username: String, id: List<Int>)

    fun getEmailById(username: String, id: Int): UserEmail?

    fun getEmailTopByIndex(username: String, index: Int, lines: Int): Pair<Int?,String?>?

    // imap support
    fun imapListDirectory(username: String): List<ImapFolder>

    fun imapSelectOneFolder(username: String, folder: String): ImapFolder?

    fun imapSelectListByFolder(username: String, fid: Int): List<ImapEmails>

    fun imapCreateFolder(username: String, folder: String): ImapFolder?

    fun imapDeleteFolder(username: String, folder: String)

    fun imapRenameFolder(username: String, oldFolder: String, newFolder: String)
}