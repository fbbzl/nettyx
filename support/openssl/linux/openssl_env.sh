##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 20223/5/27                                                              #
# all run in auto                                                                #
#                                                                                #
##################################################################################

#the Linux openssl config file
export os_openssl_ca=/etc/pki/CA
export os_openssl_ca_cnf=${os_openssl_ca}/openssl.cnf

#application config
export app_name=trash
export app=/usr/app/${app_name}
#the directory you stored all the openssl files, it's used when the application is running
export app_openssl_dir=${app}/openssl

#specifies the openssl CA-ROOT file generation location, use to sign client/server files
export ca_root_dir=${app_openssl_dir}/root
export ca_root_key=${ca_root_dir}/ca_root.key
export ca_root_crt=${ca_root_dir}/ca_root.crt

#specifies the openssl client file generation location
export client_dir=${app_openssl_dir}/client

#specifies the openssl server file generation location
export server_dir=${app_openssl_dir}/server

