#!/bin/bash

set -o nounset \
    -o errexit \


echo
echo "Welcome to the Kafka SSL keystore and trusttore for Openshift generator script."
echo
echo "This script generates a ready to use certs.yaml file that contains every secret"
echo "needed in order to run a Kafak cluster with SASL?"
echo
echo "First you need to answer several questions, to setup the certificates."
echo

echo -n "Give the expected Kafka admin password [] "
read KAFKA_ADMIN_PASSWORD
if [ -z $KAFKA_ADMIN_PASSWORD ]; then 
	echo "You must provide an admin password for the PLAIN authentication";
	echo "Promised, it's the only mandatory parameter!";
	exit 1
fi

echo -n "Give a Common Name for the key [nuxeo-backings.kafka.svc] "
read CN
if [ -z $CN ]; then CN=nuxeo-backings.kafka.svc; fi

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



KEYSTORE_CREDENTIAL_B64=`echo $KEYSTORE_CREDENTIAL|base64`
KEY_CREDENTIAL_B64=`echo $KEY_CREDENTIAL|base64`
TRUSTSTORE_CREDENTIAL_B64=`echo $TRUSTSTORE_CREDENTIAL|base64`

out=certs.yaml



cat <<EOT > $out
apiVersion: v1
kind: Secret
metadata:
  name: nuxeo-backings-kafka-keystore
data:
  keystore_credential: ${KEYSTORE_CREDENTIAL_B64}
  key_credential: ${KEY_CREDENTIAL_B64}
  truststore_credential: ${TRUSTSTORE_CREDENTIAL_B64}  

EOT


echo "  jaas.conf: |" >> $out
cat <<EOT | base64 -b64 |sed -e "s/^/       /" >> $out
KafkaServer {
   org.apache.kafka.common.security.plain.PlainLoginModule required
   username="admin"
   password="${KAFKA_ADMIN_PASSWORD}"
   user_admin="${KAFKA_ADMIN_PASSWORD}"
   user_kafkabroker1="kafkabroker1-secret";
};
EOT

# Generate CA key
openssl req -new -x509 -keyout ca.key -out ca.crt -days DAYS -subj "/CN=${CN}/OU=${OU}/O=${O}/L=${L}/S=${S}/C=${C}" -passin pass:${KEY_CREDENTIAL} -passout pass:${KEY_CREDENTIAL}

# Create truststore and import the CA cert.
keytool -keystore kafka.truststore.jks -alias CARoot -import -file ca.crt -storepass ${TRUSTSTORE_CREDENTIAL} -keypass ${KEY_CREDENTIAL}

echo "  kafka.truststore.jks: |" >> $out
base64 -b64 kafka.truststore.jks|sed -e "s/^/       /" >> $out

for i in broker0 broker1 broker2
do
	echo $i
	# Create keystores
	keytool -genkey -noprompt \
				 -alias $i \
				 -dname "CN=$i.${CN}, OU=${OU}, O=${O}, L=${L}, S=${S}, C=${C}" \
				 -keystore kafka.$i.keystore.jks \
				 -keyalg RSA \
				 -storepass ${KEYSTORE_CREDENTIAL} \
				 -keypass ${KEY_CREDENTIAL}

	# Create CSR, sign the key and import back into keystore
	keytool -keystore kafka.$i.keystore.jks -alias $i -certreq -file $i.csr -storepass ${KEYSTORE_CREDENTIAL} -keypass ${KEY_CREDENTIAL}


	openssl x509 -req -CA ca.crt -CAkey ca.key -in $i.csr -out $i-ca1-signed.crt -days 9999 -CAcreateserial -passin pass:${KEY_CREDENTIAL}

	keytool -keystore kafka.$i.keystore.jks -alias CARoot -import -file ca.crt -storepass ${KEYSTORE_CREDENTIAL} -keypass ${KEY_CREDENTIAL}

	keytool -keystore kafka.$i.keystore.jks -alias $i -import -file $i-ca1-signed.crt -storepass ${KEYSTORE_CREDENTIAL} -keypass ${KEY_CREDENTIAL}

	echo "  kafka.$i.keystore.jks: |" >> $out
	base64 -b64 kafka.$i.keystore.jks|sed -e "s/^/       /" >> $out


	cat kafka.$i.keystore.jks|base64 > kafka.$i.keystore.jks.b64
done


rm -f *.crt
rm -f *.key
rm -f *.srl
rm -f *.csr
rm -f *.jks
rm -f *.b64


