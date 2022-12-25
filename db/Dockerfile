FROM postgres
EXPOSE 5432
ENV POSTGRES_USER=nuntius
COPY schema/schema.sql docker-entrypoint-initdb.d/schema.sql