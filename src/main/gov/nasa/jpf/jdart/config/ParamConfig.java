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

import java.io.IOException;

public final class ParamConfig {
  
  public static ParamConfig parse(String pspec) {
    String[] pspecComps = pspec.split("\\s*:\\s*", 2);
    String name = null;
    String type;
    if(pspecComps.length == 1) {
      type = pspecComps[0];
    }
    else {
      name = pspecComps[0];
      type = pspecComps[1];
    }
    return new ParamConfig(type, name);
  }
  
  private final String type;
  private final String name;
  
  
  public ParamConfig(String type) {
    this(type, null);
  }
  
  
  public ParamConfig(String type, String name) {
    this.type = type;
    this.name = name;
  }
  
  public String getType() {
    return type;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean isSymbolic() {
    return (name != null);
  }
  
  public void print(Appendable a) throws IOException {
    if(name != null)
      a.append(name).append(':');
    a.append(type);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    try {
      print(sb);
      return sb.toString();
    }
    catch(IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}