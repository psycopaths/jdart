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
package gov.nasa.jpf.jdart.summaries;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jdart.config.ConcolicMethodConfig;
import java.util.ArrayList;
import java.util.Collection;

/**
 * configuration for summary generation.
 */
public class SummaryConfig {
  
  private final Collection<ConcolicMethodConfig> summaryMethods =  new ArrayList<>();
  
  public SummaryConfig(Config conf) {
    if (conf.containsKey("summary.methods")) {
      for (String id : conf.getStringSet("summary.methods")) {
        summaryMethods.add(ConcolicMethodConfig.read(
                id, "concolic.method." + id, conf));
      }
    }
  }
  
  public Collection<ConcolicMethodConfig> getSummaryMethods() {
    return summaryMethods;
  }
   
}
