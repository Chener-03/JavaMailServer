package xyz.chener.jms.core.imap.entity

class ImapResponse(
    val uid:String? = null,
    val success:Boolean?  = null,
    val message:String? = null,
    val content:String? = null,
    val postMessage:String? = null,
    val kickClient:Boolean,
    val source:String? = null,
    val doLast: (() -> Unit)? = null
) {
    fun buildEndString() : String{
        return "$uid ${if (success == true) "OK" else "BAD"} $message"
    }
}