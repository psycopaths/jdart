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

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.jdart.constraints.PostCondition;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.StackFrame;

public class SymbolicArrayElem<T> extends SymbolicVariable<T> {
  
  private final ElementInfo arrayElem;
  private final int slotId;
  
  public SymbolicArrayElem(Variable<T> var, ElementInfo arrayElem, int slotId) {
    super(var);
    this.arrayElem = arrayElem;
    this.slotId = slotId;
  }

  @Override
  public void readInitial(Valuation initVal, StackFrame sf) {
    Object value = ConcolicUtil.getArrayElement(arrayElem, slotId, variable.getType());
    initVal.setCastedValue(variable, value);
    arrayElem.defreeze();
    arrayElem.setElementAttr(slotId, variable);
    arrayElem.freeze(); //TODO: Kasper: not sure if we should freeze again
  }

  @Override
  public void apply(Valuation val, StackFrame sf) {
    T value = val.getValue(variable);
    arrayElem.defreeze();
    ConcolicUtil.setArrayElement(arrayElem, slotId, variable.getType(), value);
    arrayElem.setElementAttr(slotId, variable);
    arrayElem.freeze(); //TODO: Kasper: not sure if we should freeze again
  }

  @Override
  @SuppressWarnings("unchecked")
  public void addToPC(PostCondition pc) {
    Expression<T> expr = arrayElem.getElementAttr(slotId, Expression.class);
    pc.addCondition(variable, expr);
  }
  
}