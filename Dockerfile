FROM maven:3.8.4-openjdk-16

RUN apt-get update && apt-get install -y \
    apt-utils \
    dialog \
    xvfb \
    libxrender1 \
    libxtst6 \
    libxi6 \
    && rm -rf /var/lib/apt/lists/* 

ADD https://download2.gluonhq.com/openjfx/16/openjfx-16_linux-x64_bin-sdk.zip /opt/javafx.zip
RUN cd /opt && unzip javafx.zip && rm javafx.zip

ENV PATH="/opt/javafx-sdk-16/bin:${PATH}"
ENV JAVA_HOME=/usr/local/openjdk-16
ENV MAVEN_OPTS="--module-path /opt/javafx-sdk-16/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -Dprism.order=sw"

COPY . /usr/src/app
WORKDIR /usr/src/app

CMD ["mvn", "clean", "package"]
