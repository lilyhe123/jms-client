
FROM openjdk:8-jdk 

# -------------------------------------------------------------
ENV CLASSPATH=/demo/lib/wlthint3client.jar:/demo/classes \
    PATH=$PATH:/demo

# Copy scripts, lib and java files
# --------------------------------
RUN mkdir /demo /demo/classes
COPY  container-scripts/ /demo/
RUN cd /demo/jmsclient/samples && javac -d /demo/classes -g *.java

WORKDIR /demo 

