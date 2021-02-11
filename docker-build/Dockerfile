FROM openjdk:8
RUN /bin/mkdir /home/controller
ADD flink-operator.jar /home/controller
WORKDIR /home/controller
EXPOSE 8080
ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-jar", "flink-operator.jar"]
