FROM openjdk:8-jdk-alpine as builder
MAINTAINER daniel@wittekind.io

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN ./gradlew build shadowJar

FROM resin/raspberry-pi-openjdk:8-jre as machine
MAINTAINER daniel@wittekind.io

RUN apt-get update && \
    apt-get -y install wget && \
    apt-get clean

RUN wget http://get.pi4j.com/download/pi4j-1.1.deb && \
    sudo dpkg -i pi4j-1.1.deb

WORKDIR /mehrnebel/machine

COPY --from=builder /usr/src/app/build/libs/machine-0.0.1-fat.jar /mehrnebel/machine/app.jar

ADD docker/production.json /mehrnebel/machine/production.json

VOLUME /sys

ENTRYPOINT ["java", "-classpath", ".:classes:/opt/pi4j/lib/'*'", "-jar", "/mehrnebel/machine/app.jar"]
CMD ["-conf", "/mehrnebel/machine/production.json"]
