FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get upgrade -y && \
    apt-get purge wget libbinutils libctf0 libctf-nobfd0 libncurses6 -y && \
    apt-get autoremove -y && apt-get clean && \
    rm -rf /var/lib/apt/lists/

RUN mkdir -p /app/cache && chown 1001:1001 /app/cache
COPY target/flare.jar /app/
COPY ontology/codex-term-code-mapping.json /app/ontology/
COPY ontology/codex-code-tree.json /app/ontology/

ENV FLARE_CACHE_DISK_PATH="/app/cache"
ENV JAVA_TOOL_OPTIONS="-Xmx4g"

ENV CERTIFICATE_PATH=/app/certificates
ENV TRUSTSTORE_PATH=/app/truststore

ENV CERT_FILE=self-signed-cert.crt
ENV TRUSTSTORE_FILE=self-signed-truststore.jks


RUN mkdir -p $CERTIFICATE_PATH $TRUSTSTORE_PATH
COPY $CERT_FILE* $CERTIFICATE_PATH/

RUN if test -e $CERTIFICATE_PATH/$CERT_FILE; then \
    keytool -genkey -alias self-signed-truststore -keyalg RSA -keystore $TRUSTSTORE_PATH/$TRUSTSTORE_FILE -storepass changeit -keypass changeit -dname "CN=test,OU=test,O=test,L=test,S=test,C=TE"; \   
    keytool -importcert -alias self-signed-cert-ca -file $CERTIFICATE_PATH/$CERT_FILE -keystore $TRUSTSTORE_PATH/$TRUSTSTORE_FILE -storepass changeit -noprompt; \
    fi
 
WORKDIR /app
USER 1001

COPY ./docker-entrypoint.sh /
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]