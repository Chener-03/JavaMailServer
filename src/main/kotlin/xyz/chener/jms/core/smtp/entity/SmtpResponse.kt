package xyz.chener.jms.core.smtp.entity


data class SmtpResponse(
    val status:SmtpResponseStatus
    ,val kickClient:Boolean
    ,val message:String = ""
    ,val perMessage:String? = null
    ,val postMessage:String? = null
)
