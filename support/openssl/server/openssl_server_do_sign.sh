##################################################################################
#                                                                                #
# author: fbb                                                                    #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA
os_openssl_ca_cnf=${os_openssl_ca}/openssl.cnf

app_name=xxx
app=/usr/local/app/${app_name}
app_openssl_dir=${app}/openssl

ca_root_key=${app_openssl_dir}/root/ca_root.key
ca_root_crt=${app_openssl_dir}/root/ca_root.crt

server_csr=${app_openssl_dir}/server/${app_name}_server.csr
server_crt=${app_openssl_dir}/server/${app_name}_server.crt

openssl ca -in ${server_csr} -out ${server_crt} -cert ${ca_root_crt} -keyfile ${ca_root_key} -config ${os_openssl_ca_cnf}