##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA
os_openssl_ca_cnf=${os_openssl_ca}/openssl.cnf

app_name=youappname
app=/usr/local/yourapplocation/${app_name}
app_openssl_dir=${app}/openssl

ca_root_key=${app_openssl_dir}/root/ca_root.key
ca_root_crt=${app_openssl_dir}/root/ca_root.crt

client_csr=${app_openssl_dir}/client/${app_name}_client.csr
client_crt=${app_openssl_dir}/client/${app_name}_client.crt

openssl ca -in ${client_csr} -out ${client_crt} -cert ${ca_root_crt} -keyfile ${ca_root_key} -config ${os_openssl_ca_cnf}