##################################################################################
#                                                                                #
# author: fbb                                                                    #
# since: 2022/3/07                                                               #
# all run in auto                                                                #
#                                                                                #
##################################################################################

os_openssl_ca=/etc/pki/CA
work_dir=.

#move cnf-temp to target
mkdir -p ${os_openssl_ca}
mv -f ${work_dir}/cnf/openssl.cnf ${os_openssl_ca}/

#init openssl env-dir
sh ${work_dir}/openssl_init_dir.sh

#init ca
sh ${work_dir}/ca/openssl_init_ca_root.sh

#init server
sh ${work_dir}/server/openssl_server_init.sh
sh ${work_dir}/server/openssl_server_do_sign.sh
sh ${work_dir}/server/openssl_server_pkcs8_key.sh

#init client
sh ${work_dir}/client/openssl_client_init.sh
sh ${work_dir}/client/openssl_client_do_sign.sh
sh ${work_dir}/client/openssl_client_pkcs8_key.sh

