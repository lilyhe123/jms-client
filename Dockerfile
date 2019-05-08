
FROM store/oracle/weblogic:12.2.1.3

ENV CLIENT_DIR=/u01/demo/
ENV CLASSPATH=/u01/oracle/wlserver/modules/features/wlst.wls.classpath.jar:$CLIENT_DIR/classes \
    PATH=$PATH:$CLIENT_DIR
# Copy scripts, lib and java files
# --------------------------------
RUN mkdir -p $CLIENT_DIR/classes
COPY  container-scripts/ $CLIENT_DIR 
RUN cd $CLIENT_DIR/jmsclient/samples && javac -d $CLIENT_DIR/classes -g *.java

WORKDIR $CLIENT_DIR
