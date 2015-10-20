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
import gov.nasa.jpf.constraints.expressions.functions.Function;
import gov.nasa.jpf.constraints.expressions.functions.FunctionExpression;
import gov.nasa.jpf.jdart.bytecode.EXECUTENATIVE.SymbolicNativeMethod;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.NativeStackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

public class NATIVERETURN extends gov.nasa.jpf.jvm.bytecode.NATIVERETURN {

  /* (non-Javadoc)
   * @see gov.nasa.jpf.jvm.bytecode.NATIVERETURN#execute(gov.nasa.jpf.vm.ThreadInfo)
   */
  @Override
  public Instruction execute(ThreadInfo ti) {
    Expression<?>[] symArgs = ti.getTopFrame().getFrameAttr(Expression[].class);
    
    if(symArgs != null) {
      // We only store the symbolic arguments if the native method has
      // symbolic information, so no need to check
      SymbolicNativeMethod snm = mi.getAttr(SymbolicNativeMethod.class);
      Function<?> fn = snm.getFunction();
      Expression<?> symReturn = new FunctionExpression<>(fn, symArgs);
      NativeStackFrame sf = (NativeStackFrame)ti.getTopFrame();
      if(sf.getReturnAttr() == null) // if this is non-null, peer takes care of this!
        sf.setReturnAttr(symReturn);
      else if(!(sf.getReturnAttr() instanceof Expression))
        sf.setReturnAttr(null);
    }
    return super.execute(ti);
  }

}
