#!/bin/bash

CERT_FILE=/app/certs/ca-cert.pem
TRUSTSTORE_FILE="/app/truststore/self-signed-truststore.jks"
TRUSTSTORE_PASS=${TRUSTSTORE_PASS:-changeit}
KEY_PASS=${KEY_PASS:-changeit}


if [[ -f "$CERT_FILE" ]]; then
    echo "Found certificate file ca-cert.pem - starting FLARE with own CA"
    if [[ ! -f "$TRUSTSTORE_FILE" ]]; then
        keytool -genkey -alias self-signed-truststore -keyalg RSA -keystore $TRUSTSTORE_FILE -storepass $TRUSTSTORE_PASS -keypass $KEY_PASS -dname "CN=self-signed,OU=self-signed,O=self-signed,L=self-signed,S=self-signed,C=TE"
    else
      echo "CA cert file found, but truststore already exists -> using existing truststore and deleting previously added ca cert"
      keytool -delete -alias self-signed-cert-ca -keystore $TRUSTSTORE_FILE -storepass $TRUSTSTORE_PASS -noprompt
    fi

    echo "Importing cert $CERT_FILE"
    keytool -importcert -alias self-signed-cert-ca -file $CERT_FILE -keystore $TRUSTSTORE_FILE -storepass $TRUSTSTORE_PASS -noprompt
    java -Djavax.net.ssl.trustStore=$TRUSTSTORE_FILE -Djavax.net.ssl.trustStorePassword=$TRUSTSTORE_PASS -jar flare.jar
else
    echo "No ca cert file /app/certs/ca-cert.pem found -> starting flare without own CA"
    java -jar flare.jar
fi