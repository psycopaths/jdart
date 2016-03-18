# JDart #
JDart is a tool for performing *concolic execution* on a Java program. It is written as an extension to NASA Java Pathfinder (JPF).  The aim
of concolic execution is to explore additional behavior in the program by generating
input values which will result in a different path being taken through a program
(or method).

The concolic execution is triggered from within the regular execution of a Java
program. The JDart configuration allows users to specify a method which is executed
*concolically*: upon the first invocation of this method, a certain set of 
values (see below) will be treated *symbolically*. Once the method execution is finished,
an attempt will be made to find a valuation (i.e., an assignment of concrete values to
the symbolic variables) which triggers a different execution path through the program.

The result of the concolic execution is a *constraints tree*, i.e., a tree with its inner
nodes reflecting the decisions (involving at least one symbolic variable) that were made
during the execution of the program. The leaves are labeled with the status (`OK` if the
method was regularly exited, `ERROR` if there was an exception, or `DONT_KNOW` if no
valuation could be or should have been generated for the respective path). In case
the status in `OK` or `ERROR`, the leaf will also contain a concrete valuation suitable
for triggering exactly this path through the concolically executed method.

There is a paper under submission on JDart to TACAS 2016. If you want to
repeat experiments reported in the paper, use a
[reproducible research environment in Aptlab][4].

## Installation ##
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
property. You have additional JPF modules installed, but the minimum configuration for JDart should look like the following in `site.properties`: 
```bash
$ vim ~/.jpf/site.properties

# ...
jpf-core = /path/to/jpf-core
jpf-jdart = /path/to/jdart

extensions=${jpf-core}
```

Note that the extensions property **is not** updated with the `jpf-jdart` property. This is intentional.

**Step 3:** Installing JDart is as simple as just running the ant build ant build in the JDart directory:
```bash
$ cd /path/to/jdart 
$ ant
```

You should now be ready to use JDart.

## Using JDart ##
For an example of how to configure JDart, please have a look at the `test_xxx.jpf` files
in `src/examples/simple`. JDart can be run on these examples using the `jpf` binary in jpf-core:
```bash
$ /path/to/jpf-core/bin/jpf /path/to/jdart/src/examples/simple/test_foo.jpf
```

The documentation for the concolic execution configuration can be found in the wiki.


[0]: http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-core
[1]: https://github.com/psycopaths/jConstraints
[3]: https://github.com/psycopaths
[4]: https://www.aptlab.net/p/CAVA/jdart-tacas-2016-v4
[5]: https://github.com/psycopaths/jConstraints-z3
