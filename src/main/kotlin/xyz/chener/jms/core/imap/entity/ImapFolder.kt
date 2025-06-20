package xyz.chener.jms.core.imap.entity

data class ImapFolder(val path:String?,val tag: String?,val default: Boolean?,var fid:Int? = null)
