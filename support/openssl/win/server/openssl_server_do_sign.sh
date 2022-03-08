##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

openssl ca -in /s5/openssl/server/s5_server.csr -out /s5/openssl/server/s5_server.crt -cert /s5/openssl/root/ca_root.crt -keyfile /s5/openssl/root/ca_root.key