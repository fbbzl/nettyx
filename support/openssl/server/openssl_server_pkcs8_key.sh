##################################################################################
#                                                                                #
# author: fbb                                                                    #
# since: 2022/3/07                                                               #
# convert to pkcs8                                                               #
#                                                                                #
##################################################################################

app_name=xxx
app=/usr/local/app/${app_name}
app_openssl_dir=${app}/openssl

server_key=${app_openssl_dir}/server/${app_name}_server.key

server_pkcs8_key=${app_openssl_dir}/server/${app_name}_pkcs8_rsa_server.key

openssl pkcs8 -topk8 -in ${server_key} -out ${server_pkcs8_key} -nocrypt