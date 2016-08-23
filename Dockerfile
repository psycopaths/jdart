FROM ubuntu:14.04
MAINTAINER Kasper Luckow <kasper.luckow@sv.cmu.edu>
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
                mercurial \
                junit \
                build-essential \
                python \
                antlr3 \
                && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# set java env
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV JUNIT_HOME /usr/share/java

RUN mkdir jdart-project

# Install jpf-core
WORKDIR /jdart-project
RUN hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core 
# We know that rev 29 works with jdart
WORKDIR /jdart-project/jpf-core
RUN hg update -r 29
RUN ant 
#Could run ant test here but it takes a long time 
#RUN java -jar build/RunJPF.jar src/examples/Racer.jpf

# Install jConstraints
WORKDIR /jdart-project
RUN git clone https://github.com/psycopaths/jconstraints.git
WORKDIR /jdart-project/jconstraints
RUN mvn install

# Install Z3
WORKDIR /jdart-project
# Note that we specify a specific *release* of Z3
RUN wget https://github.com/Z3Prover/z3/releases/download/z3-4.4.1/z3-4.4.1-x64-ubuntu-14.04.zip 
RUN unzip z3-4.4.1-x64-ubuntu-14.04.zip && \
        rm z3-4.4.1-x64-ubuntu-14.04.zip
RUN ln -s z3-4.4.1-x64-ubuntu-14.04 z3
WORKDIR /jdart-project/z3/bin
RUN mvn install:install-file -Dfile=com.microsoft.z3.jar -DgroupId=com.microsoft -DartifactId=z3 -Dversion=4.4.1 -Dpackaging=jar
ENV LD_LIBRARY_PATH /jdart-project/z3/bin

# install jconstraints-z3
WORKDIR /jdart-project
RUN git clone https://github.com/psycopaths/jconstraints-z3.git 
WORKDIR /jdart-project/jconstraints-z3
RUN mvn install

# Set up jpf conf and jconstraints
RUN mkdir /root/.jpf
RUN echo "jpf-core = /jdart-project/jpf-core" >> /root/.jpf/jpf.properties
RUN echo "jpf-jdart = /jdart-project/jdart" >> /root/.jpf/jpf.properties
RUN echo "extensions=\${jpf-core}" >> /root/.jpf/jpf.properties

RUN mkdir -p /root/.jconstraints/extensions
RUN cp /jdart-project/jconstraints-z3/target/jConstraints-z3-1.0-SNAPSHOT.jar /root/.jconstraints/extensions
RUN cp /root/.m2/repository/com/microsoft/z3/4.4.1/z3-4.4.1.jar /root/.jconstraints/extensions/com.microsoft.z3.jar

# Install JDart
WORKDIR /jdart-project
RUN git clone https://github.com/psycopaths/jdart.git 
WORKDIR /jdart-project/jdart
RUN ant
