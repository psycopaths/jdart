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
import gov.nasa.jpf.constraints.expressions.Negation;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.jdart.ConcolicInstructionFactory;
import gov.nasa.jpf.jdart.ConcolicMethodExplorer;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.jdart.constraints.NumericCMP;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.Arrays;

public abstract class IFHelper {

  private IFHelper() {
    // prevent inheritance
  }
  
  
  @SuppressWarnings("unchecked")
  public static Instruction execute(IfInstruction insn, NumericComparator cmp, ThreadInfo ti) {
    ConcolicMethodExplorer analysis = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    
    if(analysis == null) {
      return null;
    }
    
    StackFrame sf = ti.getModifiableTopFrame();
    Expression<?> exp = (Expression<?>) sf.getOperandAttr();
    if(exp == null) { 
      return null;
    }
    
    ConcolicUtil.Pair<Integer> v1 = ConcolicUtil.popInt(sf);
    

    boolean sat = cmp.eval(v1.conc);
    
    if((cmp == NumericComparator.EQ || cmp == NumericComparator.NE) && BuiltinTypes.BOOL.equals(exp.getType())) {
      // special case: boolean expressions    
      Expression<Boolean>[] constraints = null;
      if(analysis.needsDecisions()) {
        constraints = new Expression[2];
        constraints[0] = exp.requireAs(BuiltinTypes.BOOL);
        constraints[1] = exp.requireAs(BuiltinTypes.BOOL);
        int neg = (cmp == NumericComparator.EQ) ? 0 : 1;
        constraints[neg] = new Negation(constraints[neg]);
      }
      int branchIdx = sat ? 0 : 1;
      
      
      analysis.decision(ti, insn, branchIdx, constraints);
      return sat ? insn.getTarget() : insn.getNext(ti);
    }
    

    Expression<Boolean>[] constraints = null;
    if(analysis.needsDecisions()) {
      constraints = new Expression[2];
      if(v1.symb instanceof NumericCMP) {
        // in this case we have to turn the cmp together with the ifne into
        // a path condition ...
        NumericCMP ncmp = (NumericCMP)v1.symb;
        constraints[0] = new NumericBooleanExpression(ncmp.getLeft(), cmp, ncmp.getRight());
        constraints[1] = new NumericBooleanExpression(ncmp.getLeft(), cmp.not(), ncmp.getRight());
      }
      else {
        // this is really a comparison against 0
        Constant<Integer> zero = Constant.create(BuiltinTypes.SINT32, 0);
        constraints[0] = new NumericBooleanExpression(v1.symb, cmp, zero);
        constraints[1] = new NumericBooleanExpression(v1.symb, cmp.not(), zero);
      }
      // System.err.println("Comparison against zero: ");
      // System.err.println(Arrays.asList(constraints));
      // System.err.println("Satisfied: " + sat);
      // System.err.println("Value is " + v1.conc);
    }
    
    int branchIdx = sat ? 0 : 1;
    
    // ADD SYMBOLIC CONSTRAINT TO PATH CONDITION !!!
    analysis.decision(ti, insn, branchIdx, constraints);

    if (ConcolicInstructionFactory.DEBUG) ConcolicInstructionFactory.logger.finest("Execute IFEQ: " + v1.conc + " [" + v1.symb + "], symb. result  [" + sat  + "]");    
    return sat ? insn.getTarget() : insn.getNext(ti);           
    
  }

}
