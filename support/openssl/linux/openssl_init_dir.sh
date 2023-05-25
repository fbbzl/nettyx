##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

#dir for openssl
# issued certificate
# shellcheck disable=SC2154
mkdir -p "${os_openssl_ca}"/certs
# ca new certificate
mkdir -p "${os_openssl_ca}"/newcerts
# private key
mkdir -p "${os_openssl_ca}"/private
# revoked certificate
mkdir -p "${os_openssl_ca}"/crl
# penSSl defined issued certificate db
touch "${os_openssl_ca}"/index.txt
# certificate serial number
echo 01 > "${os_openssl_ca}"/serial

mkdir -p "${ca_root_dir}"
mkdir -p "${client_dir}"
mkdir -p "${server_dir}"