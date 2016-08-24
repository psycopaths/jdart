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

user=vagrant
# Directories
home_dir=/home/${user}
project_root_dir=${home_dir}/jdart-project
sudo chown ${user}:${user} ${project_root_dir}

# Set these flags to control what to install
install_packages=1
install_z3=1
install_jpf_core=1
install_jdart=1


if [ ${install_packages} -eq 1 ]; then

    sudo add-apt-repository -y ppa:openjdk-r/ppa
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

    z3_version="4.4.1"
    z3_distro="x64-ubuntu-14.04"
    z3_full_version=z3-${z3_version}-${z3_distro}
    z3_archive=${z3_full_version}.zip
    cd /tmp
    wget https://github.com/Z3Prover/z3/releases/download/z3-${z3_version}/${z3_archive}
    unzip ${z3_archive}
    cd ${z3_full_version}/bin
    mvn install:install-file -Dfile=com.microsoft.z3.jar -DgroupId=com.microsoft -DartifactId=z3 -Dversion=${z3_version} -Dpackaging=jar
    rm com.microsoft.z3.jar
    sudo mv lib* /usr/lib/
    sudo mv * /usr/bin
    sudo mv ../include/* /usr/include

    cd /tmp
    rm -rf ${z3_archive} ${z3_full_version}
fi

# Install JPF modules
# JPF configuration directory

if [ ${install_jpf_core} -eq 1 ] || [ ${install_jdart} -eq 1 ]; then

    jpf_conf_dir=${home_dir}/.jpf
    mkdir -p $jpf_conf_dir
    jpf_conf_file=${home_dir}/.jpf/site.properties
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
    git checkout jconstraints-0.9.1
    mvn install
    
    # Install jconstraints-z3
    jconstraints_z3_dir=${project_root_dir}/jconstraints-z3
    git clone https://github.com/psycopaths/jconstraints-z3.git ${jconstraints_z3_dir}
    cd ${jconstraints_z3_dir}
    git checkout jconstraints-z3-0.9.0
    mvn install

    jconstraints_conf_dir=${project_root_dir}/.jconstraints
    mkdir -p ${jconstraints_conf_dir}/extensions
    ln -s ${jconstraints_conf_dir} ${home_dir}/.jconstraints || true
    cp target/jconstraints-z3-0.9.0.jar ${home_dir}/.jconstraints/extensions
    cp ${home_dir}/.m2/repository/com/microsoft/z3/${z3_version}/z3-${z3_version}.jar ${home_dir}/.jconstraints/extensions/com.microsoft.z3.jar

    echo "jconstraints = $jconstraints_dir" >> ${jpf_conf_file}

    # Install JDart
    jdart_dir=${project_root_dir}/jdart
    cd ${jdart_dir}
    ant

    echo "jpf-jdart = ${jdart_dir}" >> ${jpf_conf_file}
    echo "extensions=\${jpf-core},\${jpf-jdart}" >> ${jpf_conf_file}

fi
