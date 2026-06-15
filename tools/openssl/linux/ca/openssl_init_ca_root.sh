##################################################################################
#                                                                                #
# author: fengbinbin                                                             #
# since: 2022/3/07                                                               #
# the script of init the openssl ca root                                         #
#                                                                                #
##################################################################################

# shellcheck disable=SC2154
ca_root_key=${ca_root_dir}/ca_root.key
ca_root_crt=${ca_root_dir}/ca_root.crt

# shellcheck disable=SC2154
openssl req -new -x509 -keyout "${ca_root_key}" -out "${ca_root_crt}" -config "${os_openssl_ca_cnf}" -days "${root_key_valid_days}"
