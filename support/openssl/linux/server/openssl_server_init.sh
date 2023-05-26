##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

# shellcheck disable=SC2154
server_key=${app_openssl_dir}/server/${app_name}_server.key
server_csr=${app_openssl_dir}/server/${app_name}_server.csr

key_pass=Aqqaazz123!@@
key_length=2048
#100 years
valid_days=36500

openssl genrsa -des3 -passout pass:${key_pass} -out "${server_key}" ${key_length}

# shellcheck disable=SC2154
openssl req -new -key "${server_key}" -out "${server_csr}" -config "${os_openssl_ca_cnf}" -days ${valid_days}
