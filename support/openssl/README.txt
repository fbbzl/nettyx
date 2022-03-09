##################################################################################
#                                                                                #
# author: fbb                                                                    #
# since: 2022/3/07                                                               #
# Ssl implementation                                                             #
#                                                                                #
##################################################################################

Refers:
  script refer: https://my.oschina.net/u/176493/blog/688541
  openssl command refer : https://www.cnblogs.com/aixiaoxiaoyu/p/8650180.html


execute the openssl_auto.sh OR execute command with the following steps:

OpenSslConf:
   1. find / -name openssl.cnf OR use ./cnf/openssl.cnf as template
   2. config this openssl.cnf by business

Dir:
   1. ./openssl_init_dir.sh

Ca:
   1. ca/openssl_init_ca_root.sh

Server:
   1. server/openssl_server_init.sh
   2. server/openssl_server_do_sign.sh
   3. server/openssl_server_pkcs8_key.sh

Client:
   1. client/openssl_client_init.sh
   2. client/openssl_client_do_sign.sh
   3. client/openssl_client_pkcs8_key.sh


