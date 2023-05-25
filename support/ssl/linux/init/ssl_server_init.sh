##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the server ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

app_name=youappname
app=/usr/local/gdi/${app_name}

key_alias=securechat
server_path=${app}/ssl/server
keysize=2048
validity=3650
keyalg=RSA
keypass=asdfgh
keystore_path=${server_path}/s5_server.jks
storepass=asdfgh
cer_path=${server_path}/s5_server.cer

mkdir -p ${server_path}

#genarate server RSA keypair and key-store
#the key-store is use to store the cer of the client
keytool -genkey -alias ${key_alias} -keysize ${keysize} -validity ${validity} -keyalg ${keyalg} -dname "CN=localhost" -keypass ${keypass} -storepass ${storepass} -keystore ${keystore_path}

#generate server self-signature cer
keytool -export -alias ${key_alias} -keystore ${keystore_path} -storepass ${storepass} -file ${cer_path}