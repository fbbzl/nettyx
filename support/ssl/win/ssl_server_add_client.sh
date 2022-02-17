#
# ONLY USE IN DEV
#

project=fz
client_cer_path=/${project}/ssl/client/${project}.cer
server_keystore_path=/${project}/ssl/server/${project}.jks
server_keystore_pass=123456

keytool -import -trustcacerts -alias smcc -file ${client_cer_path} -storepass ${server_keystore_pass} -keystore ${server_keystore_path}