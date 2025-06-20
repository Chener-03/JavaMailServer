package xyz.chener.jms.ss.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model


@TableName("email_folder")
open class Folder: Model<Folder>() {

    @TableId(type = IdType.AUTO)
    open var id: Int? = null

    open var folderPath: String? = null

    open var tag: String? = null

    @TableField("`default`")
    open var default: Boolean? = null

    open var username: String? = null
}