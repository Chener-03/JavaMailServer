package xyz.chener.jms.ss.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.extension.activerecord.Model
import xyz.chener.jms.core.smtp.entity.UserEmail
import java.util.*

open class EmailInfo : Model<EmailInfo>() {

    @TableId(type = IdType.AUTO)
    open var id: Int? = null

    open var uuid: String? = null

    @TableField("`from`")
    open var from: String? = null

    @TableField("`to`")
    open var to: String? = null

    open var content: String? = null

    open var subject: String? = null

    open var username: String? = null

    open var createTime: Date? = null

    open var readTime: Date? = null

    open var type: Int? = null

    open var state: Int? = null

    open var sendfailmsg: String? = null

    @TableLogic(value = "0", delval = "1")
    open var isdel: Int? = 0

    companion object{
        fun createByUserEmail(ue:UserEmail):EmailInfo{
            val ei = EmailInfo()
            ei.id = ue.id
            ei.uuid = ue.uuid
            ei.from = ue.from
            ei.to = ue.to
            ei.content = ue.content
            ei.subject = ue.subject
            ei.username = ue.username
            ei.createTime = ue.createTime
            ei.readTime = ue.readTime
            ei.type = ue.type
            ei.state = ue.state
            return ei
        }

        fun toUserEmail(ei:EmailInfo):UserEmail{
            val ue = UserEmail(
                ei.id
                ,if (ei.uuid == null) "" else ei.uuid!!
                ,if (ei.from == null) "" else ei.from!!
                ,if (ei.to == null) "" else ei.to!!
                ,ei.content
                ,ei.subject
                ,ei.username
                ,ei.createTime
                ,ei.readTime
                ,ei.type
                ,ei.state,false)
            return ue
        }

    }

}