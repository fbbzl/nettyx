##################################################################################
# author: fengbinbin                                                             #                                                                               #
# since: 2022/1/12                                                               #                                                                                  #
# the script of the client ssl                                                   #                                                                                                                 #
#                                                                                #                                  #
##################################################################################

project=fz
keysize=2048
validity=3650
keyalg=RSA
keypass=dfjhg45
keystore_path=/usr/ssl/client/${project}.jks
storepass=dfjhg45
cer_path=/usr/ssl/client/${project}.cer

#genarate client RSA keypair and key-store
#the key-store is use to store the cer of the server
keytool -genkey -alias smcc -keysize ${keysize} -validity ${validity} -keyalg ${keyalg} -dname "CN=localhost" -keypass ${keypass} -storepass ${storepass} -keystore ${keystore_path}

#generate client self-signature cer
keytool -export -alias smcc -keystore ${keystore_path} -storepass ${storepass} -file ${cer_path}