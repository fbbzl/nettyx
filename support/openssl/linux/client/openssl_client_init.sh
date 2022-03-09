##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# generate key for client                                                        #
#                                                                                #
##################################################################################

openssl_dir=/usr/local/gdi/s5/openssl

openssl_cnf=/etc/pki/CA/openssl_s5.cnf

client_key=${openssl_dir}/client/s5_client.key
client_csr=${openssl_dir}/client/s5_client.csr

key_pass=123456
key_length=2048

openssl genrsa -des3 -passout pass:${key_pass} -out ${client_key} ${key_length}

openssl req -new -key ${client_key} -out ${client_csr} -config ${openssl_cnf}