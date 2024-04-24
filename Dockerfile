# Use a base image with JDK installed
FROM eclipse-temurin:17-jdk

# Install necessary packages
RUN apt-get update && apt-get install -y \
    xvfb \
    libxrender1 \
    libxtst6 \
    libxi6 \
    unzip \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Download and set up Maven manually since the Maven image isn't being used directly
ARG MAVEN_VERSION=3.9.6
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Download and setup JavaFX
ADD https://download2.gluonhq.com/openjfx/16/openjfx-16_linux-x64_bin-sdk.zip /opt/javafx.zip
RUN cd /opt && unzip javafx.zip && rm javafx.zip

# Set environment variables for Java and Maven to include JavaFX
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/javafx-sdk-16/bin:${JAVA_HOME}/bin:${PATH}"
ENV MAVEN_OPTS="--module-path /opt/javafx-sdk-16/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -Dprism.order=sw"

# Copy your application source code
COPY . /usr/src/app
WORKDIR /usr/src/app

# Run Maven build
CMD ["mvn", "clean", "package"]
