##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# convert to pkcs8                                                               #
#                                                                                #
##################################################################################

openssl_dir=/usr/local/gdi/s5/openssl

server_key=${openssl_dir}/server/s5_server.key

server_pkcs8_key=${openssl_dir}/server/s5_pkcs8_rsa_server.key

openssl pkcs8 -topk8 -in ${server_key} -out ${server_pkcs8_key} -nocrypt