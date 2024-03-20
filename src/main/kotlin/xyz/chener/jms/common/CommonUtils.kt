package xyz.chener.jms.common

import org.xbill.DNS.Lookup
import org.xbill.DNS.MXRecord
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import xyz.chener.jms.core.smtp.entity.EmailAddress
import xyz.chener.jms.core.smtp.entity.MxRecord

class CommonUtils {
    companion object {

        const val DNS_SERVER = "223.5.5.5"

        fun getMx(domain:String) :List<MxRecord> {
            try {
                val resolver = SimpleResolver(DNS_SERVER)
                resolver.port = 53;
                val lookup = Lookup(domain, Type.MX)
                lookup.setResolver(resolver)
                lookup.run()
                if (lookup.result == Lookup.SUCCESSFUL) {
                    val res = ArrayList<MxRecord>()
                    lookup.answers.forEach {
                        if (it is MXRecord){
                            res.add(MxRecord(it.target.toString(),it.priority))
                        }
                    }
                    return res
                }
            }catch (_:Throwable){}
            return emptyList()
        }

        fun parseEmailAddr(addr:String) : EmailAddress? {
            try {
                val indexOf = addr.indexOf("@")
                val username = addr.substring(0,indexOf)
                val domain = addr.substring(indexOf+1)
                if (username.isNotEmpty() && domain.isNotEmpty()){
                    return EmailAddress(username, domain)
                }
            }catch (_:Exception){}
            return null
        }

        fun checkDomain(domain:String):Boolean{
            return domain.matches(Regex("^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2})?\$"))
        }

        fun AssertState(condition: Boolean, message: String) {
            if (!condition) {
                throw IllegalStateException(message)
            }
        }

        fun uuid():String{
            return java.util.UUID.randomUUID().toString().replace("-","")
        }

    }
}