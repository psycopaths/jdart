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

import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.jdart.ConcolicInstructionFactory;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.jdart.ConcolicUtil.Pair;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Convert double to int ..., value => ..., result
 */
public class D2I extends gov.nasa.jpf.jvm.bytecode.D2I {

  @Override
  public Instruction execute(ThreadInfo ti) {
    StackFrame sf = ti.getTopFrame();

    // value concrete => no symbolic computation needed.
    if (sf.getLongOperandAttr() == null) {
      return super.execute(ti);
    }

    Pair<Double> val = ConcolicUtil.popDouble(sf);
    
    int i = val.conc.intValue();
    CastExpression<Double,Integer> cast = CastExpression.create(val.symb, BuiltinTypes.SINT32);
    
    Pair<Integer> result = new Pair<Integer>(i, cast);
    ConcolicUtil.pushInt(result, sf);
    
    if (ConcolicInstructionFactory.DEBUG) ConcolicInstructionFactory.logger.finest("Execute D2I: " + result);
    return getNext(ti);
  }
}
