package xyz.chener.jms.core.pop3.entity

data class Pop3Response(
    val success:Boolean?,
    val msg:String?,
    val kickClient:Boolean,
    val source:String? = null
){
    fun getStatusText():String?{
        success?.let {
            return if(it) "+OK" else "-ERR"
        }
        return null
    }
}