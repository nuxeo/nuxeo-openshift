#!/bin/bash

set -o nounset \
    -o errexit \


echo
echo "Welcome to the MongoDB SSL keystore and trusttore for Openshift generator script."
echo
echo "This script generates a ready to use certs.yaml file that contains every secret"
echo "needed in order to run a TLS MongoDB cluster."
echo
echo "First you need to answer several questions, to setup the certificates."
echo

echo -n "Give a Common Name for the key [mongodb.svc] "
read CN
if [ -z $CN ]; then CN=mongodb.svc; fi

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



out=certs.yaml

cat <<EOT > $out
apiVersion: v1
kind: Secret
metadata:
  name: nuxeo-backings-mongodb-certs
stringData:  
EOT


# Generate CA key
openssl req -newkey rsa:2048 -new -x509 -days 365 -nodes -out mongodb-cert.crt -keyout mongodb-cert.key -subj "/CN=${CN}/OU=${OU}/O=${O}/L=${L}/S=${S}/C=${C}"
# Create truststore and import the CA cert.


echo "  tls.key: |">> $out
cat mongodb-cert.key |sed -e "s/^/      /" >> $out
echo "  tls.crt: |">> $out
cat mongodb-cert.crt |sed -e "s/^/      /" >> $out


rm -f *.crt
rm -f *.key
