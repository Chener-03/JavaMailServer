package xyz.chener.jms.core.imap

class ImapResponse(
    val uid:String,
    val success:Boolean,
    val message:String,
    val content:String,
    val postMessage:String,
    val kickClient:Boolean,
    val source:String? = null,
    val doLast: (() -> Unit)? = null
) {
    fun buildEndString() : String{
        return "$uid ${if (success) "OK" else "BAD"} $message"
    }
}