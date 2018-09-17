FROM ubuntu:14.04
MAINTAINER Kasper Luckow <kasper.luckow@sv.cmu.edu>

#############################################################################
# Setup base image 
#############################################################################
RUN \
  apt-get update -y && \
  apt-get install software-properties-common -y && \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository ppa:webupd8team/java -y && \
  apt-get update -y && \
  apt-get install -y oracle-java8-installer \
                ant \
                maven \
                git \
                junit \
                build-essential \
                python \
                antlr3 \
                && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

#############################################################################
# Environment 
#############################################################################

# set java env
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV JUNIT_HOME /usr/share/java

RUN mkdir /jdart-project
ENV JDART_DIR /jdart-project

#############################################################################
# Dependencies 
#############################################################################

# Install jpf-core
WORKDIR ${JDART_DIR}
RUN git clone https://github.com/javapathfinder/jpf-core.git

# We know that rev 29 works with jdart
WORKDIR ${JDART_DIR}/jpf-core
RUN git checkout JPF-8.0
RUN ant 
#Could run ant test here but it takes a long time 

# Install jConstraints
WORKDIR ${JDART_DIR}
RUN git clone https://github.com/psycopaths/jconstraints.git
WORKDIR ${JDART_DIR}/jconstraints
RUN git checkout jconstraints-0.9.1
RUN mvn install

# Install Z3
WORKDIR ${JDART_DIR}
# Note that we specify a specific *release* of Z3
RUN wget https://github.com/Z3Prover/z3/releases/download/z3-4.4.1/z3-4.4.1-x64-ubuntu-14.04.zip 
RUN unzip z3-4.4.1-x64-ubuntu-14.04.zip && \
        rm z3-4.4.1-x64-ubuntu-14.04.zip
RUN ln -s z3-4.4.1-x64-ubuntu-14.04 z3
WORKDIR ${JDART_DIR}/z3/bin
RUN mvn install:install-file -Dfile=com.microsoft.z3.jar -DgroupId=com.microsoft -DartifactId=z3 -Dversion=4.4.1 -Dpackaging=jar
ENV LD_LIBRARY_PATH ${JDART_DIR}/z3/bin

# install jconstraints-z3
WORKDIR ${JDART_DIR}
RUN git clone https://github.com/psycopaths/jconstraints-z3.git 
WORKDIR ${JDART_DIR}/jconstraints-z3
RUN git checkout jconstraints-z3-0.9.0
RUN mvn install

# Set up jpf conf and jconstraints
RUN mkdir /root/.jpf
RUN echo "jpf-core = ${JDART_DIR}/jpf-core" >> /root/.jpf/site.properties
RUN echo "jpf-jdart = ${JDART_DIR}/jdart" >> /root/.jpf/site.properties
RUN echo "extensions=\${jpf-core}" >> /root/.jpf/site.properties

RUN mkdir -p /root/.jconstraints/extensions
RUN cp ${JDART_DIR}/jconstraints-z3/target/jconstraints-z3-0.9.0.jar /root/.jconstraints/extensions
RUN cp /root/.m2/repository/com/microsoft/z3/4.4.1/z3-4.4.1.jar /root/.jconstraints/extensions/com.microsoft.z3.jar

#############################################################################
# Install JDart
#############################################################################

WORKDIR ${JDART_DIR}
RUN git clone https://github.com/psycopaths/jdart.git 
WORKDIR ${JDART_DIR}/jdart
RUN ant
