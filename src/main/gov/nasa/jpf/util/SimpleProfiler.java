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
package gov.nasa.jpf.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 */
public class SimpleProfiler {

  public static final boolean PROFILE = true;
  
  public static final Map<String,Long> cumulated = new HashMap<String, Long>();
  public static final Map<String,Long> pending = new HashMap<String, Long>();
          
  
  public static void start(String name) {
    if (!PROFILE) {
      return;
    }
    long start = System.currentTimeMillis();
  
    pending.put(name,start);
    
  }
  
  public static void stop(String name) {
    if (!PROFILE) {
      return;
    }
    Long start = pending.remove(name);
    if (start == null) {
      return;
    }
    long duration = System.currentTimeMillis() - start;
    Long sum = cumulated.get(name);
    if (sum == null) {
      sum = (long)0;
    }
    cumulated.put(name, sum + duration);
  }
  
  
  public static String getResults() {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, Long> e : cumulated.entrySet()) {
      sb.append(e.getKey()).append(": ").append(e.getValue()).append(" ms [").append(e.getValue()/1000).append(" s]\n");
    }
    return sb.toString();
  }
  
}
