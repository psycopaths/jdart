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
import gov.nasa.jpf.constraints.types.Type;
import gov.nasa.jpf.constraints.util.ExpressionUtil;
import gov.nasa.jpf.jdart.ConcolicMethodExplorer;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.jdart.ConcolicUtil.Pair;
import gov.nasa.jpf.jdart.annotations.SymbolicPeer;
import gov.nasa.jpf.jdart.config.AnalysisConfig;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EXECUTENATIVE extends gov.nasa.jpf.jvm.bytecode.EXECUTENATIVE {
  
  public static class SymbolicNativeParam {
    private final Type<?> type;
    private final int stackOffset;
    
    public SymbolicNativeParam(Type<?> type, int stackOffset) {
      this.type = type;
      this.stackOffset = stackOffset;
    }
    public Type<?> getType() {
      return type;
    }
    public int getStackOffset() {
      return stackOffset;
    }
  }
  
  public static class SymbolicNativeMethod {
    
    private final Function<?> func;
    private final List<SymbolicNativeParam> params;
    
    public SymbolicNativeMethod(String name, Type<?> returnType, List<SymbolicNativeParam> params) {
      if(name == null) {
        this.func = null;
        this.params = null;
      }
      else {
        Type<?>[] paramTypes = new Type<?>[params.size()];
        int i = 0;
        for(SymbolicNativeParam snp : params) {
          paramTypes[i++] = snp.getType();
        }
        this.func = new Function<>(name, returnType, paramTypes);
        this.params = params;
      }
    }
    
    public boolean isNull() {
      return (func == null);
    }
    
    public Function<?> getFunction() {
      return func;
    }
    
    public List<SymbolicNativeParam> getParams() {
      return params;
    }
  }
  
  private static SymbolicNativeMethod NULL_SYMBOLIC_METHOD = new SymbolicNativeMethod(null, null, null);
  
  private static void prepareNativeMethod(NativeMethodInfo nmi) {
    SymbolicNativeMethod snm = nmi.getAttr(SymbolicNativeMethod.class);
    if(snm != null)
      return;
    
    NativePeer natPeer = nmi.getNativePeer();
    Class<?> peerClass = (natPeer != null) ? natPeer.getPeerClass() : null;
    Method peerMethod = nmi.getMethod();
    
    byte ret = nmi.getReturnTypeCode();
    if(ret == Types.T_ARRAY || ret == Types.T_REFERENCE || ret == Types.T_VOID) {
      snm = NULL_SYMBOLIC_METHOD;
    }
    else if((peerClass != null && peerClass.getAnnotation(SymbolicPeer.class) != null)
        || (peerMethod != null && peerMethod.getAnnotation(SymbolicPeer.class) != null)) {
      // This is a symbolic peer!
      snm = NULL_SYMBOLIC_METHOD;
    }
    else {
      Type<?> retType = ConcolicUtil.forTypeCode(ret);
      byte[] argTypes = nmi.getArgumentTypes();
      int ofs = 0;
      List<SymbolicNativeParam> params = new ArrayList<>();
      for(int i = argTypes.length - 1; i >= 0; --i) {
        byte code = argTypes[i];
        if(code == Types.T_ARRAY || code == Types.T_REFERENCE || code == Types.T_VOID)
          continue;
        int siz = Types.getTypeSize(code);
        Type<?> t = ConcolicUtil.forTypeCode(code);
        SymbolicNativeParam param = new SymbolicNativeParam(t, ofs);
        params.add(param);
        ofs += siz;
      }
      if(params.isEmpty()) {
        snm = NULL_SYMBOLIC_METHOD;
      }
      else {
        snm = new SymbolicNativeMethod(nmi.getFullName(), retType, params);
      }
    }
    nmi.setAttr(snm);
  }

  public EXECUTENATIVE(NativeMethodInfo nmi) {
    super(nmi);
    prepareNativeMethod(nmi);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.jvm.bytecode.EXECUTENATIVE#execute(gov.nasa.jpf.vm.ThreadInfo)
   */
  @Override
  public Instruction execute(ThreadInfo ti) {
    ConcolicMethodExplorer cmex = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    SymbolicNativeMethod snm = executedMethod.getAttr(SymbolicNativeMethod.class);
    if(snm == null)
      return super.execute(ti);
    if(snm.isNull() || cmex == null) // of no interest, symbolically
      return super.execute(ti);
    
    
    boolean allConcrete = true;
    
    List<SymbolicNativeParam> params = snm.getParams();
    Pair<?>[] args = new Pair[params.size()];
    
    StackFrame sf = ti.getCallerStackFrame();
    int i = 0;
    for(SymbolicNativeParam snp : params) {
      Pair<?> p = ConcolicUtil.peek(sf, snp.getStackOffset(), snp.getType());
      args[i++] = p;
      if(!p.isConcrete()) {
        allConcrete = false;
      }
    }
    
    Instruction insn = super.execute(ti);
    
    if(!allConcrete) {
      Expression<?>[] symArgs = new Expression[params.size()];
      AnalysisConfig ac = cmex.getAnalysisConfig();
      if (!ac.maxNestingDepthExceeded(1)) {
        int maxNestingDepth = -1;
        for(int j = 0; j < symArgs.length; j++) {
          symArgs[j] = args[j].symb;
          if (ac.hasMaxNestingDepth()) {
            int d = ExpressionUtil.nestingDepth(args[j].symb);
            if(d > maxNestingDepth)
              maxNestingDepth = d;
          }
        }
        // Store symbolic arguments in *native execution frame*
        if(!ac.maxNestingDepthExceeded(maxNestingDepth + 1))
          ti.getTopFrame().setFrameAttr(symArgs);
      }
    }
    return insn;
  }
  
  

}
