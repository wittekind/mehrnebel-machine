FROM resin/rpi-raspbian:jessie as builder
MAINTAINER daniel@wittekind.io

COPY docker/raspberrypi.gpg.key /key/
RUN echo 'deb http://archive.raspberrypi.org/debian/ jessie main' >> /etc/apt/sources.list.d/raspi.list && \
    echo oracle-java8-jdk shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-key add /key/raspberrypi.gpg.key

RUN apt-get update && \
    apt-get -y install oracle-java8-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN ./gradlew build shadowJar

FROM resin/rpi-raspbian:jessie as fogger
MAINTAINER daniel@wittekind.io

COPY docker/raspberrypi.gpg.key /key/
RUN echo 'deb http://archive.raspberrypi.org/debian/ jessie main' >> /etc/apt/sources.list.d/raspi.list && \
    echo oracle-java8-jdk shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-key add /key/raspberrypi.gpg.key

RUN apt-get update && \
    apt-get -y install oracle-java8-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /mehrnebel/artnet-fogger

COPY --from=builder /usr/src/app/build/libs/artnet-fogger-0.0.1-fat.jar /mehrnebel/artnet-fogger/app.jar

ADD docker/production.json /mehrnebel/artnet-fogger/production.json

CMD ["java", "-jar", "/mehrnebel/artnet-fogger/app.jar", "-conf", "/mehrnebel/artnet-fogger/production.json"]
