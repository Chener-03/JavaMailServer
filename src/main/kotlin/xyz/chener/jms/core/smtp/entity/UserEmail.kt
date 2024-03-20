package xyz.chener.jms.core.smtp.entity

import java.util.*

data class UserEmail(
    // id
    var id:Int?,
    // uuid
    var uuid:String,
    // 发件人
    var from:String,
    // 收件人
    var to:String,
    // 内容
    var content:String?,
    // 主题
    var subject:String? = null,
    // 用户
    var username:String?,
    // 创建时间
    var createTime:Date?,
    // 读取时间
    var readTime:Date? = null,
    // 类型  发件 收件 草稿 垃圾
    var type:Int?,
    // 状态  发件（成功 失败）    收件/垃圾（已读 未读）
    var state:Int?,
    // 逻辑删除
    var isDel:Boolean
)


enum class EmailType(val code:Int){
    // 发件
    SEND(1),
    // 收件
    RECEIVE(2),
    // 草稿
    DRAFT(3),
    // 垃圾
    TRASH(4)
}

enum class EmailState(val code:Int){
    // 发送成功
    SEND_SUCCESS(1),
    // 发送失败
    SEND_FAILED(2),
    // 已读
    READ(3),
    // 未读
    UNREAD(4)
}