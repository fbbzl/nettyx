##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# the script of init the openssl ca root                                         #
#                                                                                #
##################################################################################

openssl req -new -x509 -keyout /s5/openssl/root/ca_root.key -out /s5/openssl/root/ca_root.crt -days 7300
