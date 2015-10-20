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

import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.ValuationEntry;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.types.Type;
import gov.nasa.jpf.jdart.ConcolicUtil;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 *
 */
public class ConcolicValuesFromConfig extends ConcolicValues {
    
  private final LinkedList<Valuation> vals = new LinkedList<>();

  public ConcolicValuesFromConfig(MethodSpec spec, String text) {
    if (text == null) {
      return;
    }
    text = text.trim();
    StringTokenizer tok = new StringTokenizer(text, ";");
    while (tok.hasMoreTokens()) {
      String valString = tok.nextToken().trim();
      Valuation vltn = parseValuation(valString, spec);
      this.vals.add(vltn);
    }
  }  

  @Override
  public boolean hasNext() {
    return !vals.isEmpty();
  }

  @Override
  public Valuation next() {
    return vals.poll();
  }
}
