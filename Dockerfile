FROM eclipse-temurin:21.0.9_10-jre-noble@sha256:67fc762eabacb56e5444b367889e04ce8c839b8f4b3d8ef3e459c5579fbefd8a

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install libjemalloc2 -y && \
    apt-get purge wget libncurses6 -y && \
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
