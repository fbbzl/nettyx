##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

openssl genrsa -des3 -passout pass:123456 -out /s5/openssl/server/s5_server.key 2048

openssl req -new -key /s5/openssl/server/s5_server.key -out /s5/openssl/server/s5_server.csr
