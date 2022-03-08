##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# generate key for server                                                        #
#                                                                                #
##################################################################################

server_key=/s5/openssl/server/s5_server.key
server_csr=/s5/openssl/server/s5_server.csr

key_pass=123456
key_length=2048

openssl genrsa -des3 -passout pass:${key_pass} -out ${server_key} ${key_length}

openssl req -new -key ${server_key} -out ${server_csr}
