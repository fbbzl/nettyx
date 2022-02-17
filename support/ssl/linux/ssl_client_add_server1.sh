##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the client ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

project=fz
server_cer_path=/usr/ssl/server/${project}.cer
client_keystore_path=/usr/ssl/client/${project}.jks
client_keystore_pass=dfjhg45

keytool -import -trustcacerts -alias securechat -file ${server_cer_path} -storepass ${client_keystore_pass} -keystore ${client_keystore_path}