#
# ONLY USE IN DEV
#

project=fz
server_cer_path=/${project}/ssl/server/${project}.cer
client_keystore_path=/${project}/ssl/client/${project}.jks
client_keystore_pass=123456

keytool -import -trustcacerts -alias securechat -file ${server_cer_path} -storepass ${client_keystore_pass} -keystore ${client_keystore_path}