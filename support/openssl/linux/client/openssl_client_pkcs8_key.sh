##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# convert to pkcs8                                                               #
#                                                                                #
##################################################################################

openssl_dir=/usr/local/gdi/s5/openssl

client_key=${openssl_dir}/client/s5_client.key

client_pkcs8_key=${openssl_dir}/client/s5_pkcs8_rsa_client.key

openssl pkcs8 -topk8 -in ${client_key} -out ${client_pkcs8_key} -nocrypt