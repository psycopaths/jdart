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
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.jdart.constraints.PostCondition;
import gov.nasa.jpf.vm.StackFrame;

public class SymbolicParam<T> extends SymbolicVariable<T> {
  
  private final int stackOffset;
  
  public SymbolicParam(Variable<T> variable, int stackOffset) {
    super(variable);
    this.stackOffset = stackOffset;
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.SymbolicVariable#readInitial(gov.nasa.jpf.constraints.api.Valuation, gov.nasa.jpf.vm.StackFrame)
   */
  @Override
  public void readInitial(Valuation initVal, StackFrame sf) {
    Object value = ConcolicUtil.peek(sf, stackOffset, variable.getResultType());
    initVal.setCastedValue(variable, value);
    ConcolicUtil.setOperandAttr(sf, stackOffset, variable.getType(), variable);
  }
  
  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.SymbolicVariable#apply(gov.nasa.jpf.constraints.api.Valuation, gov.nasa.jpf.vm.StackFrame)
   */
  @Override
  public void apply(Valuation val, StackFrame sf) {
    T value = val.getValue(variable);
    ConcolicUtil.setOperand(sf, stackOffset, variable.getType(), value);
    ConcolicUtil.setOperandAttr(sf, stackOffset, variable.getType(), variable);
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.SymbolicVariable#addToPC(gov.nasa.jpf.jdart.constraints.PostCondition)
   */
  @Override
  public void addToPC(PostCondition pc) {
    // Since Java is call by value, parameter modifications will
    // not be reflected in the postcondition
  }
}