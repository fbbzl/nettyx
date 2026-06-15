##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# Ssl implementation                                                             #
#                                                                                #
##################################################################################

Refers:
  script refer: https://my.oschina.net/u/176493/blog/688541
  openssl command refer : https://www.cnblogs.com/aixiaoxiaoyu/p/8650180.html

******************************************************************************

Follow these steps to perform the operation

1. Edit the openssl_env.sh file, overwrite the variable values in it according to your business needs, such as jar path, certificate storage path, etc
2. Copy nettyx-tools-openssl direct to the server
3. install openssl. If it is not installed, run the yum install openssl command to install it
4. Execute the openssl_auto-. sh script and then follow the console prompts to enter the information required for openssl. You will get the following file

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

The above files are required before you implement openssl