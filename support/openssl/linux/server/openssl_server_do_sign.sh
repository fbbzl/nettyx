##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

ca_root_key=/s5/openssl/root/ca_root.key
ca_root_crt=/s5/openssl/root/ca_root.crt

server_csr=/s5/openssl/server/s5_server.csr
server_crt=/s5/openssl/server/s5_server.crt

openssl ca -in ${server_csr} -out ${server_crt} -cert ${ca_root_crt} -keyfile ${ca_root_key}