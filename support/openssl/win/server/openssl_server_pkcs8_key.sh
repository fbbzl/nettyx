##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# convert to pkcs8                                                               #
#                                                                                #
##################################################################################

openssl pkcs8 -topk8 -in /s5/openssl/server/s5_server.key -out /s5/openssl/server/s5_pkcs8_rsa_server.key -nocrypt