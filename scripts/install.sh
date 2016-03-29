#!/bin/bash

# Copyright 2016 Marko Dimjašević
#
# The PSYCO: A Predicate-based Symbolic Compositional Reasoning environment
# platform is licensed under the Apache License, Version 2.0 (the "License"); you
# may not use this file except in compliance with the License. You may obtain a
# copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.

# This script is used for installing JDart and its dependencies in a
# Vagrant virtual machine. See Vagrantfile in the root directory.

# Exit on error
set -e

home=/home/vagrant
project_root_dir=${home}/jdart-project

# Set these flags to control what to install
install_packages=1
install_z3=1
install_jpf_core=1
install_jdart=1

# Directories
z3_dir=$project_root_dir/z3


if [ ${install_packages} -eq 1 ]; then

    # Fixate the Debian mirror used
    apt_source_file=/etc/apt/sources.list
    sudo su -c "echo 'deb http://ftp.us.debian.org/debian jessie main' > ${apt_source_file}"
    sudo su -c "echo 'deb-src http://ftp.us.debian.org/debian jessie main' >> ${apt_source_file}"

    sudo su -c "echo 'deb http://security.debian.org/ jessie/updates main' >> ${apt_source_file}"
    sudo su -c "echo 'deb-src http://security.debian.org/ jessie/updates main' >> ${apt_source_file}"

    # Make sure Debian backports are enabled. This is needed for Java
    # 8 packages
    apt_source_file=/etc/apt/sources.list.d/debian-backports.list
    sudo su -c "echo 'deb http://mirrors.kernel.org/debian jessie-backports main' >> ${apt_source_file}"
    sudo su -c "echo 'deb-src http://mirrors.kernel.org/debian jessie-backports main' >> ${apt_source_file}"

    sudo apt-get update
    dependencies="git mercurial ant ant-optional openjdk-8-jre openjdk-8-jre-headless openjdk-8-jdk antlr3 libguava-java python maven build-essential"
    sudo apt-get install --assume-yes $dependencies

    # Set Java 8 as the default Java version
    sudo update-alternatives --set java  /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
    sudo update-alternatives --set javac /usr/lib/jvm/java-8-openjdk-amd64/bin/javac
fi

# Make sure Java 8 is the default version
java_version=`java -version 2>&1 | grep "version" | awk -F"\"" '{print $2}' | awk -F"\." '{print $1"."$2}'`
if [ "${java_version}" != "1.8" ]; then
    echo "Error: Java 8 must be set as the default Java version" >&2
    exit 1
fi
javac_version=`javac -version 2>&1 | awk -F" " '{print $2}' | awk -F"\." '{print $1"."$2}'`
if [ "${javac_version}" != "1.8" ]; then
    echo "Error: Java compiler 8 must be set as the default Java version" >&2
    exit 1
fi

# Install Z3

if [ ${install_z3} -eq 1 ]; then

    mkdir -p ${z3_dir}
    cd ${project_root_dir}
    z3_archive=z3-4.4.1.tar.gz
    wget https://github.com/Z3Prover/z3/archive/${z3_archive}
    tar xf ${z3_archive} --strip-components 1 --directory ${z3_dir}
    rm ${z3_archive}
    cd ${z3_dir}
    
    python scripts/mk_make.py --java
    cd build
    make all
    sudo make install

    mvn install:install-file -Dfile=com.microsoft.z3.jar -DgroupId=com.microsoft -DartifactId=z3 -Dversion=0.9 -Dpackaging=jar
fi

# Install JPF modules
# JPF configuration directory

if [ ${install_jpf_core} -eq 1 ] || [ ${install_jdart} -eq 1 ]; then

    jpf_conf_dir=${home}/.jpf
    mkdir -p $jpf_conf_dir
    jpf_conf_file=${home}/.jpf/site.properties
fi


if [ ${install_jpf_core} -eq 1 ]; then

    # Install jpf-core
    jpf_core_dir=${project_root_dir}/jpf-core
    hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core ${jpf_core_dir}
    cd ${jpf_core_dir}
    # Revert to a commit we are sure works with JDart
    hg update -r 29
    ant

    echo "jpf-core = ${jpf_core_dir}" >> ${jpf_conf_file}
fi


if [ ${install_jdart} -eq 1 ]; then

    # Install jconstraints
    jconstraints_dir=${project_root_dir}/jconstraints
    git clone https://github.com/psycopaths/jconstraints.git ${jconstraints_dir}
    cd ${jconstraints_dir}
    mvn install

    # Install jconstraints-z3
    # export LD_LIBRARY_PATH=/usr/lib
    jconstraints_z3_dir=${project_root_dir}/jconstraints-z3
    git clone https://github.com/psycopaths/jconstraints-z3.git ${jconstraints_z3_dir}
    cd ${jconstraints_z3_dir}
    mvn install

    jconstraints_conf_dir=${project_root_dir}/.jconstraints
    mkdir -p ${jconstraints_conf_dir}/extensions
    ln -s ${jconstraints_conf_dir} ${home}/.jconstraints || true
    cp target/jConstraints-z3-1.0-SNAPSHOT.jar ${home}/.jconstraints/extensions
    cp ${z3_dir}/build/com.microsoft.z3.jar ${home}/.jconstraints/extensions

    echo "jconstraints = $jconstraints_dir" >> ${jpf_conf_file}

    # Install JDart
    jdart_dir=${project_root_dir}/jdart
    cd ${jdart_dir}
    ant

    echo "jpf-jdart = ${jdart_dir}" >> ${jpf_conf_file}
    echo "extensions=\${jpf-core},\${jpf-jdart}" >> ${jpf_conf_file}

fi
