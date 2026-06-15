##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# generate key for client                                                        #
#                                                                                #
##################################################################################

# shellcheck disable=SC2154
client_key=${client_dir}/${app_name}_client.key
client_csr=${client_dir}/${app_name}_client.csr

openssl genrsa -des3 -passout pass:"${client_key_pass}" -out "${client_key}" "${client_key_length}"

# shellcheck disable=SC2154
openssl req -new -key "${client_key}" -out "${client_csr}" -config "${os_openssl_ca_cnf}" -days "${client_key_valid_days}"