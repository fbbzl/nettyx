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

Hi, Bro, just execute openssl_auto_all, and then type the information that openssl wanted, finally you'll got
the following files:

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