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
package gov.nasa.jpf.jdart.bytecode;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.expressions.NumericCompound;
import gov.nasa.jpf.constraints.expressions.NumericOperator;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.jdart.ConcolicInstructionFactory;
import gov.nasa.jpf.jdart.ConcolicMethodExplorer;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

public class IDIV extends gov.nasa.jpf.jvm.bytecode.IDIV {
  
  @Override
  @SuppressWarnings("unchecked")
  public Instruction execute (ThreadInfo ti) {
    ConcolicMethodExplorer analysis = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    if(analysis == null)
      return super.execute(ti);
    
		StackFrame sf = ti.getTopFrame();

		if(sf.getOperandAttr(0) == null && sf.getOperandAttr(1) == null) {
			return super.execute(ti); 
    } 
    
    boolean symbolicDiv = sf.getOperandAttr() != null;
    
	  ConcolicUtil.Pair<Integer> right = ConcolicUtil.popInt(sf);
	  ConcolicUtil.Pair<Integer> left = ConcolicUtil.popInt(sf);

    if (symbolicDiv) {
      Expression<Boolean>[] constraints = null;
      if(analysis.needsDecisions()) {
        constraints = new Expression[2];
        Constant<Integer> zero = Constant.create(BuiltinTypes.SINT32, 0);
        constraints[0] = NumericBooleanExpression.create(right.symb, NumericComparator.NE, zero);
        constraints[1] = NumericBooleanExpression.create(right.symb, NumericComparator.EQ, zero);
      }
      analysis.decision(ti, this, (right.conc != 0) ? 0 : 1, constraints);
    }
    
    if (right.conc == 0)
			return ti.createAndThrowException("java.lang.ArithmeticException", "division by zero");
    
    NumericCompound<Integer> symb = new NumericCompound<Integer>(
            left.symb, NumericOperator.DIV, right.symb);    
    
    int conc = left.conc / right.conc;    
    
    ConcolicUtil.Pair<Integer> result = new ConcolicUtil.Pair<Integer>(conc, symb);
    ConcolicUtil.pushInt(result, sf);

    if (ConcolicInstructionFactory.DEBUG) ConcolicInstructionFactory.logger.finest("Execute IDIV: " + result);		
    return getNext(ti);
  }    
}
