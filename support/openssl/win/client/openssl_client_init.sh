##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# generate key for client                                                        #
#                                                                                #
##################################################################################

openssl genrsa -des3 -passout pass:123456 -out /s5/openssl/client/s5_client.key 2048

openssl req -new -key /s5/openssl/client/s5_client.key -out /s5/openssl/client/s5_client.csr