#
# USE IN DEV
#

#genarate server RSA keypair and key-store
#the key-store is use to store the cer of the client

project=fz
keysize=2048
validity=3650
keyalg=RSA
keypass=123456
keystore_path=/${project}/ssl/server/${project}.jks
storepass=123456
cer_path=/${project}/ssl/server/${project}.cer

keytool -genkey -alias securechat -keysize ${keysize} -validity ${validity} -keyalg ${keyalg} -dname "CN=localhost" -keypass ${keypass} -storepass ${storepass} -keystore ${keystore_path}

#generate server self-signature cer
keytool -export -alias securechat -keystore ${keystore_path} -storepass ${storepass} -file ${cer_path}