##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the client ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

project=fz
client_cer_path=/usr/ssl/client/${project}.cer
server_keystore_path=/usr/ssl/server/${project}.jks
server_keystore_pass=dfjhg45

keytool -import -trustcacerts -alias smcc -file ${client_cer_path} -storepass ${server_keystore_pass} -keystore ${server_keystore_path}