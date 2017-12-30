FROM resin/raspberry-pi-openjdk:8-jdk as builder
MAINTAINER daniel@wittekind.io

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN sudo apt-get install ca-certificates-java

RUN ./gradlew build shadowJar

FROM resin/raspberry-pi-openjdk:8-jre as machine
MAINTAINER daniel@wittekind.io

WORKDIR /mehrnebel/machine

COPY --from=builder /usr/src/app/build/libs/machine-0.0.1-fat.jar /mehrnebel/machine/app.jar

ADD docker/production.json /mehrnebel/machine/production.json

CMD ["java", "-jar", "/mehrnebel/machine/app.jar", "-conf", "/mehrnebel/machine/production.json"]
