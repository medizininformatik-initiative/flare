FROM eclipse-temurin:21.0.5_11-jre-jammy@sha256:5f8358c9d5615c18e95728e8b8528bda7ff40a7a5da2ac9a35b7a01f5d9b231a

RUN apt-get update && apt-get upgrade -y && \
    apt-get purge wget libbinutils libctf0 libctf-nobfd0 libncurses6 -y && \
    apt-get autoremove -y && apt-get clean && \
    rm -rf /var/lib/apt/lists/

RUN mkdir -p /app/cache && chown 1001:1001 /app/cache
COPY target/flare.jar /app/
COPY ontology/mapping.zip /app/ontology/

ENV FLARE_CACHE_DISK_PATH="/app/cache"
ENV JAVA_TOOL_OPTIONS="-Xmx4g"

ENV CERTIFICATE_PATH=/app/certs
ENV TRUSTSTORE_PATH=/app/truststore
ENV TRUSTSTORE_FILE=self-signed-truststore.jks

RUN mkdir -p $CERTIFICATE_PATH $TRUSTSTORE_PATH
RUN chown 1001 $CERTIFICATE_PATH $TRUSTSTORE_PATH

WORKDIR /app
USER 1001

COPY ./docker-entrypoint.sh /
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]
