FROM openjdk:11-jre
WORKDIR /app
COPY ./target/*.jar ./app.jar
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "-Djava.security.egd=file:/dev/./urandom", "/app/app.jar", "--server.port=8080"]
EXPOSE 8080