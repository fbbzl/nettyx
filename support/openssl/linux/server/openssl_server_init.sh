##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA
os_openssl_cnf=${os_openssl_ca}/openssl.cnf

app_name=youappname
app=/usr/local/gdi/${app_name}
app_openssl_dir=${app}/openssl

server_key=${app_openssl_dir}/server/${app_name}_server.key
server_csr=${app_openssl_dir}/server/${app_name}_server.csr

key_pass=Aqqaazz123!
key_length=2048
#100 years
valid_days=36500

openssl genrsa -des3 -passout pass:${key_pass} -out ${server_key} ${key_length}

openssl req -new -key ${server_key} -out ${server_csr} -config ${os_openssl_cnf} -days ${valid_days}
