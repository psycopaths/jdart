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
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.ConstraintSolver;
import gov.nasa.jpf.constraints.solvers.ConstraintSolverFactory;
import gov.nasa.jpf.constraints.types.TypeContext;
import gov.nasa.jpf.jdart.ConcolicPerturbator;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.jdart.termination.NeverTerminate;
import gov.nasa.jpf.jdart.termination.TerminationStrategy;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for the concolic execution
 */
public class ConcolicConfig {

  /**
   * keys used in configuration
   */
  private static final String CONF_PREFIX = "concolic";
  
  /**
   * constraint solver used by method explorers
   */
  private ConstraintSolver solver;
  
  private TypeContext types = new TypeContext(true);
  
  protected final AnalysisConfig globalConfig = new AnalysisConfig();
  
  private final Map<String,ConcolicMethodConfig> concolicMethods
    = new HashMap<>();
  
    
  /**
   * strategy for terminating jdart
   */
  private TerminationStrategy termination;
  
  /**
   * 
   * @param conf 
   */  
  
  public ConcolicConfig() {
    
  }
  public ConcolicConfig(Config conf) {
    initialize(conf);
  }
  
  /* ******************************************************************************
   * 
   * api
   * 
   */  
  
  public ConcolicConfig(ConcolicConfig other) {
    this.solver = other.solver;
    this.concolicMethods.putAll(other.concolicMethods);
    this.termination = other.termination;
  }

  /**
   * get method config for concolic method
   * 
   * @return 
   */
  public ConcolicMethodConfig getMethodConfig(String id) {
    ConcolicMethodConfig mc = concolicMethods.get(id);
    return mc;
  }
  
  public Collection<ConcolicMethodConfig> getMethodConfigs() {
    return concolicMethods.values();
  }
  
  public void setConstraintSolver(ConstraintSolver solver) {
    this.solver = solver;
  }
  
  /**
   * @return the solver
   */
  public ConstraintSolver getSolver() {
    return solver;
  }
  
  public TypeContext getTypes() {
    return types;
  }
  
  public TerminationStrategy getTerminationStrategy() {
    return this.termination;
  }
  
  public Config generateJPFConfig() {
    return generateJPFConfig(null);
  }
    
  /** 
   * generates a jpf config corresponding to this configuration
   * 
   * @param conf
   * @return 
   */
  public Config generateJPFConfig(Config conf) {
    Config newConf = new Config("");
    if(conf != null) {
      newConf.putAll(conf);
      newConf.setClassLoader(conf.getClassLoader());
    }
    else {
      newConf.initClassLoader(JPF.class.getClassLoader());
    }
    
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for(ConcolicMethodConfig mc : concolicMethods.values()) {
      generatePerturbConfig(mc, newConf);
      if(first)
        first = false;
      else
        sb.append(',');
      sb.append(mc.getId());
    }
    
    newConf.setProperty("perturb.params", sb.toString());
    
    return newConf;
  }  
  
  
  private static void generatePerturbConfig(ConcolicMethodConfig mc, Config conf) {
    String perturbPrefix = "perturb." + mc.getId() + ".";
    String perturbMethod = mc.toJPFPerturbString();
    
    conf.setProperty(perturbPrefix + "method", perturbMethod);
    conf.setProperty(perturbPrefix + "class", ConcolicPerturbator.class.getName());
    
    String loc = mc.getLocation();
    if(loc != null)
      conf.setProperty(perturbPrefix + "location", loc);
  }
  

  public void registerConcolicMethod(ConcolicMethodConfig mc) {
    concolicMethods.put(mc.getId(), mc);
  }
  
  public void addConcolicMethod(String id, MethodSpec methodSpec, AnalysisConfig ac) {
    if(ac == null)
      ac = globalConfig;
    ConcolicMethodConfig cm = ConcolicMethodConfig.create(id, methodSpec, ac);
    registerConcolicMethod(cm);
  }
  
  public void addConcolicMethod(String id, String className, String methodName, AnalysisConfig ac, ParamConfig ...params) {
    addConcolicMethod(id, new MethodSpec(className, methodName, params), ac);
  }
  
  public void addConcolicMethod(String id, String methodSpec, AnalysisConfig ac) {
    addConcolicMethod(id, MethodSpec.parse(methodSpec), ac);
  }
  
  public void clearConcolicMethods() {
    concolicMethods.clear();
  }
  
  /* ******************************************************************************
   * 
   * private helpers
   * 
   */  
  
  
  /**
   * initialize object from config file
   * 
   * @param conf 
   */
  private void initialize(Config conf) {
    // create a constraint solver
    ConstraintSolverFactory cFactory = new ConstraintSolverFactory(conf);
    this.solver = cFactory.createSolver(conf);
    
    // parse symbolic method info
    if(conf.hasValue(CONF_PREFIX + ".method")) {
      String id = conf.getString(CONF_PREFIX + ".method");
      ConcolicMethodConfig mc = ConcolicMethodConfig.read(id, CONF_PREFIX + ".method." + id, conf);
      registerConcolicMethod(mc);
    }
    
    // parse termination
    this.termination = parseTerminationStrategy(conf);
  }
  
  public static TerminationStrategy parseTerminationStrategy(Config conf) {
    if (!conf.hasValue("jdart.termination")) {
      return new NeverTerminate();     
    }
    return parseTerminationStrategy(conf.getProperty("jdart.termination"));
  }
    
  public static TerminationStrategy parseTerminationStrategy(String line) {
    try {
      String[] opt = line.split("\\,");
      Class clazz = Class.forName(opt[0].trim());
      Object obj = null;
      if (opt.length <= 1) {
        obj = clazz.newInstance();         
      } else {
        Object[] params = new Object[opt.length-1];
        Class<?>[] types = new Class[opt.length-1];
        for (int i=0; i<params.length; i++) {
          types[i] = int.class;
          params[i] = (int) Integer.parseInt(opt[i+1].trim());
        }
        Constructor c = clazz.getConstructor(types);
        obj = c.newInstance(params);
      }
      TerminationStrategy t = (TerminationStrategy)obj;
      return t;
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | 
            NoSuchMethodException | SecurityException | IllegalArgumentException | 
            InvocationTargetException ex) {
      Logger.getLogger("jdart").log(Level.WARNING, 
              "Could not instantiate termination strategy: " + 
              ex.getClass().getSimpleName() + " / " +ex.getMessage(), ex);
    }
    return new NeverTerminate();
  }
  
  
  
  // LEGACY API
  
  
  /**
   * method configuration
   */
  @Deprecated
  public static class MethodConfig {
    private final String className;
    private final String methodName;
    private final Class<?>[] paramTypes;
    private final int hashCode;
    private String[] paramNames;

    public MethodConfig(String className, String methodName,
        Class<?>[] paramTypes) {
      this.className = className;
      this.methodName = methodName;
      this.paramTypes = paramTypes;

      // calculate hashcode (performance optimization, not sure if needed)
      int hash = 3;
      hash = 79 * hash
          + (this.className != null ? this.className.hashCode() : 0);
      hash = 79 * hash
          + (this.methodName != null ? this.methodName.hashCode() : 0);
      hash = 79 * hash + Arrays.deepHashCode(this.paramTypes);
      this.hashCode = hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final MethodConfig other = (MethodConfig) obj;
      if ((this.className == null) ? (other.className != null)
          : !this.className.equals(other.className)) {
        return false;
      }
      if ((this.methodName == null) ? (other.methodName != null)
          : !this.methodName.equals(other.methodName)) {
        return false;
      }
      if (!Arrays.deepEquals(this.paramTypes, other.paramTypes)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return this.hashCode;
    }

    /**
     * @return the className
     */
    public String getClassName() {
      return className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
      return methodName;
    }

    /**
     * @return the paramTypes
     */
    public Class<?>[] getParamTypes() {
      return paramTypes;
    }

    /**
     * @return the paramNames
     */
    public String[] getParamNames() {
      return paramNames;
    }

    @Override
    public String toString() {
      return this.className + "." + this.methodName + "("
          + Arrays.toString(paramNames) + ":" + Arrays.toString(paramTypes)
          + ")";
    }
    
    public MethodSpec toMethodSpec() {
      ParamConfig[] pcs = new ParamConfig[paramTypes.length];
      for(int i = 0; i < pcs.length; i++) {
        pcs[i] = new ParamConfig(paramTypes[i].getSimpleName(), paramNames[i]);
      }
      return new MethodSpec(className, methodName, pcs);
    }
  }
  
  /**
   * parse one line of config into method description
   * 
   * @param configLine
   * @return 
   */
  @Deprecated
  protected MethodConfig parseMethodConfig(String configLine) {
    // gov.nasa.example.Classname.methodname(name:int,name2:java.lang.Integer)    
    String[] split = configLine.split("[\\(\\)]");
    String fullMethodName = split[0].trim();
    String paramString = (split.length > 1) ? split[1].trim() : ",";
    // gov.nasa.example.Classname.methodname
    int idx = fullMethodName.lastIndexOf(".");
    String className = fullMethodName.substring(0, idx).trim();
    String methodName = fullMethodName.substring(idx+1).trim();    
    // name:int,name2:java.lang.Integer
    
    split = paramString.split(",");
    Class<?>[] paramTypes = new Class<?>[split.length];
    String[] paramNames = new String[split.length];
    for (int i=0; i<split.length;i++) {
      paramNames[i] = split[i].substring(0,split[i].indexOf(":")).trim();
      String tName = split[i].substring(split[i].indexOf(":")+1);
      paramTypes[i] = ConcolicUtil.parseType(tName.trim());
    }

    MethodConfig mc = new MethodConfig(className, methodName, paramTypes);
    mc.paramNames = paramNames;
    return mc;
  }
  
  @Deprecated
  public void addConcolicClass(final String className) {
    globalConfig.addSymbolicStatic(className);
    final Predicate<? super String> old = globalConfig.getSymbolicFieldsInclude();
    final Predicate<String> inclPred = new Predicate<String>() {
      @Override
      public boolean apply(String str) {
        return str.endsWith("@" + className) || old.apply(str);
      }
    };
    globalConfig.setSymbolicFieldsInclude(inclPred);
  }

}
