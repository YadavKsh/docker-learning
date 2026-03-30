FROM eclipse-temurin:21-jdk-jammy
LABEL authors="kshit"

WORKDIR /usr/src/myapp

COPY . /usr/src/myapp

RUN javac src/Test.java

CMD ["java", "-cp", "src", "Test"]