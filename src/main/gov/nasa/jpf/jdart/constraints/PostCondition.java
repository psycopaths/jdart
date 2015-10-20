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
package gov.nasa.jpf.jdart.constraints;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class PostCondition {

  private Map<Variable<?>, Expression<?>> conditions = new HashMap<>();

  public Map<Variable<?>, Expression<?>> getConditions() {
    return conditions;
  }
  
  public <T> void addCondition(Variable<T> var, Expression<T> expression) {
    conditions.put(var, expression);
  }
  
  public <T> void setReturn(Expression<T> expression) {
    Variable<T> retVal = Variable.create(expression.getType(), "return");
    addCondition(retVal, expression);
  }

  public void print(Appendable a) throws IOException {
    a.append("[ ");
    for (Entry<Variable<?>, Expression<?>> e : conditions.entrySet()) {
      a.append(e.getKey().getName()).append(":=").append(String.valueOf(e.getValue())).append(", ");
    }
    a.append(" ]");
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

  
  // LEGACY API
  
  @Deprecated
  @SuppressWarnings({"rawtypes","unchecked"})
  public Map<Variable, Expression> getRawConditions() {
    return (Map<Variable,Expression>)(Map)conditions;
  }
}
