##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# read to pkcs8                                                               #
#                                                                                #
##################################################################################

# shellcheck disable=SC2154
server_key=${app_openssl_dir}/server/${app_name}_server.key
server_pkcs8_key=${app_openssl_dir}/server/${app_name}_pkcs8_rsa_server.key

openssl pkcs8 -topk8 -in "${server_key}" -out "${server_pkcs8_key}" -nocrypt