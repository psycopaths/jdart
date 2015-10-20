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

import java.io.IOException;
import java.util.List;

/**
 * method configuration
 */
public class ConcolicMethodConfig {
  
  public static ConcolicMethodConfig read(String id, String prefix, Config config) {
    String methodSpec = config.getProperty(prefix);
    MethodSpec ms = MethodSpec.parse(methodSpec);
    
    String location = config.getProperty(prefix + ".location");
    
    String configId = config.getProperty(prefix + ".config");
    
    AnalysisConfig ac = AnalysisConfig.read("jdart.configs." + configId, config);
    
    
    String valConf = config.getProperty(prefix + ".values");   
    ConcolicValues values = new ConcolicValuesFromConfig(ms, valConf);
    
    if (config.containsKey(prefix + ".valfile")) {
      values = new ConcolicValuesFromFile(config.getProperty(prefix + ".valfile"), ms);
    }
    
    return new ConcolicMethodConfig(id, ms, location, ac, values);
  }
  
//
//  public static ConcolicMethodConfig create(String id, MethodSpec methodSpec) {
//    return new ConcolicMethodConfig(null, null, id, methodSpec, null, null);
//  }
  
  private final String id;
  private MethodSpec methodSpec;
  private String location;
  private AnalysisConfig analysisConfig;
  private final ConcolicValues concValues;
  
  public ConcolicMethodConfig(String id, MethodSpec methodSpec, String location, AnalysisConfig analysisConfig, ConcolicValues vals) {
    this.id = id;
    this.methodSpec = methodSpec;
    this.location = location;
    this.analysisConfig = analysisConfig;
    this.concValues = vals;
  }
  
  public String getId() {
    return id;
  }
  
  public AnalysisConfig getAnalysisConfig() {
    return analysisConfig;
  }
  
  public void setAnalysisConfig(AnalysisConfig ac) {
    this.analysisConfig = ac;
  }
  
  /**
   * @return the className
   */
  public String getClassName() {
    return methodSpec.getClassName();
  }
  

  /**
   * @return the methodName
   */
  public String getMethodName() {
    return methodSpec.getMethodName();
  }

  public List<ParamConfig> getParams() {
    return methodSpec.getParams();
  }
  
  public void setMethod(MethodSpec methodSpec) {
    this.methodSpec = methodSpec;
  }
  
  public String getLocation() {
    return location;
  }
  
  public void setLocation(String location) {
    this.location = location;
  }
  
  public void printJPFPerturb(Appendable a) throws IOException {
    print(a, false);
  }
  
  public String toJPFPerturbString() {
    StringBuilder sb = new StringBuilder();
    try {
      printJPFPerturb(sb);
      return sb.toString();
    }
    catch(IOException ex) {
      throw new RuntimeException(ex); // SHOULD NOT HAPPEN
    }
  }
  
  public void print(Appendable a, boolean includeNames) throws IOException {
    methodSpec.print(a, includeNames);
    if(location != null)
      a.append('@').append(location);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    try {
      print(sb, true);
      return sb.toString();
    }
    catch(IOException ex) {
      throw new RuntimeException(ex); // SHOULD NOT HAPPEN
    } 
  }

  public static ConcolicMethodConfig create(String id, MethodSpec methodSpec,
      AnalysisConfig ac) {
    return new ConcolicMethodConfig(id, methodSpec, null, ac, new ConcolicValuesFromConfig(methodSpec, null));
  }

  public ConcolicValues getConcolicValues() {
    return this.concValues;
  }
}