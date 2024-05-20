package xyz.chener.jms.common

import org.xbill.DNS.Lookup
import org.xbill.DNS.MXRecord
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import xyz.chener.jms.core.smtp.entity.EmailAddress
import xyz.chener.jms.core.smtp.entity.MxRecord
import java.lang.RuntimeException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.jvm.Throws

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

        fun decodeModifiedUTF7(str:String):String{
            val output = StringBuilder()
            var base64Mode = false
            val base64Buffer = StringBuilder()

            for (element in str) {
                val ch: Char = element
                if (ch == '&') {
                    base64Mode = true
                } else if (base64Mode && ch == '-') {
                    if (base64Buffer.isNotEmpty()) {
                        val base64Chunk = base64Buffer.toString().replace(',', '/')
                        val decodedBytes = Base64.getDecoder().decode(base64Chunk)
                        output.append(String(decodedBytes, StandardCharsets.UTF_16BE))
                        base64Buffer.setLength(0)
                    }
                    base64Mode = false
                } else if (base64Mode) {
                    base64Buffer.append(ch)
                } else {
                    output.append(ch)
                }
            }
            return output.toString()
        }

        @Throws(RuntimeException::class)
        fun encodeModifiedUTF7(str:String):String{
            val output = java.lang.StringBuilder()
            val base64Buffer = java.lang.StringBuilder()

            for (element in str) {
                val ch: Char = element
                if (ch.code in 0x20..0x7E) { // ASCII printable characters
                    if (base64Buffer.isNotEmpty()) {
                        output.append("&")
                        output.append(Base64.getEncoder().encodeToString(base64Buffer.toString().toByteArray(StandardCharsets.UTF_16BE)).replace("=",""))
                        output.append("-")
                        base64Buffer.clear()
                    }
                    if (ch == '&' || ch == '-'){
                        throw RuntimeException("Invalid character in modified UTF-7")
                    }
                    output.append(ch)
                } else {
                    base64Buffer.append(ch)
                }
            }

            if (base64Buffer.isNotEmpty()) {
                output.append("&")
                output.append(Base64.getEncoder().encodeToString(base64Buffer.toString().toByteArray(StandardCharsets.UTF_16BE)).replace("=",""))
                output.append("-")
            }

            return output.toString()
        }

    }
}