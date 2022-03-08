##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# convert to pkcs8                                                               #
#                                                                                #
##################################################################################

openssl pkcs8 -topk8 -in /s5/openssl/client/s5_client.key -out /s5/openssl/client/s5_pkcs8_rsa_client.key -nocrypt