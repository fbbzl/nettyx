##################################################################################
#                                                                                #
# author: 503280366                                                              #
# since: 2022/3/07                                                               #
# the script of init the openssl ca root                                         #
#                                                                                #
##################################################################################

ca_root_key=/s5/openssl/root/ca_root.key
ca_root_crt=/s5/openssl/root/ca_root.crt
#20 years
valid_days=7300

openssl req -new -x509 -keyout ${ca_root_key} -out ${ca_root_crt} -days ${valid_days}
