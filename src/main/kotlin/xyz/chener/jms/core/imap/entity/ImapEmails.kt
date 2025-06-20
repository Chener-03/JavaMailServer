package xyz.chener.jms.core.imap.entity

import com.baomidou.mybatisplus.annotation.TableField
import java.time.LocalDateTime

open class ImapEmails {


    var id: Long? = null

    var username: String? = null

    var folderId: Int? = null

    var uid: String? = null

    var subject: String? = null

    var fromAddress: String? = null

    var sender: String? = null

    var toAddress: String? = null

    var ccAddress: String? = null

    var bccAddress: String? = null

    var dateSent: LocalDateTime? = null

    var flags: String? = null

    var mimeVersion: String? = null

    var contentType: String? = null

    var rawHeaders: String? = null

    var plainText: String? = null

    var htmlText: String? = null

    var hasAttachment: Int? = null

    var createdAt: LocalDateTime? = null

    var updatedAt: LocalDateTime? = null

}