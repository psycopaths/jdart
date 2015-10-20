/*
 * Copyright (C) 2015, United States Government, as represented by the 
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The PSYCO: A Predicate-based Symbolic Compositional Reasoning environment 
 * platform is licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0. 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package gov.nasa.jpf.jdart.config;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.MethodInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.JPFLogger;

public class AnalysisConfig {
  
  private static transient JPFLogger logger = JPF.getLogger("jdart");
    
  public static Predicate<MethodInfo> methodPatternFromString(String patternStr) {
    final Pattern pat = patternFromString(patternStr);
    
    return new Predicate<MethodInfo>() {
      @Override
      public boolean apply(MethodInfo mi) {
        StringBuilder niceMethName = new StringBuilder();
        niceMethName.append(mi.getClassName());
        niceMethName.append('.');
        niceMethName.append(mi.getName());
        niceMethName.append('(');
        String[] paramTypes = mi.getArgumentTypeNames();
        for(int i = 0; i < paramTypes.length; i++) {
          if(i != 0)
            niceMethName.append(',');
          niceMethName.append(paramTypes[i]);
        }
        niceMethName.append(')');
        String str = niceMethName.toString();
        return pat.matcher(str).matches();
      }
    };
  }
  public static Pattern patternFromString(String patternStr) {
    String[] subPats = patternStr.trim().split("\\s*;\\s*");
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    sb.append("^(");
    for(String sp : subPats) {
      if(first)
        first = false;
      else
        sb.append("|");

      sb.append(sp.replace(".", "\\.").replace("(", "\\(").replace("[","\\[").replace("*", ".*").replace("?", "."));
    }
    sb.append(")$");
    String regex = sb.toString();
    logger.finest("Regex is " + regex);
    return Pattern.compile(regex);
  }
  
  public static Predicate<String> predicateFromString(String patternStr) {
    final Pattern pat = patternFromString(patternStr);
    return new Predicate<String>() {
      @Override
      public boolean apply(String arg) {
        return pat.matcher(arg).matches();
      }
    };
  }
  
  
  public static AnalysisConfig read(String string, Config config) {
    AnalysisConfig ac = new AnalysisConfig();
    ac.parse(string, config);
    return ac;
  }
    
  private final List<String> symbolicStatics = new ArrayList<>();
  private final List<String> constraints = new ArrayList<>();
  
  private Predicate<? super String> symbFieldExclude = Predicates.alwaysFalse();
  private Predicate<? super String> symbFieldInclude = Predicates.alwaysFalse();
  private Predicate<? super ClassInfo> specialExclude = Predicates.alwaysFalse();
  
  private Predicate<? super MethodInfo> suspendExploration = Predicates.alwaysFalse();
  private Predicate<? super MethodInfo> resumeExploration = Predicates.alwaysFalse();
  private boolean exploreInitially = true;
  
  private int treeMaxDepth = -1;
  private int treeMaxAltDepth = -1;
  private int maxNestingDepth = -1;

  private boolean loadAxioms = false;
  
  public void parse(String prefix, Config config) {
    String maxDepthKey = prefix + ".max_depth";
    if(config.hasValue(maxDepthKey)) {
      this.treeMaxDepth = config.getInt(maxDepthKey);
    }
    String maxAltDepthKey = prefix + ".max_alt_depth";
    if(config.hasValue(maxAltDepthKey)) {
      this.treeMaxAltDepth = config.getInt(maxAltDepthKey);
    }
    
    String maxNestDepthKey = prefix + ".max_nesting_depth";
    if(config.hasValue(maxNestDepthKey)) {
      this.maxNestingDepth = config.getInt(maxNestDepthKey);
    }
    
    String constrKey = prefix + ".constraints";
    if(config.hasValue(constrKey)) {
      String constraintsStr = config.getString(constrKey);
      setConstraints(constraintsStr);
    }

    String fdefKey = prefix + ".use_func_defs";
    if(config.hasValue(fdefKey)) {
      this.loadAxioms = config.getBoolean(fdefKey);
    }
    
    String explPrefix = prefix + ".exploration.";
    String explInitiallyKey = explPrefix + "initial";
    if(config.hasValue(explInitiallyKey)) {
      this.exploreInitially = config.getBoolean(explInitiallyKey);
    }
    String suspendKey = explPrefix + "suspend";
    if(config.hasValue(suspendKey)) {
      setSuspendExploration(config.getString(suspendKey));
    }
    String resumeKey = explPrefix + "resume";
    if(config.hasValue(resumeKey)) {
      setResumeExploration(config.getString(resumeKey));
    }
    
    
    String symbPrefix = prefix + ".symbolic.";
    String symbStaticKey = symbPrefix + "statics";
    if(config.hasValue(symbStaticKey)) {
      String symbStaticStr = config.getString(symbStaticKey);
      setSymbolicStatics(symbStaticStr);
    }
    
    String symbExclKey = symbPrefix + "exclude";
    if(config.hasValue(symbExclKey)) {
      String symbExclStr = config.getString(symbExclKey);
      setSymbolicFieldsExclude(symbExclStr);
    }
    
    String symbInclKey = symbPrefix + "include";
    if(config.hasValue(symbInclKey)) {
      String symbInclStr = config.getString(symbInclKey);
      setSymbolicFieldsInclude(symbInclStr);
    }
    
  }
  
  public int getTreeMaxDepth() {
    return treeMaxDepth;
  }
  
  public void setTreeMaxDepth(int treeMaxDepth) {
    this.treeMaxDepth = treeMaxDepth;
  }
  
  public boolean maxDepthExceeded(int depth) {
    if(treeMaxDepth < 0)
      return false;
    return (depth > treeMaxDepth);
  }
  
  public int getTreeMaxAltDepth() {
    return treeMaxAltDepth;
  }
  
  public void setTreeMaxAltDepth(int treeMaxAltDepth) {
    this.treeMaxAltDepth = treeMaxAltDepth;
  }
  
  public boolean maxAltDepthExceeded(int altDepth) {
    if(treeMaxAltDepth < 0)
      return false;
    return (altDepth > treeMaxAltDepth);
  }
  
  public void setConstraints(List<String> constraints) {
    this.constraints.clear();
    this.constraints.addAll(constraints);
  }
  
  public void setConstraints(String constraintsStr) {
    String[] elems = constraintsStr.trim().split("\\s*;\\s*");
    setConstraints(Arrays.asList(elems));
  }
  
  public void addConstraint(String constraint) {
    this.constraints.add(constraint);
  }
  
  public void addConstraints(List<String> constraints) {
    this.constraints.addAll(constraints);
  }
  
  public List<String> getConstraints() {
    return Collections.unmodifiableList(this.constraints);
  }
  
  public void setSymbolicStatics(List<String> symbolicStatics) {
    this.symbolicStatics.clear();
    this.symbolicStatics.addAll(symbolicStatics);
  }
  
  public void setSymbolicStatics(String symbolicStaticString) {
    String[] classes = symbolicStaticString.trim().split("\\s*,\\s*");
    this.symbolicStatics.clear();
    this.symbolicStatics.addAll(Arrays.asList(classes));
  }
  
  public void addSymbolicStatic(String symbolicStatic) {
    this.symbolicStatics.add(symbolicStatic);
  }
  
  public void addSymbolicStatics(List<String> symbolicStatics) {
    this.symbolicStatics.addAll(symbolicStatics);
  }
  
  public void clearSymbolicStatics() {
    this.symbolicStatics.clear();
  }
  
  public List<String> getSymbolicStatics() {
    return Collections.unmodifiableList(symbolicStatics);
  }
  
  public void setSymbolicFieldsExclude(Predicate<? super String> symbFieldsExclude) {
    this.symbFieldExclude = symbFieldsExclude;
  }
  
  public void setSymbolicFieldsExclude(String symbFieldsExcludeStr) {
    this.symbFieldExclude = predicateFromString(symbFieldsExcludeStr);
  }
  
  public Predicate<? super String> getSymbolicFieldsExclude() {
    return this.symbFieldExclude;
  }
  
  public void setSymbolicFieldsInclude(Predicate<? super String> symbFieldsInclude) {
    this.symbFieldInclude = symbFieldsInclude;
  }
  
  public void setSymbolicFieldsInclude(String symbFieldsInclude) {
    this.symbFieldInclude = predicateFromString(symbFieldsInclude);
  }
  
  public Predicate<? super String> getSymbolicFieldsInclude() {
    return symbFieldInclude;
  }
  
  public void setSpecialExclude(Predicate<? super ClassInfo> specialExclude) {
    this.specialExclude = specialExclude;
  }
  
  public Predicate<? super ClassInfo> getSpecialExclude() {
    return specialExclude;
  }
  
  public void setSpecialExclude(String excludeString) {
    final Pattern pat = patternFromString(excludeString);
    this.specialExclude = new Predicate<ClassInfo>() {
      @Override
      public boolean apply(ClassInfo ci) {
        return pat.matcher(ci.getName()).matches();
      }
    };
  }

  public boolean hasMaxNestingDepth() {
    return maxNestingDepth >= 0;
  }
  
  public boolean maxNestingDepthExceeded(int depth) {
    if(maxNestingDepth < 0)
      return false;
    return (depth > maxNestingDepth);
  }
  
  public boolean isExploreInitially() {
    return exploreInitially;
  }
  
  public void setSuspendExploration(String str) {
    Predicate<MethodInfo> pred = methodPatternFromString(str);
    this.suspendExploration = pred;
  }
  
  public void setResumeExploration(String str) {
    Predicate<MethodInfo> pred = methodPatternFromString(str);
    this.resumeExploration = pred;
  }
  
  public void setSuspendExploration(Predicate<? super MethodInfo> pred) {
    this.suspendExploration = pred;
  }
  
  public void setResumeExploration(Predicate<? super MethodInfo> pred) {
    this.resumeExploration = pred;
  }

  public boolean suspendExploration(MethodInfo mi) {
    return suspendExploration.apply(mi);
  }
  
  public boolean resumeExploration(MethodInfo mi) {
    return resumeExploration.apply(mi);
  }

  public boolean loadFunctionDefintions() {
    return loadAxioms;
  }

}
