FROM resin/rpi-raspbian as machine
MAINTAINER daniel@wittekind.io

COPY docker/raspberrypi.gpg.key /key/
RUN echo 'deb http://archive.raspberrypi.org/debian/ wheezy main' >> /etc/apt/sources.list.d/raspi.list && \
    echo oracle-java8-jdk shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-key add /key/raspberrypi.gpg.key

RUN apt-get update && \
    apt-get -y install wget oracle-java8-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN wget http://get.pi4j.com/download/pi4j-1.1.deb && \
    sudo dpkg -i pi4j-1.1.deb

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN ./gradlew build shadowJar

RUN mkdir -p /mehrnebel/machine && \
    cp /usr/src/app/build/libs/machine-0.0.1-fat.jar /mehrnebel/machine/app.jar && \
    rm -rf /usr/src/app

WORKDIR /mehrnebel/machine

ADD docker/production.json /mehrnebel/machine/production.json

ENTRYPOINT ["java", "-classpath", ".:classes:/opt/pi4j/lib/'*'", "-jar", "/mehrnebel/machine/app.jar"]
CMD ["-conf", "/mehrnebel/machine/production.json"]
