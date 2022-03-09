##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

openssl_dir=/usr/local/gdi/s5/openssl

openssl_cnf=/etc/pki/CA/openssl_s5.cnf

server_key=${openssl_dir}/server/s5_server.key
server_csr=${openssl_dir}/server/s5_server.csr

key_pass=123456
key_length=2048

openssl genrsa -des3 -passout pass:${key_pass} -out ${server_key} ${key_length}

openssl req -new -key ${server_key} -out ${server_csr} -config ${openssl_cnf}
