##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# Ssl implementation                                                             #
#                                                                                #
##################################################################################

References:
  Script reference: https://my.oschina.net/u/176493/blog/688541
  OpenSSL command reference: https://www.cnblogs.com/aixiaoxiaoyu/p/8650180.html

******************************************************************************

Step 1: Edit openssl_env.sh and set the variables for your application, including the JAR path and certificate directory.
Step 2: Copy the nettyx-tools-openssl directory to the server.
Step 3: Install OpenSSL if needed, for example with yum install openssl.
Step 4: Run openssl_auto.sh and enter the OpenSSL information requested by the console prompts. The following files will be generated:

CA ROOT:
ca_root.key
ca_root.crt

Client:
youappname_client.csr
youappname_client.crt
youappname_client.key
youappname_pkcs8_rsa_client.key

Server:
youappname_server.csr
youappname_server.crt
youappname_server.key
youappname_pkcs8_rsa_server.key

These files are required for the OpenSSL setup.



