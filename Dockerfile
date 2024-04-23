FROM maven:3.9.6-eclipse-temurin-17

RUN apt-get update && apt-get install -y \
    xvfb \
    libxrender1 \
    libxtst6 \
    libxi6 \
    unzip \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/* 

ADD https://download2.gluonhq.com/openjfx/16/openjfx-16_linux-x64_bin-sdk.zip /opt/javafx.zip
RUN cd /opt && unzip javafx.zip && rm javafx.zip

# Ensure JAVA_HOME is set to where the JDK is actually installed in the Maven image
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/javafx-sdk-16/bin:$JAVA_HOME/bin:$PATH"
ENV MAVEN_OPTS="--module-path /opt/javafx-sdk-16/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -Dprism.order=sw"

COPY . /usr/src/app
WORKDIR /usr/src/app

# Command to run Maven build
CMD ["mvn", "clean", "package"]
