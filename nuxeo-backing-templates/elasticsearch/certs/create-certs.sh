#!/bin/bash

set -o nounset \
    -o errexit \


echo
echo "Welcome to the Searchguard SSL keystore and trusttore for Openshift generator script."
echo
echo "This script generates a ready to use certs.yaml file that contains every secret"
echo "needed in order to run an Elasticsearch cluster with Searguard"
echo
echo "First you need to answer several questions, to setup the certificates."
echo

echo -n "Give the expected ES admin password [] "
read SEARCHGUARD_ADMIN_PASSWORD
if [ -z $SEARCHGUARD_ADMIN_PASSWORD ]; then 
	echo "You must provide an admin password";
	echo "Promised, it's the only mandatory parameter!";
	exit 1
fi

echo -n "Give a Common Name for the key [searchguard.elasticsearch.svc] "
read CN
if [ -z $CN ]; then CN=searchguard.elasticsearch.svc; fi

echo -n "Give the Organizational Unit OU of the key [engineering] "
read OU
if [ -z $OU ]; then OU=engineering; fi


echo -n "Give the Organization of the key [nuxeo] "
read O
if [ -z $O ]; then O=nuxeo; fi


echo -n "Give the Location for the key [Irvine] "
read L
if [ -z $L ]; then L=irvine; fi

echo -n "Give the State for the key [ca] "
read S
if [ -z $S ]; then S=ca; fi

echo -n "Give the Country for the key [engineering] "
read C
if [ -z $C ]; then C=us; fi

echo -n "Give the number of days for the validity of the certificate [365] "
read DAYS
if [ -z $DAYS ]; then DAYS=365; fi


echo -n "Provide the password for the key [password] "
read KEY_CREDENTIAL
if [ -z $KEY_CREDENTIAL ]; then KEY_CREDENTIAL=password; fi

echo -n "Provide the password for the keystore [password] "
read KEYSTORE_CREDENTIAL
if [ -z $KEYSTORE_CREDENTIAL ]; then KEYSTORE_CREDENTIAL=password; fi

echo -n "Provide the password for the trust store [password] "
read TRUSTSTORE_CREDENTIAL
if [ -z $TRUSTSTORE_CREDENTIAL ]; then TRUSTSTORE_CREDENTIAL=password; fi



KEYSTORE_CREDENTIAL_B64=`echo -n $KEYSTORE_CREDENTIAL|base64`
KEY_CREDENTIAL_B64=`echo -n $KEY_CREDENTIAL|base64`
TRUSTSTORE_CREDENTIAL_B64=`echo -n $TRUSTSTORE_CREDENTIAL|base64`

out=certs.yaml



cat <<EOT > $out
apiVersion: v1
kind: Secret
metadata:
  name: nuxeo-backings-elasticsearch-keystore
stringData:
  authcz.admin_dn: CN=${CN}, OU=${OU}, O=${O}, L=${L}, S=${S}, C=${C}
data:
  keystore_credential: ${KEYSTORE_CREDENTIAL_B64}
  key_credential: ${KEY_CREDENTIAL_B64}
  truststore_credential: ${TRUSTSTORE_CREDENTIAL_B64}  

EOT


# Generate CA key
openssl req -new -x509 -keyout ca.key -out ca.crt -days $DAYS -subj "/CN=${CN}/OU=${OU}/O=${O}/L=${L}/S=${S}/C=${C}" -passin pass:${KEY_CREDENTIAL} -passout pass:${KEY_CREDENTIAL}

# Create truststore and import the CA cert.
keytool -keystore searchguard-truststore.p12 -deststoretype pkcs12 -noprompt -alias CARoot -import -file ca.crt -storepass ${TRUSTSTORE_CREDENTIAL} -keypass ${KEY_CREDENTIAL}

echo "  searchguard-truststore.p12: |" >> $out
base64 -b64 searchguard-truststore.p12|sed -e "s/^/       /" >> $out

for i in client-0 client-1 data-0 data-1 data-2 master-0 master-1 master-2
do
	echo $i
	# Create keystores
	keytool -genkey -noprompt \
				 -alias $i \
				 -ext san=dns:$i.searchguard.elasticsearch.svc,dns:localhost,ip:127.0.0.1,oid:1.2.3.4.5.5 \
				 -dname "CN=$i.${CN},OU=${OU},O=${O},L=${L},S=${S},C=${C}" \
				 -keystore searchguard.$i.keystore.p12 \
				 -keyalg RSA \
				 -deststoretype pkcs12 \
				 -storepass ${KEYSTORE_CREDENTIAL} \
				 -keypass ${KEY_CREDENTIAL}


	# Create CSR, sign the key and import back into keystore
	keytool -keystore searchguard.$i.keystore.p12 \
	           -alias $i \
	           -certreq \
	           -file $i.csr \
	           -deststoretype pkcs12 \
	           -storepass ${KEYSTORE_CREDENTIAL} \
	           -keypass ${KEY_CREDENTIAL} \
	           -ext san=dns:$i.searchguard.elasticsearch.svc,dns:localhost,ip:127.0.0.1,oid:1.2.3.4.5.5


	openssl x509 -req -CA ca.crt -CAkey ca.key -in $i.csr -out $i-ca1-signed.crt -days 9999 -CAcreateserial -passin pass:${KEY_CREDENTIAL} -extensions server_ext

	keytool -keystore searchguard.$i.keystore.p12 -deststoretype pkcs12  --noprompt -alias CARoot -import -file ca.crt -storepass ${KEYSTORE_CREDENTIAL} -keypass ${KEY_CREDENTIAL}

	keytool -keystore searchguard.$i.keystore.p12 --noprompt \
		-alias $i \
		-deststoretype pkcs12 \
		-import \
		-file $i-ca1-signed.crt \
		-storepass ${KEYSTORE_CREDENTIAL} \
		-keypass ${KEY_CREDENTIAL} \
		-ext san=dns:$i.svc.local,dns:localhost,ip:127.0.0.1,oid:1.2.3.4.5.5

	echo "  searchguard.$i.keystore.p12: |" >> $out
	base64 -b64 searchguard.$i.keystore.p12|sed -e "s/^/       /" >> $out
	
done



for i in admin elastic
do
	echo $i
	# Create keystores
	keytool -genkey -noprompt \
				 -alias $i \
				 -ext san=dns:$i.searchguard.elasticsearch.svc,dns:localhost,ip:127.0.0.1,oid:1.2.3.4.5.5 \
				 -dname "CN=${i},O=nuxeo,C=com" \
				 -keystore client.$i.keystore.p12 \
				 -keyalg RSA \
				 -deststoretype pkcs12 \
				 -storepass ${KEYSTORE_CREDENTIAL} \
				 -keypass ${KEY_CREDENTIAL}


	# Create CSR, sign the key and import back into keystore
	keytool -keystore client.$i.keystore.p12 \
	           -alias $i \
	           -certreq \
	           -file $i.csr \
	           -deststoretype pkcs12 \
	           -storepass ${KEYSTORE_CREDENTIAL} \
	           -keypass ${KEY_CREDENTIAL} \
	           -ext san=dns:$i.searchguard.elasticsearch.svc,dns:localhost,ip:127.0.0.1,oid:1.2.3.4.5.5


	openssl x509 -req -CA ca.crt -CAkey ca.key -in $i.csr -out $i-ca1-signed.crt -days 9999 -CAcreateserial -passin pass:${KEY_CREDENTIAL} -extensions server_ext

	keytool -keystore client.$i.keystore.p12 -deststoretype pkcs12  --noprompt -alias CARoot -import -file ca.crt -storepass ${KEYSTORE_CREDENTIAL} -keypass ${KEY_CREDENTIAL}

	keytool -keystore client.$i.keystore.p12 --noprompt \
		-alias $i \
		-deststoretype pkcs12 \
		-import \
		-file $i-ca1-signed.crt \
		-storepass ${KEYSTORE_CREDENTIAL} \
		-keypass ${KEY_CREDENTIAL} \
		-ext san=dns:$i.svc.local,dns:localhost,ip:127.0.0.1,oid:1.2.3.4.5.5

	echo "  client.$i.keystore.p12: |" >> $out
	base64 -b64 client.$i.keystore.p12|sed -e "s/^/       /" >> $out
	
done



rm -f *.crt
rm -f *.key
rm -f *.srl
rm -f *.csr
rm -f *.jks
rm -f *.p12
rm -f *.b64


