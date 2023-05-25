##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# convert to pkcs8                                                               #
#                                                                                #
##################################################################################

app_name=youappname
app=/usr/local/yourapplocation/${app_name}
app_openssl_dir=${app}/openssl

client_key=${app_openssl_dir}/client/${app_name}_client.key
client_pkcs8_key=${app_openssl_dir}/client/${app_name}_pkcs8_rsa_client.key

openssl pkcs8 -topk8 -in ${client_key} -out ${client_pkcs8_key} -nocrypt