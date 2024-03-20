package xyz.chener.jms.ss

import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.logging.stdout.StdOutImpl
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.transaction.TransactionFactory
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Proxy
import java.net.JarURLConnection
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionUtils private constructor(){

    companion object{
        val instance: SessionUtils by lazy { SessionUtils() }
    }

    val sqlSessionFactory: SqlSessionFactory

    private val mappers: ConcurrentHashMap<Class<*>, Any?> = ConcurrentHashMap()

    init {
        Assert.state(StringUtils.hasText(System.getProperty("jdbcUrl")),"Jvm param jdbcUrl is null")
        Assert.state(StringUtils.hasText(System.getProperty("username")),"Jvm param username is null")
        Assert.state(StringUtils.hasText(System.getProperty("password")),"Jvm param password is null")

        // datasource
        val datasourceConfig = HikariConfig()
        datasourceConfig.jdbcUrl = System.getProperty("jdbcUrl")
        datasourceConfig.driverClassName = "com.mysql.cj.jdbc.Driver"
        datasourceConfig.username = System.getProperty("username")
        datasourceConfig.password = System.getProperty("password")
        datasourceConfig.maximumPoolSize = 5
        datasourceConfig.connectionTimeout = 10000
        val datasource = HikariDataSource(datasourceConfig)

        // mybatis plus
        val mbpConfig = MybatisConfiguration()
        mbpConfig.isMapUnderscoreToCamelCase = true
        mbpConfig.isUseGeneratedKeys = true
//        mbpConfig.logImpl = StdOutImpl::class.java
        val globalConfig = GlobalConfigUtils.getGlobalConfig(mbpConfig)
        globalConfig.setSqlInjector(DefaultSqlInjector())
        globalConfig.setIdentifierGenerator(DefaultIdentifierGenerator.getInstance())
        globalConfig.setSuperMapperClass(BaseMapper::class.java)
        this.registryMapperXml(mbpConfig, "mapper/")
        mbpConfig.addMappers("xyz.chener.jms.ss.mapper")

        val transactionFactory: TransactionFactory = JdbcTransactionFactory()
        val environment = Environment("dev", transactionFactory, datasource)
        mbpConfig.environment = environment
        sqlSessionFactory = MybatisSqlSessionFactoryBuilder().build(mbpConfig)

    }


    fun <T> getMapper(clazz: Class<T>): T {
        if (mappers[clazz] != null){
            return mappers[clazz] as T
        }else{
            val mapper = createMapperProxy(clazz)
            mappers[clazz] = mapper
            return mapper
        }
    }


    private fun <T> createMapperProxy(clazz: Class<T>): T {
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)){ proxy, method, args ->
            sqlSessionFactory.openSession().use { session ->
                val sourceObj = session.getMapper(clazz)
                var result: Any? = null
                try {
                    result = method.invoke(sourceObj, *args)
                    session.commit()
                }catch (ex:Throwable){
                    session.rollback()
                    throw ex
                }
                return@use result
            }
        } as T
    }


    @Throws(IOException::class)
    private fun registryMapperXml(configuration: MybatisConfiguration, classPath: String) {
        val contextClassLoader = Thread.currentThread().contextClassLoader
        val mapper = contextClassLoader.getResources(classPath)
        while (mapper.hasMoreElements()) {
            val url = mapper.nextElement()
            if (url.protocol == "file") {
                val path = url.path
                val file = File(path)
                val files = file.listFiles()
                if (files != null) {
                    for (f in files) {
                        if (f.name.endsWith(".xml")) {
                            val `in` = FileInputStream(f)
                            val xmlMapperBuilder =
                                XMLMapperBuilder(`in`, configuration, f.path, configuration.sqlFragments)
                            xmlMapperBuilder.parse()
                            `in`.close()
                        }
                    }
                }
            } else {
                val urlConnection = url.openConnection() as JarURLConnection
                val jarFile = urlConnection.jarFile
                val entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    val jarEntry = entries.nextElement()
                    if (jarEntry.name.endsWith(".xml") && jarEntry.name.startsWith(classPath)) {
                        val `in` = jarFile.getInputStream(jarEntry)
                        val xmlMapperBuilder =
                            XMLMapperBuilder(`in`, configuration, jarEntry.name, configuration.sqlFragments)
                        xmlMapperBuilder.parse()
                        `in`.close()
                    }
                }
            }
        }
    }

}