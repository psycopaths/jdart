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
import gov.nasa.jpf.constraints.expressions.NumericCompound;
import gov.nasa.jpf.constraints.expressions.NumericOperator;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.jdart.ConcolicInstructionFactory;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


public class IINC extends gov.nasa.jpf.jvm.bytecode.IINC {
	public IINC(int localVarIndex, int incConstant){
		super(localVarIndex, incConstant);
	}
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame sf = ti.getTopFrame();
    Expression<?> sym_v = ( Expression<?>) sf.getLocalAttr(index);
    sf.setLocalVariable(index, sf.getLocalVariable(index) + increment, false);
    if (sym_v != null) {
    	sf.setLocalAttr(index, NumericCompound.create(
              sym_v.requireAs(BuiltinTypes.SINT32),NumericOperator.PLUS, Constant.create(BuiltinTypes.SINT32, increment)));
    	if (ConcolicInstructionFactory.DEBUG) ConcolicInstructionFactory.logger.finest("IINC " + sf.getLocalAttr(index));
    }
    return getNext(ti);
  }
}
