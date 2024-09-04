##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

# shellcheck disable=SC2154
client_crt=${client_dir}/${app_name}_client.crt
client_csr=${client_dir}/${app_name}_client.csr

# shellcheck disable=SC2154
openssl ca -in "${client_csr}" -out "${client_crt}" -cert "${ca_root_crt}" -keyfile "${ca_root_key}" -config "${os_openssl_ca_cnf}"