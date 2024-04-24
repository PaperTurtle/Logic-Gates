# Use a base JDK image from Eclipse Temurin
FROM eclipse-temurin:21-jdk as builder

# Install required packages for the build
RUN apt-get update && apt-get install -y \
    xvfb \
    libxrender1 \
    libxtst6 \
    libxi6 \
    unzip \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Download and extract JavaFX SDK
ADD https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-x64_bin-sdk.zip /opt/javafx.zip
RUN cd /opt && unzip javafx.zip && rm javafx.zip

# Set environment variables to configure Java and Maven
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/javafx-sdk-21/bin:$JAVA_HOME/bin:$PATH"
ENV MAVEN_HOME=/usr/share/maven
ENV MAVEN_CONFIG="/root/.m2"
ENV MAVEN_OPTS="--module-path /opt/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -Dprism.order=sw"

# Install Maven
ARG MAVEN_VERSION=3.9.6
RUN mkdir -p $MAVEN_HOME /usr/share/maven/ref \
  && curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar -xzC $MAVEN_HOME --strip-components=1 \
  && ln -s $MAVEN_HOME/bin/mvn /usr/bin/mvn

# Copy the project files into the image
COPY . /usr/src/app
WORKDIR /usr/src/app

# Build the application
RUN mvn clean package

# Start a new stage for the final image to reduce size
FROM eclipse-temurin:21-jdk
COPY --from=builder /usr/src/app/target/logic_gates-1.0-SNAPSHOT-shaded.jar /app/application.jar
WORKDIR /app
CMD ["java", "-jar", "/app/application.jar"]
