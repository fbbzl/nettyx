##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# do sign by root                                                                #
#                                                                                #
##################################################################################

ca_root_key=/s5/openssl/root/ca_root.key
ca_root_crt=/s5/openssl/root/ca_root.crt

client_csr=/s5/openssl/client/s5_client.csr
client_crt=/s5/openssl/client/s5_client.crt

openssl ca -in ${client_csr} -out ${client_crt} -cert ${ca_root_crt} -keyfile ${ca_root_key}