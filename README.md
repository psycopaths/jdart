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
   respective directory.
```bash
# ~/.jpf/site.properties
# ...
jpf-core = /path/to/jpf-core
```

### jConstraints ###
JDart uses the [*jConstraints*][1] library as an abstraction layer for interfacing
the solver (*jConstraints* uses plugins for supporting multiple constraint solvers.
For licensing reasons no plugin is included in JDart. In order to run JDart you 
have to install a plugin for at least one constraint solver.
You have to copy the jar of any plugin into the library folder and update the
native classpath in the `jpf.properties` file accordingly.

*JConstraints* supports a variety of solvers. Please consult the respective installation instructions for each of them from by accessing the modules on main GitHub organization, [Psycopaths][3]. E.g., the Z3 plugin, *jConstraints-z3*, can be installed by following the instructions on the [GitHub page][1].


## Installing JDart ##
Installing JDart is as simple as just running `ant` in the JDart directory. Make sure
that your `site.properties` contains the appropriate entry for the `jpf-jdart`
property.

## Using JDart ##
For an example of how to configure JDart, please have a look at the `test_xxx.jpf` files
in `src/examples/simple`. JDart can be run on these examples using the `jpf` binary:
```bash
/path/to/jpf-core/bin/jpf src/examples/simple/test_foo.jpf
```
Make sure that the `JVM_ARGS` environment variable is set correctly (cf. the instructions
for installing *jpf-constraints*).

## Concolic Execution Configuration ##
Every concolic execution must be part of the regular execution of a Java program. Hence,
the `target` configuration value for JPF **must** appear in the configuration.

In the JDart configuration, several methods can be prepared to be concolically analyzed,
but only a single specific analysis can be executed per program execution. Each target
method configuration must have a unique (among target method configurations) name
(e.g., `cm1`). The JPF configuration prefix for a configuration named `<name>` is
`concolic.methods.<name>` (e.g., `concolic.methods.sample`). In the remainder of
this section, we will refer to this prefix as `<prefix>`.

The method to be in fact analyzed is specified by setting the `concolic.method` configuration
to the `<name>` of the target method. For example:
```bash
...
concolic.method = cm1
```

THe specification of a target method is given through its fully qualified name plus
its signature (using primitive type or fully qualified class names; without return
type). Parameters can optionally be prepended by a name followed by a colon (`:`).
This specifies both that they are to be treated symbolically as well as the name
used to refer to them (it is discouraged to use anything but valid Java identifiers
here).

A sample target method configuration could look like this:
```bash
concolic.methods.cm1=sample.Foo.foo(a:int,b:boolean)
```
This triggers the concolic execution of method `foo` with an `int`
and a `boolean` parameter of class `sample.Foo`. The parameters
will be referred to as `a` and `b`, respectively. Note that also
the following would have been valid:
```bash
concolic.methods.cm1=sample.Foo.foo(a:int,boolean)
```
In this case, however, the second parameter would not be treated symbolically.


###  Naming Scheme ###
The names to assign to symbolic variables are generally quite straightforward,
and are mostly valid Java expressions. The names for the symbolic
parameters are derived from the method specification (see above); if the method
is an instance method, the target instance will be referred to as `this`. 
If an object named `obj` is symbolic and a field `f` of it is meant to be made symbolic,
it will be referred to as `obj.f` (for example `this.a`). If an array variable named
`arr` is symbolic, its elements will be referred to as `arr[0]`, `arr[1]` etc.
Static fields of classes which should be treated symbolically are referred to by their
fully qualified name (e.g., `Sample.Foo.staticField`). As this complies with Java,
there should be no name clashes in assigning symbolic names to variables.

In order to improve readability, deviations from this scheme occur for the following
frequently used Java classes:

 * `java.lang.String`: As there currently is no possibility to treat strings symbolically,
   those are **never** symbolic.
 * `java.util.ArrayList`, `java.util.LinkedList`, `java.util.Vector`: The symbolic naming
   scheme corresponds to those for arrays; this also means that `size`/`elementCount` fields
   are never made symbolic.
 * `java.util.HashMap`: The symbolic naming scheme is similar to those of arrays. However,
   the indices are quoted strings if the key type is string (for instance `hashMap["foo"]`),
   or a running number with a leading hash sign (`hashMap[#0]`, `hashMap[#1]` etc.)
   if the key is of some other type.


## Analysis Configuration ##
The concolic execution can be further configured by specifying an "analysis configuration".
Analysis configurations can be set up independently of concolic execution tasks.
This allows to maintain a set of profiles which can be used for several concolic
analyses.

Each analysis configuration must have a unique (among analysis configurations)
name (e.g., `sample`). The JPF configuration prefix for a configuration named
`<name>` is `jdart.configs.<name>` (e.g., `jdart.configs.sample`). In the
remainder of this section, we will refer to this prefix as `<prefix>`.

To specify that a certain analysis configuration should be used in a concolic execution
named `<cm>`, the (non-prefixed) name of the analysis configuration needs to be specified
as the `concolic.methods.<cm>.config` key. For example:
```
#!bash
concolic.method.cm1.config=sample
...
jdart.configs.sample... # analysis config
```

General Configuration
-------------------------------	
**Config key prefix:** `<prefix>`, e.g., `jdart.configs.sample`

* `max_depth` (integer): the maximum depth of the constraints tree.
  The resulting constraints tree will in no case be deeper than this;
  explorations which go beyond this depth will simply not be reflected
  in the tree. A negative value means infinite (default).
* `max_alt_depth` (integer): the maximum depth of *alternation* (i.e.,
  constraint negation) in the constraint tree. At the root of every
  sub-tree which was accessed through a valuation obtained from a constraint
  solver, the alternation depth increases by one. For unexplored nodes
  with an alternation depth larger than this value, no constraint solving
  will be attempted. A negative value means infinite (default).
* `max_nesting_depth` (integer): Native methods cannot be executed concolically.
  Their return values (if dependent on symbolic argument values) will be
  reflected as an (uninterpreted) function in the constraints tree.
  This option bounds the nesting depth of these function expressions. If this
  value is exceeded (i.e., a native call is made to a method with a symbolic
  argument of the specified nesting depth), the symbolic information for the
  return value is discarded altogether.
* `constraints` (`;`-separated list of strings): further constraints to be
  imposed on the exploration. This is an expression over the overall set
  of symbolic variables, which will be enforced for *every* constraint 
  solving step during concolic execution.

## Symbolic Fields Configuration ##
**Config key prefix:** `<prefix>.symbolic`, e.g., `jdart.configs.sample.symbolic`

Without any additional configuration, only named parameters of the targeted
concolic method (and `this`, in case it is an instance method)
are treated symbolically. In order to also treat class and
instance *fields* symbolically, the symbolic configuration has to be adjusted.

Please note that treating an *object* symbolically merely means to *check* if
its fields should be treated symbolically rather than automatically treating
all of its fields symbolically right away.

* `statics` (`;`-separated list of class names): classes whose *static* fields
  to treat symbolically. In this case, the class is treated as an object, i.e.,
  the `static` fields to be made symbolic have to be explicitly included (see below).
* `include` (`;`-separated list of patterns): patterns specifying fields that should
  be treated symbolically. For example, specifying `this.*` here treats all fields of the instance
  corresponding to the method invocation symbolically. `sample.Foo.*` makes all fields
  (which are not `static final`, as this is usually used to specify constants) of
  the class `sample.Foo` symbolic - if it has been listed in `statics` (see above).
* `exclude` (`;`-separated list of patterns): similar to include, but with the opposite effect.
  If a potentially symbolic variable matches one of the patterns, it is **not** treated
  symbolically. This overrides anything specified under `include`.

## Exploration Configuration ##
**Config key prefix:** `<prefix>.exploration`, e.g., `jdart.configs.sample.exploration`

The options in this group allow to steer the *exploration* of a method,
i.e., whether or not JDart will attempt to exercise other paths through this
method. The default behavior is to explore all the time.

The `suspend` and `resume` option values allow to specify patterns of methods
within which to change this behavior. If a method is entered which matches the
`suspend` pattern, exploration is suspended; conversely, if a method is entered
that matches the `resume` pattern, exploration is resumed. Finally, the boolean
`initial` option controls whether or not JDart is exploring initially (the
default is `true`).

Examples:
```bash
jdart.configs.sample.exploration.initial = false # don't explore initially
# Resume exploration in the method sample.Foo.foo(int,boolean)
jdart.configs.sample.exploration.resume = sample.Foo.foo(int,boolean)
# .. but suspend it again in any method with name bar (regardless of arguments)
# and the method baz().
jdart.configs.sample.exploration.resume =\
	sample.Foo.bar(*);\
	sample.Foo.baz()
```

[0]: http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-core
[1]: https://github.com/psycopaths/jConstraints-z3
[2]: https://z3.codeplex.com
[3]: https://github.com/psycopaths
[4]: https://www.aptlab.net/p/CAVA/jdart-tacas-2016-v4
