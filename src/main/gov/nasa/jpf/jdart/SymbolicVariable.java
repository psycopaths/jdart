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

/**
 * Represents a symbolic (primitive) variable. This can either be
 * a (primitive) argument on the stack, a primitive field of some
 * object on the heap (including <code>this</code>), or an
 * element of a primitive array.
 * 
 *
 * @param <T>
 */
public abstract class SymbolicVariable<T> {
  protected final Variable<T> variable;
  
  /**
   * Constructor.
   * @param variable the associated {@link Variable} 
   */
  public SymbolicVariable(Variable<T> variable) {
    this.variable = variable;
  }
  
  public Variable<T> getVariable() {
    return variable;
  }
  
  /**
   * Read the initial 
   * @param initVal
   * @param sf
   */
  public abstract void readInitial(Valuation initVal, StackFrame sf);
  public abstract void apply(Valuation val, StackFrame sf);
  public abstract void addToPC(PostCondition pc);
}