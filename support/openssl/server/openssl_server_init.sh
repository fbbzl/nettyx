##################################################################################
#                                                                                #
# author: fbb                                                                    #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA
os_openssl_cnf=${os_openssl_ca}/openssl.cnf

app_name=xxx
app=/usr/local/app/${app_name}
app_openssl_dir=${app}/openssl

server_key=${app_openssl_dir}/server/${app_name}_server.key
server_csr=${app_openssl_dir}/server/${app_name}_server.csr

key_pass=123456
key_length=2048

openssl genrsa -des3 -passout pass:${key_pass} -out ${server_key} ${key_length}

openssl req -new -key ${server_key} -out ${server_csr} -config ${os_openssl_cnf}
