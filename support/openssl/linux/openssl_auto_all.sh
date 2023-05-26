##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# all run in auto                                                                #
#                                                                                #
##################################################################################

source ./openssl_env.sh

work_dir=.

#copy cnf-temp to target
# shellcheck disable=SC2154
mkdir -p "${os_openssl_ca}"
cp -f ${work_dir}/cnf/openssl.cnf "${os_openssl_ca}"/

#init openssl env-dir
echo '############################################'
echo '# Initializing OpenSSL Working Directories #'
echo '############################################'
source ${work_dir}/openssl_init_dir.sh

#init ca
echo '########################'
echo '# Initializing CA ROOT #'
echo '########################'
source ${work_dir}/ca/openssl_init_ca_root.sh

#init server
echo '#######################'
echo '# Initializing Server #'
echo '#######################'
source ${work_dir}/server/openssl_server_init.sh
source ${work_dir}/server/openssl_server_do_sign.sh
source ${work_dir}/server/openssl_server_pkcs8_key.sh

#init client
echo '#######################'
echo '# Initializing Client #'
echo '#######################'
source ${work_dir}/client/openssl_client_init.sh
source ${work_dir}/client/openssl_client_do_sign.sh
source ${work_dir}/client/openssl_client_pkcs8_key.sh

