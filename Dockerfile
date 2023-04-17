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

ENV TRUSTSTORE_FILE=self-signed-truststore.jks

RUN mkdir -p $CERTIFICATE_PATH $TRUSTSTORE_PATH
RUN keytool -genkey -alias self-signed-truststore -keyalg RSA -keystore $TRUSTSTORE_PATH/$TRUSTSTORE_FILE -storepass changeit -keypass changeit -dname "CN=test,OU=test,O=test,L=test,S=test,C=TE" 
RUN chown 1001 $TRUSTSTORE_PATH/$TRUSTSTORE_FILE

WORKDIR /app
USER 1001

COPY ./docker-entrypoint.sh /
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]
