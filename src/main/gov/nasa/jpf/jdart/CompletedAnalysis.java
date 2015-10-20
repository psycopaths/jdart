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
package gov.nasa.jpf.jdart;

import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.jdart.config.ConcolicMethodConfig;
import gov.nasa.jpf.jdart.constraints.ConstraintsTree;

import java.io.IOException;

public class CompletedAnalysis {
  
  private final ConcolicMethodConfig methodConfig;
  private final Valuation initialValuation;
  private final ConstraintsTree constraintsTree;
  
  private final Object[] initParams;

  public CompletedAnalysis(ConcolicMethodConfig methodConfig, Valuation initialValuation, Object[] initParams, ConstraintsTree constraintsTree) {
    this.methodConfig = methodConfig;
    this.initialValuation = initialValuation;
    this.constraintsTree = constraintsTree;
    this.initParams = initParams;
  }

  
  public ConcolicMethodConfig getMethodConfig() {
    return methodConfig;
  }
  
  public Valuation getInitialValuation() {
    return initialValuation;
  }
  
  public ConstraintsTree getConstraintsTree() {
    return constraintsTree;
  }
  
  
  public void print(Appendable a) throws IOException {
    a.append("Initial valuation: ");
    initialValuation.print(a);
    a.append('\n');
    a.append("Constraints tree:\n");
    a.append(constraintsTree.toString(false, true));
  }
  
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


  public Object[] getInitParams() {
    return initParams;
  }
  
}
