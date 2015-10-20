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
import gov.nasa.jpf.constraints.expressions.Negation;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.jdart.ConcolicInstructionFactory;
import gov.nasa.jpf.jdart.ConcolicMethodExplorer;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Helper class for symbolic execution of IF_ICMP instructions.
 * 
 *
 */
public class IF_ICMPHelper {

  /**
   * Execute an IF_ICMP instruction. A return value of <tt>null</tt> means concrete execution.
   * @param instruction the instruction to execute
   * @param cmp the comparator to apply
   * @param ti the current thread
   * @return the next instruction after branching, or <tt>null</tt> for concrete execution.
   */
  @SuppressWarnings("unchecked")
  public static Instruction execute(IfInstruction instruction, NumericComparator cmp, ThreadInfo ti) {
    ConcolicMethodExplorer analysis = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    if(analysis == null) {
      return null;
    }
    
    StackFrame sf = ti.getTopFrame();
    
    if (sf.getOperandAttr(0) == null && sf.getOperandAttr(1) == null) {
      return null; // null return value means concrete execution
    }
    
    Expression<?> rsym = sf.getOperandAttr(0, Expression.class);
    Expression<?> lsym = sf.getOperandAttr(1, Expression.class);
    
    
    ConcolicUtil.Pair<Integer> right = ConcolicUtil.popInt(sf);
    ConcolicUtil.Pair<Integer> left = ConcolicUtil.popInt(sf);      

    
    int cmpRes = 0;
    if(left.conc < right.conc)
      cmpRes = -1;
    else if(left.conc > right.conc)
      cmpRes = 1;
    
    Expression<Boolean>[] constraints = null;
    if(analysis.needsDecisions()) {
      constraints = new Expression[2];
      if((cmp == NumericComparator.EQ || cmp == NumericComparator.NE)
          && ((lsym == null || lsym.getType().equals(BuiltinTypes.BOOL))
              && (rsym == null || rsym.getType().equals(BuiltinTypes.BOOL)))) {
        if(lsym != null && rsym != null) { // symbolic / symbolic
          constraints[0] = NumericBooleanExpression.create(lsym, cmp, rsym);
          constraints[1] = NumericBooleanExpression.create(lsym, cmp.not(), rsym);
        }
        else { // symbolic / concrete
          boolean cmpVal;
          Expression<?> sym;
          if(lsym == null) {
            sym = rsym;
            cmpVal = (left.conc != 0);
          }
          else {
            sym = lsym;
            cmpVal = (right.conc != 0);
          }
          Expression<Boolean> bsym = sym.requireAs(BuiltinTypes.BOOL);
          constraints[0] = bsym;
          constraints[1] = bsym;
          int neg = (cmpVal ^ (cmp == NumericComparator.EQ)) ? 0 : 1;
          constraints[neg] = new Negation(constraints[neg]);
        }
      }
      else {
        constraints[0] = NumericBooleanExpression.create(left.symb, cmp, right.symb);
        constraints[1] = NumericBooleanExpression.create(left.symb, cmp.not(), right.symb);
      }
    }
    
    int branchIdx = (cmp.eval(cmpRes)) ? 0 : 1;
    // ADD SYMBOLIC CONSTRAINT TO PATH CONDITION !!!
    analysis.decision(ti, instruction, branchIdx, constraints);

    
    if (ConcolicInstructionFactory.DEBUG) ConcolicInstructionFactory.logger.finest("Execute IF_ICMP: " + left.conc + " [" + left.symb + "] " + cmp + " " + right.conc + " [" + right.symb + "], symb. result  [" + (branchIdx == 0) + "]");
    return (branchIdx == 0) ? instruction.getTarget() : instruction.getNext(ti);         
  }
}
