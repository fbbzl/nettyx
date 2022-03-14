##################################################################################
#                                                                                #
# author: fbb                                                                    #
# since: 2022/3/14                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

work_dir=.

sh ${work_dir}/server/openssl_server_init.sh
sh ${work_dir}/server/openssl_server_do_sign.sh
sh ${work_dir}/server/openssl_server_pkcs8_key.sh