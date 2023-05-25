##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the client ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

app_name=youappname
app=/usr/local/gdi/${app_name}

importing_key_alias=testbench
server_cer_path=${app}/ssl/server/s5_server.cer
client_keystore_path=${app}/ssl/client/s5_client.jks
client_keystore_pass=asdfgh

keytool -import -trustcacerts -alias ${importing_key_alias} -file ${server_cer_path} -storepass ${client_keystore_pass} -keystore ${client_keystore_path}