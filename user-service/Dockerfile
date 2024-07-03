#
# Build stage
#
FROM maven:3.8.3-openjdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Run stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/user_service.jar /home/app/target/user_service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/home/app/target/user_service.jar"]