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

/**
 *
 */
public abstract class ConcolicValues {
  
  public abstract boolean hasNext();
  
  public abstract Valuation next();
  
  protected static Valuation parseValuation(String text, MethodSpec spec) {
    text = text.trim();
    String[] values = text.split(",");
    Valuation vltn = new Valuation();
    assert values.length == spec.getParams().size();
      for (int i=0; i<values.length; i++) {
        ParamConfig pc = spec.getParams().get(i);
        Type t = ConcolicUtil.forName(pc.getType());
        Variable v = Variable.create(t, pc.getName());
        Object value = ConcolicUtil.fromString(t, values[i].trim());
        vltn.addEntry(new ValuationEntry<>(v, value));
      }
      return vltn;
  }
  
}
