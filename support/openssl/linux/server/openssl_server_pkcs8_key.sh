##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# convert to pkcs8                                                               #
#                                                                                #
##################################################################################

server_key=/s5/openssl/server/s5_server.key

server_pkcs8_key=/s5/openssl/server/s5_pkcs8_rsa_server.key

openssl pkcs8 -topk8 -in ${server_key} -out ${server_pkcs8_key} -nocrypt