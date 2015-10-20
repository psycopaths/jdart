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
import gov.nasa.jpf.constraints.expressions.LogicalOperator;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.expressions.PropositionalCompound;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.jdart.ConcolicMethodExplorer;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.jdart.ConcolicUtil.Pair;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

public abstract class SwitchHelper {
  
  public static interface SwitchInstruction {
    public Instruction executeConcrete(ThreadInfo ti);
    public int chooseTarget(int concreteValue);
    public int getTargetValue(int targetIdx);
    public int getTargetPC(int targetIdx);
    public int getNumTargets();
  }
  
  public static <I extends Instruction & SwitchInstruction> Instruction execute(I si, ThreadInfo ti) {
    ConcolicMethodExplorer analysis = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    if(analysis == null)
      return si.executeConcrete(ti);
    
    StackFrame sf = ti.getModifiableTopFrame();
    if(sf.getOperandAttr() == null)
      return si.executeConcrete(ti);
    
    Pair<Integer> val = ConcolicUtil.popInt(sf);
    
    Expression<Boolean>[] decisionExprs = null;
    
    if(analysis.needsDecisions()) {
      decisionExprs = buildDecisions(si, val.symb);
    }
    
    int idx = si.chooseTarget(val.conc.intValue());
    int targetPc = si.getTargetPC(idx);
    int symbBranch = (idx == gov.nasa.jpf.jvm.bytecode.SwitchInstruction.DEFAULT) ? si.getNumTargets() : idx;
    
    analysis.decision(ti, si, symbBranch, decisionExprs);
    return sf.getMethodInfo().getInstructionAt(targetPc);
  }
  
  @SuppressWarnings("unchecked")
  private static Expression<Boolean>[] buildDecisions(SwitchInstruction si, Expression<Integer> symbExpr) {
    int numTargets = si.getNumTargets();
    Expression<Boolean>[] result = new Expression[si.getNumTargets() + 1];
    Expression<Boolean> defaultExpr = null;
    for(int i = 0; i < numTargets; i++) {
      int tgtVal = si.getTargetValue(i);
      Constant<Integer> c = Constant.create(BuiltinTypes.SINT32, tgtVal);
      Expression<Boolean> posExpr = NumericBooleanExpression.create(symbExpr, NumericComparator.EQ, c);
      result[i] = posExpr;
      Expression<Boolean> negExpr = NumericBooleanExpression.create(symbExpr, NumericComparator.NE, c);
      if(defaultExpr == null)
        defaultExpr = negExpr;
      else
        defaultExpr = new PropositionalCompound(defaultExpr, LogicalOperator.AND, negExpr);
    }
    result[numTargets] = defaultExpr;
    
    return result;
  }

}
