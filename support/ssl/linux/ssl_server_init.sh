##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the server ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

project=fz
keysize=2048
validity=3650
keyalg=RSA
keypass=dfjhg45
keystore_path=/usr/ssl/server/${project}.jks
storepass=dfjhg45
cer_path=/usr/ssl/server/${project}.cer

#genarate server RSA keypair and key-store
#the key-store is use to store the cer of the client
keytool -genkey -alias securechat -keysize ${keysize} -validity ${validity} -keyalg ${keyalg} -dname "CN=localhost" -keypass ${keypass} -storepass ${storepass} -keystore ${keystore_path}

#generate server self-signature cer
keytool -export -alias securechat -keystore ${keystore_path} -storepass ${storepass} -file ${cer_path}