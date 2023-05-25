##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# generate key for client                                                        #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA
os_openssl_ca_cnf=${os_openssl_ca}/openssl.cnf

app_name=youappname
app=/usr/local/gdi/${app_name}
app_openssl_dir=${app}/openssl

client_key=${app_openssl_dir}/client/${app_name}_client.key
client_csr=${app_openssl_dir}/client/${app_name}_client.csr

key_pass=Aqqaazz123!
key_length=2048
#100 years
valid_days=36500

openssl genrsa -des3 -passout pass:${key_pass} -out ${client_key} ${key_length}

openssl req -new -key ${client_key} -out ${client_csr} -config ${os_openssl_ca_cnf} -days ${valid_days}