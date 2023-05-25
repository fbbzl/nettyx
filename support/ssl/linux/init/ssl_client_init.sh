##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the client ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

app_name=youappname
app=/usr/local/gdi/${app_name}

key_alias=smcc
client_path=${app}/ssl/client
keysize=2048
validity=3650
keyalg=RSA
keypass=asdfgh
keystore_path=${client_path}/s5_client.jks
storepass=asdfgh
cer_path=${client_path}/s5_client.cer

mkdir -p ${client_path}

#genarate client RSA keypair and key-store
#the key-store is use to store the cer of the server
keytool -genkey -alias ${key_alias} -keysize ${keysize} -validity ${validity} -keyalg ${keyalg} -dname "CN=localhost" -keypass ${keypass} -storepass ${storepass} -keystore ${keystore_path}

#generate client self-signature cer
keytool -export -alias ${key_alias} -keystore ${keystore_path} -storepass ${storepass} -file ${cer_path}