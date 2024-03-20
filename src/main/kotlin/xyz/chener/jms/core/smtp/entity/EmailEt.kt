package xyz.chener.jms.core.smtp.entity

data class MxRecord(var domain:String,var priority:Int)

data class EmailAddress(var username:String,var domain: String){
    fun toEmailAddress():String = "$username@$domain"
}