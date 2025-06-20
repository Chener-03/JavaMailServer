package xyz.chener.jms.ss.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.time.LocalDateTime


@TableName("emails")
open class Emails : Model<Emails>() {

    @TableId(type = IdType.AUTO)
    var id: Long? = null

    var username: String? = null

    @TableField("folder_id")
    var folderId: Int? = null

    var uid: String? = null

    var subject: String? = null

    @TableField("from_address")
    var fromAddress: String? = null

    var sender: String? = null

    @TableField("to_address")
    var toAddress: String? = null

    @TableField("cc_address")
    var ccAddress: String? = null

    @TableField("bcc_address")
    var bccAddress: String? = null

    @TableField("date_sent")
    var dateSent: LocalDateTime? = null

    var flags: String? = null

    @TableField("mime_version")
    var mimeVersion: String? = null

    @TableField("content_type")
    var contentType: String? = null

    @TableField("raw_headers")
    var rawHeaders: String? = null

    @TableField("plain_text")
    var plainText: String? = null

    @TableField("html_text")
    var htmlText: String? = null

    @TableField("has_attachment")
    var hasAttachment: Int? = null

    @TableField("created_at")
    var createdAt: LocalDateTime? = null

    @TableField("updated_at")
    var updatedAt: LocalDateTime? = null

}