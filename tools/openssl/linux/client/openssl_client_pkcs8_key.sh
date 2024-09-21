##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# read to pkcs8                                                               #
#                                                                                #
##################################################################################

# shellcheck disable=SC2154
client_key=${client_dir}/${app_name}_client.key
client_pkcs8_key=${client_dir}/${app_name}_pkcs8_rsa_client.key

openssl pkcs8 -topk8 -in "${client_key}" -out "${client_pkcs8_key}" -nocrypt