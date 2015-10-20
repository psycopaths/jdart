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
package gov.nasa.jpf.jdart.peers;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.jdart.annotations.SymbolicPeer;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.constraints.expressions.functions.math.BooleanDoubleFunction;
import gov.nasa.jpf.constraints.expressions.functions.math.UnaryDoubleFunction;
import gov.nasa.jpf.constraints.expressions.functions.math.UnaryDoubleLongFunction;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.functions.Function;
import gov.nasa.jpf.constraints.expressions.functions.FunctionExpression;

/**
 *
 */
public class JPF_java_lang_Double extends gov.nasa.jpf.vm.JPF_java_lang_Double {

  @MJI
  @SymbolicPeer
  public boolean isNaN__D__Z(MJIEnv env, int clsRef, double a) {
    attachSymbolicReturnFunc(env, BooleanDoubleFunction.DOUBLE_IS_NAN);
    return Double.isNaN(a);
  }
  

  @MJI
  @SymbolicPeer
  public boolean isInfinite__D__Z(MJIEnv env, int rcls, double v) {
    attachSymbolicReturnFunc(env, BooleanDoubleFunction.DOUBLE_IS_INFINITE);
    return Double.isInfinite(v);
  }  

  @MJI
  //@SymbolicPeer
  public long doubleToLongBits__D__J(MJIEnv env, int rcls, double v0) {
    //attachSymbolicReturnFunc(env, UnaryDoubleLongFunction.DOUBLE_TO_LONG_BITS);
    return Double.doubleToLongBits(v0);
  }

  @MJI
  public long doubleToRawLongBits__D__J(MJIEnv env, int rcls, double v0) {
    return Double.doubleToRawLongBits(v0);
  }

  @MJI
  public double longBitsToDouble__J__D(MJIEnv env, int rcls, long v0) {
    return Double.longBitsToDouble(v0);
  }

  @MJI
  public int toString__D__Ljava_lang_String_2(MJIEnv env, int objref, double d) {
    String s = Double.toString(d);    
    return env.newString(s);
  }


  private static void attachSymbolicReturnFunc(MJIEnv env, Function<?> func) {
    Object[] attrs = env.getArgAttributes();
    if (attrs == null) {
      return;
    }
    Object attr0 = attrs[0];
    if (!(attr0 instanceof Expression)) {
      return;
    }
    Expression<?> expr = (Expression<?>) attr0;
    Expression<?> symret = new FunctionExpression<>(func, expr);
    env.setReturnAttribute(symret);
  }
}
