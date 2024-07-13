FROM java:8-jdk-alpine

COPY ./target/p2p-validate-service-1.0.0.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch p2p-validate-service-1.0.0.jar'

ENTRYPOINT ["java","-jar","p2p-validate-service-1.0.0.jar"]