##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA

#dir for openssl
# issued certificate
mkdir -p ${os_openssl_ca}/certs
# ca new certificate
mkdir -p ${os_openssl_ca}/newcerts
# private key
mkdir -p ${os_openssl_ca}/private
# revoked certificate
mkdir -p ${os_openssl_ca}/crl
# penSSl defined issued certificate db
touch ${os_openssl_ca}/index.txt
# certificate serial number
echo 01 > ${os_openssl_ca}/serial

#dir for app
app_name=youappname
app=/usr/local/gdi/${app_name}
app_openssl_dir=${app}/openssl

root_dir=${app_openssl_dir}/root
client_dir=${app_openssl_dir}/client
server_dir=${app_openssl_dir}/server

mkdir -p ${root_dir}
mkdir -p ${client_dir}
mkdir -p ${server_dir}