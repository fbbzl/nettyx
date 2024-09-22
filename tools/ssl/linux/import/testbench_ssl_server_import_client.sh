##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the client ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

app_name=youappname
app=/usr/local/yourapplocation/${app_name}

importing_key_alias=smcc
client_cer_path=${app}/ssl/client/s5_client.cer
server_keystore_path=${app}/ssl/server/s5_server.jks
server_keystore_pass=asdfgh

keytool -import -trustcacerts -alias ${importing_key_alias} -file ${client_cer_path} -storepass ${server_keystore_pass} -keystore ${server_keystore_path}