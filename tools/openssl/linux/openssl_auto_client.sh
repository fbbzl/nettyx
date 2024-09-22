##################################################################################
#                                                                                #
# author: fbb                                                                    #
# since: 2022/3/14                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

work_dir=.

sh ${work_dir}/client/openssl_client_init.sh
sh ${work_dir}/client/openssl_client_do_sign.sh
sh ${work_dir}/client/openssl_client_pkcs8_key.sh