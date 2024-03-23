package xyz.chener.jms.repositorys

import xyz.chener.jms.core.smtp.entity.UserEmail

class NoopMailRepositoryImpl : MailRepository {
    override fun save(mailList:List<UserEmail>) {

    }

    override fun updateMailSendState(uuid: String, success: Boolean, errorMsg: String?) {

    }

    override fun getEmailCountAndSize(username: String, id: Int?): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun getEmailUidList(username: String, id: Int?): List<Pair<Int, String>> {
        return emptyList()
    }

    override fun getEmailIdAndSizeList(username: String, id: Int?): List<Pair<Int, Int>> {
        return emptyList()
    }

    override fun deleteEmail(username: String, id: List<Int>) {

    }

    override fun getEmailById(username: String, id: Int): UserEmail? {
        return null
    }

    override fun getEmailTopByIndex(username: String, index: Int, lines: Int): Pair<Int?,String?>? {
        return null
    }
}