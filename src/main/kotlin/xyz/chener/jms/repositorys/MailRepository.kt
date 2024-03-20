package xyz.chener.jms.repositorys

import xyz.chener.jms.core.smtp.entity.UserEmail
import javax.swing.text.Style

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

    fun getEmailTopByIndex(username: String, index: Int, lines: Int): String?

}