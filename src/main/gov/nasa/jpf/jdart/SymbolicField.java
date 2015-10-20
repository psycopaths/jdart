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
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.jdart.constraints.PostCondition;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.StackFrame;

/**
 * Symbolic variable for a field of an object on the heap.
 * 
 *
 * @param <T>
 */
public class SymbolicField<T> extends SymbolicVariable<T> {
  private final ElementInfo elementInfo;
  private final FieldInfo fieldInfo;
  
  public static <T> SymbolicField<T> create(Variable<T> variable, ElementInfo elementInfo, FieldInfo fieldInfo) {
    return new SymbolicField<T>(variable, elementInfo, fieldInfo);
  }
  
  public SymbolicField(Variable<T> variable, ElementInfo elementInfo, FieldInfo fieldInfo) {
    super(variable);
    this.elementInfo = elementInfo;
    this.fieldInfo = fieldInfo;
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.SymbolicVariable#readInitial(gov.nasa.jpf.constraints.api.Valuation, gov.nasa.jpf.vm.StackFrame)
   */
  @Override
  public void readInitial(Valuation initVal, StackFrame sf) {
    Object value = elementInfo.getFieldValueObject(fieldInfo.getName());
    initVal.setCastedValue(variable, value);
    elementInfo.defreeze();
    elementInfo.setFieldAttr(fieldInfo, variable);
    //elementInfo.freeze();
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.SymbolicVariable#apply(gov.nasa.jpf.constraints.api.Valuation, gov.nasa.jpf.vm.StackFrame)
   */
  @Override
  public void apply(Valuation val, StackFrame sf) {
    T value = val.getValue(variable);
    elementInfo.defreeze();
    ConcolicUtil.setField(elementInfo, fieldInfo, variable.getType(), value);
    elementInfo.setFieldAttr(fieldInfo, variable);
    //elementInfo.freeze();
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.SymbolicVariable#addToPC(gov.nasa.jpf.jdart.constraints.PostCondition)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void addToPC(PostCondition pc) {
    Expression<T> expr = elementInfo.getFieldAttr(fieldInfo, Expression.class);
    if(expr == null)
      expr = Constant.createCasted(variable.getType(), elementInfo.getFieldValueObject(fieldInfo.getName()));
    //System.err.println("Collecting postcondition for " + variable + ": " + expr);
    pc.addCondition(variable, expr);
  }
}