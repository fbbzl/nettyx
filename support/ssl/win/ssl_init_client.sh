#
# ONLY USE IN DEV
#

#genarate client RSA keypair and key-store
#the key-store is use to store the cer of the server

project=fz
keysize=2048
validity=3650
keyalg=RSA
keypass=123456
keystore_path=/${project}/ssl/client/${project}.jks
storepass=123456
cer_path=/${project}/ssl/client/${project}.cer


keytool -genkey -alias smcc -keysize ${keysize} -validity ${validity} -keyalg ${keyalg} -dname "CN=localhost" -keypass ${keypass} -storepass ${storepass} -keystore ${keystore_path}

#generate client self-signature cer
keytool -export -alias smcc -keystore ${keystore_path} -storepass ${storepass} -file ${cer_path}
