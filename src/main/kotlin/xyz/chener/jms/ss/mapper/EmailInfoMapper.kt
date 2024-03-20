package xyz.chener.jms.ss.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.apache.ibatis.annotations.Param
import xyz.chener.jms.ss.entity.EmailInfo

interface EmailInfoMapper : BaseMapper<EmailInfo> {

    fun getCountAndSize(@Param("username") username: String,@Param("id") id: Int?): Map<String,Any>

    fun getIdAndSize(@Param("username") username: String,@Param("id") id: Int?): List<Map<String,Any>>

}