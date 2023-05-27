##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# Ssl implementation                                                             #
#                                                                                #
##################################################################################

参考文献:
  脚本参考连接: https://my.oschina.net/u/176493/blog/688541
  openssl指令参考连接: https://www.cnblogs.com/aixiaoxiaoyu/p/8650180.html

******************************************************************************

第一步 编辑openssl_env.sh文件, 根据自身业务需要覆盖其中的变量值, 如jar路径, 证书存放路径等等
第二步 将nettyx-support-openssl目录复制到服务器上
第三步 安装openssl, 如未安装, 可以使用yum install openssl进行安装
第三步 执行openssl_auto.sh脚本, 然后根据控制台的提示输入openssl所需要的信息, 你会得到如下的文件

CA ROOT:
ca_root.key
ca_root.crt

Client:
youappname_client.csr
youappname_client.crt
youappname_client.key
youappname_pkcs8_rsa_client.key

Server:
youappname_server.csr
youappname_server.crt
youappname_server.key
youappname_pkcs8_rsa_server.key

以上文件是在你实现openssl所必需的



