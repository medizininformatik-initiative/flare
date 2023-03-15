FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get upgrade -y && \
    apt-get purge wget libbinutils libctf0 libctf-nobfd0 libncurses6 -y && \
    apt-get autoremove -y && apt-get clean && \
    rm -rf /var/lib/apt/lists/

RUN mkdir -p /app/cache && chown 1001:1001 /app/cache
COPY target/flare.jar /app/
COPY ontology/codex-term-code-mapping.json /app/ontology/
COPY ontology/codex-code-tree.json /app/ontology/

WORKDIR /app
USER 1001

ENV FLARE_CACHE_DISK_PATH="/app/cache"
ENV FLARE_MAPPING_MAPPINGS_FILE="/app/ontology/codex-term-code-mapping.json"
ENV FLARE_MAPPING_CONCEPT_TREE_FILE="/app/ontology/codex-code-tree.json"

CMD ["java", "-jar", "flare.jar"]
