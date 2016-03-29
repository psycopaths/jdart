# coding: utf-8
# -*- mode: ruby -*-
# vi: set ft=ruby :

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


# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
    config.cache.synced_folder_opts = {
      type: :nfs,
      mount_options: ['rw', 'vers=4', 'tcp', 'nolock']
    }
  end
  
  config.vm.box = "debian/jessie64"

  config.vm.provider :libvirt do |domain|
    domain.uri = 'qemu+unix:///system'
    domain.disk_bus = "virtio"
    domain.memory = 2048
  end

  config.vm.provider :virtualbox do |domain|
    domain.memory = 2048
  end
  
  config.vm.synced_folder ".", "/home/vagrant/jdart-project/jdart"

  config.vm.provision :shell, path: "scripts/install.sh"
end
