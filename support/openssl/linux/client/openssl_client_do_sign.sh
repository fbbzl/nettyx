##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

openssl_dir=/usr/local/gdi/s5/openssl

openssl_cnf=/etc/pki/CA/openssl_s5.cnf

ca_root_key=${openssl_dir}/root/ca_root.key
ca_root_crt=${openssl_dir}/root/ca_root.crt

client_csr=${openssl_dir}/client/s5_client.csr
client_crt=${openssl_dir}/client/s5_client.crt

openssl ca -in ${client_csr} -out ${client_crt} -cert ${ca_root_crt} -keyfile ${ca_root_key} -config ${openssl_cnf}