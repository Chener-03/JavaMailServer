package xyz.chener.jms.ss.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.util.Date

open class UserInfo : Model<UserInfo>() {

    @TableId(type = IdType.AUTO)
    open var id: Int? = null

    open var username: String? = null

    open var password: String? = null

    open var phone: String? = null

    open var createTime: Date? = null
}