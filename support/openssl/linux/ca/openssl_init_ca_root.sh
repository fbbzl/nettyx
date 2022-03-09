##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# the script of init the openssl ca root                                         #
#                                                                                #
##################################################################################

openssl_dir=/usr/local/gdi/s5/openssl

openssl_cnf=/etc/pki/CA/openssl_s5.cnf

ca_root_key=${openssl_dir}/root/ca_root.key
ca_root_crt=${openssl_dir}/root/ca_root.crt
#20 years
valid_days=7300

openssl req -new -x509 -keyout ${ca_root_key} -out ${ca_root_crt} -config ${openssl_cnf} -days ${valid_days}
