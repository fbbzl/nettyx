##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

openssl_ca=/etc/pki/CA

#dir for openssl
# issued certificate
mkdir -p ${openssl_ca}/certs
# ca new certificate
mkdir -p ${openssl_ca}/newcerts
# private key
mkdir -p ${openssl_ca}/private
# revoked certificate
mkdir -p ${openssl_ca}/crl
# penSSl defined issued certificate db
touch ${openssl_ca}/index.txt
# certificate serial number
echo 01 > ${openssl_ca}/serial

#dir for s5
openssl_dir=/usr/local/gdi/s5/openssl

root_dir=${openssl_dir}/root
client_dir=${openssl_dir}/client
server_dir=${openssl_dir}/server

mkdir -p ${root_dir}
mkdir -p ${client_dir}
mkdir -p ${server_dir}