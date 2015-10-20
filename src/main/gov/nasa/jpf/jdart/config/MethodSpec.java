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

import gov.nasa.jpf.vm.Types;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class MethodSpec {
  
  
  public static MethodSpec parse(String methodSpec) {
    return parse(methodSpec, true);
  }
  
  
  public static MethodSpec parse(String methodSpec, boolean qualified) {
    int openParIdx = methodSpec.indexOf('(');
    int closeParIdx = methodSpec.indexOf(')', openParIdx + 1);
    if(openParIdx == -1 || closeParIdx == -1)
      throw new IllegalArgumentException("Invalid method specification " + methodSpec);
    
    String fqmn = methodSpec.substring(0, openParIdx).trim();
    int lastDotIdx = fqmn.lastIndexOf('.');
    if((lastDotIdx != -1) ^ qualified)
      throw new IllegalArgumentException("Invalid method specification " + methodSpec);
    
    String className, methodName;
    if(qualified) {
      className = fqmn.substring(0, lastDotIdx);
      methodName = fqmn.substring(lastDotIdx + 1);
    }
    else {
      className = null;
      methodName = fqmn;
    }
    
    String paramSpecStr = methodSpec.substring(openParIdx + 1, closeParIdx).trim();
    
    ParamConfig[] pconf;
    if(paramSpecStr.isEmpty()) {
      pconf = new ParamConfig[0];
    }
    else {
      String[] paramSpecs = paramSpecStr.split("\\s*,\\s*");
      
      pconf = new ParamConfig[paramSpecs.length];
      
      for(int i = 0; i < paramSpecs.length; i++)
        pconf[i] = ParamConfig.parse(paramSpecs[i]);
    }
    
    return new MethodSpec(className, methodName, pconf);
  }
  
  private final String className;
  private final String methodName;
  private final List<ParamConfig> params;
  
  public MethodSpec(String className, String methodName, ParamConfig ...params) {
    this.className = className;
    this.methodName = methodName;
    this.params = Arrays.asList(params);
  }
  
  public String getClassName() {
    return className;
  }
  
  public boolean isQualified() {
    return (className != null);
  }
  
  public String getMethodName() {
    return methodName;
  }
  
  public List<ParamConfig> getParams() {
    return params;
  }
  
  public void print(Appendable a, boolean includeNames) throws IOException {
    a.append(className).append('.').append(methodName);
    a.append('(');
    boolean first = true;
    for(ParamConfig pc : params) {
      if(first)
        first = false;
      else
        a.append(',');
      if(includeNames)
        pc.print(a);
      else
        a.append(pc.getType());
    }
    a.append(')');
  }
  
  public void printJPFPerturb(Appendable a) throws IOException {
    print(a, false);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    try {
      print(sb, true);
      return sb.toString();
    }
    catch(IOException e) {
      throw new IllegalStateException(e);
    }
  }
  
  public String toJPFPerturbString() {
    StringBuilder sb = new StringBuilder();
    try {
      printJPFPerturb(sb);
      return sb.toString();
    }
    catch(IOException e) {
      throw new IllegalStateException(e);
    }
  }
  
  public String methodSignature() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    for(ParamConfig pc : params)
      sb.append(Types.getTypeSignature(pc.getType(), true));
    sb.append(')');
    return sb.toString();
  }

  public boolean allParamsSymbolic() {
    for(ParamConfig p : params) {
      if(!p.isSymbolic()) {
//        System.err.println("Param " + p + " not symbolic");
        return false;
      }
    }
    return true;
        
  }
}