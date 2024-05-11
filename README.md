# 极简邮件服务器 (POP3、SMTP)
#### *核心代码不到500行*
#### *IMAP 和 Web 端后续支持*
***
### 使用
```shell
默认提供了mysql做存储,可自己实现接口扩展 (删除xyz.chener.jms.ss包下内容即可)

默认运行参数
-DjdbcUrl="mysql地址" 
-Dusername="用户名" 
-Dpassword="密码" 
-Ddomain="邮箱域名"
-Dssl.cert="证书"
-Dssl.key="证书"

建表语句在 create_table.sql 中

配置域名mx记录网上有教程...
```