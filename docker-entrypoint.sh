CERT_FILE="/app/certificates/self-signed-cert.crt"
TRUSTSTORE_FILE="/app/truststore/self-signed-truststore.jks"

if [[ -f "$CERT_FILE"  &&  -f "$TRUSTSTORE_FILE" ]]; then
    keytool -importcert -alias self-signed-cert-ca -file $CERT_FILE -keystore $TRUSTSTORE_FILE -storepass changeit -noprompt
    java -Djavax.net.ssl.trustStore=$TRUSTSTORE_FILE -Djavax.net.ssl.trustStorePassword=changeit -jar flare.jar
else
    java -jar flare.jar
fi
