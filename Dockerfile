FROM resin/rpi-raspbian:stretch as builder
MAINTAINER daniel@wittekind.io

RUN apt-key adv --recv-key --keyserver keyserver.ubuntu.com EEA14886 && \
    echo 'deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' && \
    echo 'deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' && \
    apt-get update && \
    apt-get -y install oracle-java8-installer oracle-java8-set-default && \
    source /etc/profile

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN ./gradlew build shadowJar

FROM resin/rpi-raspbian as machine
MAINTAINER daniel@wittekind.io

COPY docker/raspberrypi.gpg.key /key/
RUN echo 'deb http://archive.raspberrypi.org/debian/ wheezy main' >> /etc/apt/sources.list.d/raspi.list && \
    echo oracle-java8-jdk shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-key add /key/raspberrypi.gpg.key

RUN apt-get update && \
    apt-get -y install oracle-java8-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /mehrnebel/machine

COPY --from=builder /usr/src/app/build/libs/machine-0.0.1-fat.jar /mehrnebel/machine/app.jar

ADD docker/production.json /mehrnebel/machine/production.json

CMD ["java", "-jar", "/mehrnebel/machine/app.jar", "-conf", "/mehrnebel/machine/production.json"]
