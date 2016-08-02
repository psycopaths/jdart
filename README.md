# JDart #
JDart is a tool for performing *concolic execution* on a Java program. It is written as an extension to NASA Java Pathfinder (JPF).  The aim
of concolic execution is to explore additional behavior in the program by generating
input values which will result in a different path being taken through a program
(or method).

To cite JDart, please use the most recent paper that was accepted at TACAS 2016:

* Kasper Luckow, Marko Dimjasevic, Dimitra Giannakopoulou, Falk Howar, Malte Isberner, Temesghen Kahsai, Zvonimir Rakamaric, Vishwanath Raman, **JDart: A Dynamic Symbolic Analysis Framework**, 22nd International Conference on Tools and Algorithms for the Construction and Analysis of Systems (TACAS 2016), \[[pdf](http://soarlab.org/publications/tacas2016-ldghikrr.pdf)\] \[[bibtex](http://soarlab.org/publications/tacas2016-ldghikrr.bib)\].

If you want to repeat experiments reported in the paper, use a
[reproducible research environment in Aptlab][4].

## Installation ##
If you want to install JDart in an easy way in a virtual machine,
simply run:

```bash
vagrant up
```

The command will take about 20 minutes depending on your machine. For
this to work you need [Vagrant][6] installed. Additionally, you need
either [VirtualBox][7] or [libvirt][8]. The command above will
automatically do all the steps described below, so you can skip to the
Using JDart section.

The prerequisites for JDart are:
* JPF-core 8
* jConstraints
* Adding jConstraints solver plugins, e.g., jConstraints-z3 for interfacing with Z3

The following provides installation instructions for each of these components.

### JPF-core 8 ##
JDart is compatible with version 8 of the Java PathFinder framework. Please make sure
you have the most recent version of [jpf-core][0].

**Step 1:** Clone the `jpf-core` repository:
```bash
$ hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core
```

**Step 2:** Build *jpf-core*
```bash
$ ant
```

**Step 3:** Make sure the `jpf-core` property in your `site.properties` (in `$HOME/.jpf`) points to the
   respective directory. Also make sure to add the property to `extensions`. In summary, your `site.properties` file should contain the following:
```bash
$ vim ~/.jpf/site.properties

# ...
jpf-core = /path/to/jpf-core
extensions=${jpf-core}
```

### jConstraints ###
JDart uses the [jConstraints][1] library as an abstraction layer for interfacing
the solver. jConstraints uses plugins for supporting multiple constraint solvers.
For licensing reasons no plugin is included in JDart. 

In order to run JDart you have to install jConstraints **and** a plugin for **at least** one constraint solver.

jConstraints supports a variety of solvers. Please consult the respective installation instructions for each of them by accessing the modules on the main GitHub organization, [Psycopaths][3].

In summary:

**Step 1:** Follow the installation instructions for [jConstraints][1]

**Step 2:** Assuming you would like to have Z3 support, follow the installation instructions for [jConstraints-z3][5]. Alternatively, have a look at the other solver plugins for jConstraints on [Psycopaths' GitHub org][3].


## Installing JDart ##
**Step 1:** Clone the `JDart` repository:
```bash
$ git clone https://github.com/psycopaths/jdart.git
```

**Step 2:** Make sure that your `site.properties` contains the appropriate entry for the `jpf-jdart`
property. You can have additional JPF modules installed, but the minimum configuration for JDart should look like the following in `site.properties`: 
```bash
$ vim ~/.jpf/site.properties

# ...
jpf-core = /path/to/jpf-core
jpf-jdart = /path/to/jdart

extensions=${jpf-core}
```

Note that the extensions property **is not** updated with the `jpf-jdart` property. This is intentional. Instead, use the `@using = jpf-jdart` directive in your application `jpf` file. 

**Step 3:** Installing JDart is as simple as just running the ant build ant build in the JDart directory:
```bash
$ cd /path/to/jdart 
$ ant
```

You should now be ready to use JDart.

## Using JDart ##
The analysis configuration is specified in a jpf application properties file. The minimum configuration required is:
```
@using = jpf-jdart

# Specify the analysis shell. Can also be MethodSummarizer
shell=gov.nasa.jpf.jdart.JDart

# Specify the constraint solver. Can be any of the jConstraints solver plugins
symbolic.dp=z3

# Provide the fully qualified class name of the entry point of the SUT
target=features.simple.Input

# Set up the concolic method with symbolic/concrete parameters. See the wiki for more details
concolic.method.bar=features.simple.Input.bar(d:double)

# Specify the concolic method configuration object to use
concolic.method=bar

```

For an example of how to configure JDart, please have a look at the `test_xxx.jpf` files
in `src/examples/features/simple`. JDart can be run on these examples using the `jpf` binary in jpf-core:
```bash
$ /path/to/jpf-core/bin/jpf /path/to/jdart/src/examples/features/simple/test_foo.jpf
```

The documentation for the concolic execution configuration can be found in the wiki.


[0]: http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-core
[1]: https://github.com/psycopaths/jConstraints
[3]: https://github.com/psycopaths
[4]: https://www.aptlab.net/p/CAVA/jdart-tacas-2016-v4
[5]: https://github.com/psycopaths/jConstraints-z3
[6]: https://www.vagrantup.com/
[7]: https://www.virtualbox.org/
[8]: https://libvirt.org/
