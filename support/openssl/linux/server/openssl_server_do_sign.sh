##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

server_crt=${app_openssl_dir}/server/${app_name}_server.crt
server_csr=${app_openssl_dir}/server/${app_name}_server.csr

# shellcheck disable=SC2154
openssl ca -in "${server_csr}" -out "${server_crt}" -cert "${ca_root_crt}" -keyfile "${ca_root_key}" -config "${os_openssl_ca_cnf}"