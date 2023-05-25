##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# the script of init the openssl ca root                                         #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA
os_openssl_ca_cnf=${os_openssl_ca}/openssl.cnf

app_name=youappname
app=/usr/local/gdi/${app_name}
app_openssl_dir=${app}/openssl

ca_root_key=${app_openssl_dir}/root/ca_root.key
ca_root_crt=${app_openssl_dir}/root/ca_root.crt
#100 years
valid_days=36500

openssl req -new -x509 -keyout ${ca_root_key} -out ${ca_root_crt} -config ${os_openssl_ca_cnf} -days ${valid_days}
