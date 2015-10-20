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
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.LogicalOperator;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.expressions.PropositionalCompound;
import gov.nasa.jpf.constraints.expressions.UnaryMinus;
import gov.nasa.jpf.constraints.expressions.functions.Function;
import gov.nasa.jpf.constraints.expressions.functions.FunctionExpression;
import gov.nasa.jpf.constraints.expressions.functions.math.MathFunctions;
import static gov.nasa.jpf.constraints.expressions.functions.math.axioms.PropertyBuilder.*;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.constraints.types.Type;
import gov.nasa.jpf.jdart.ConcolicMethodExplorer;
import gov.nasa.jpf.jdart.annotations.SymbolicPeer;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.ThreadInfo;
//public class JPF_java_lang_Math extends NativePeer {//extends gov.nasa.jpf.vm.JPF_java_lang_Math {
public class JPF_java_lang_Math extends gov.nasa.jpf.vm.JPF_java_lang_Math {
  
  
  private static Constant<Double> PLUS_ONE_D = Constant.create(BuiltinTypes.DOUBLE, 1.0);
  private static Constant<Double> MINUS_ONE_D = Constant.create(BuiltinTypes.DOUBLE, -1.0);
  
  private static <T> Expression<T> argAttribute(MJIEnv env, Type<T> type) {
    return argAttribute(env, type, 0);
  }
  
  private static <T> Expression<T> argAttribute(MJIEnv env, Type<T> type, int argIdx) {
    ThreadInfo ti = env.getThreadInfo();
    if(ConcolicMethodExplorer.getCurrentAnalysis(ti) == null)
      return null;
    
    Object[] attrs = env.getArgAttributes();
    if(attrs == null)
      return null;
    Object attr0 = attrs[argIdx];
    if(attr0 == null)
      return null;
    if(!(attr0 instanceof Expression)) {
      Expression<?> expr = (Expression<?>)attr0;
      return expr.requireAs(type);
    }
    return null;
  }
  
  private static void attachSymbolicReturnFunc(MJIEnv env, Function<?> func, Object ... values) {
    Object[] attrs = env.getArgAttributes();
    if(attrs == null)
      return;
    
    Expression<?> expr[] = new Expression<?>[func.getArity()];
    boolean symb = false;
    for (int i=0; i<func.getArity(); i++) {      
      Object attr0 = attrs[i];
      if((attr0 instanceof Expression)) {
        expr[i] = (Expression<?>)attr0;
        symb = true;
      } else if (func.getArity() > 1) { // no need to do this if no symb. values are present
        expr[i] = new Constant( func.getParamTypes()[i], values[i]);
      }
    }
    
    if (!symb) {
      return;
    }
    
    Expression<?> symret = new FunctionExpression<>(func, expr);
    env.setReturnAttribute(symret);
  }
  
  @SuppressWarnings("unchecked")
  private static <T extends Comparable<T>> void checkRange(MJIEnv env, T value, Constant<T> min, Constant<T> max, boolean strict) {
    ThreadInfo ti = env.getThreadInfo();
    ConcolicMethodExplorer analysis = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    if(analysis == null)
      return;
    Object[] attrs = env.getArgAttributes();
    if(attrs == null)
      return;
    Object valAttr = attrs[0];
    if(valAttr == null)
      return;
    Expression<T> valSymb = (Expression<T>)valAttr;
    
    NumericComparator lowCmp = (strict) ? NumericComparator.GT : NumericComparator.GE;
    NumericComparator highCmp = (strict) ? NumericComparator.LT : NumericComparator.LE;
    
    Expression<Boolean>[] constraints = null;
    if(analysis.needsDecisions()) {
      constraints = new Expression[2];
      constraints[0] = new PropositionalCompound(
          new NumericBooleanExpression(valSymb, lowCmp, min), LogicalOperator.AND, NumericBooleanExpression.create(valSymb, highCmp, max));
      constraints[1] = new PropositionalCompound(
          NumericBooleanExpression.create(valSymb, lowCmp.not(), min), LogicalOperator.OR, NumericBooleanExpression.create(valSymb, highCmp.not(), max));
    }
    
    int lcr = value.compareTo(min.getValue());
    int hcr = value.compareTo(max.getValue());
    int branchIdx = (lowCmp.eval(lcr) && highCmp.eval(hcr)) ? 0 : 1;
    
    analysis.decision(ti, null, branchIdx, constraints);
  }
  
  @MJI
  @SymbolicPeer
  public double abs__D__D(MJIEnv env, int clsRef, double a) {
    Expression<Double> asym = argAttribute(env, BuiltinTypes.DOUBLE);
    if(asym != null) {
      Expression<Double> symReturn = ite(gte(asym, new Constant<>(BuiltinTypes.DOUBLE, 0.0)), asym, new UnaryMinus<>(asym));
      env.setReturnAttribute(symReturn);
    }
    return Math.abs(a);
  }
  
  @MJI
  @SymbolicPeer
  public double max__DD__D(MJIEnv env, int clsRef, double x, double y) {
    Expression<Double> xsym = argAttribute(env, BuiltinTypes.DOUBLE, 0);
    Expression<Double> ysym = argAttribute(env, BuiltinTypes.DOUBLE, 1);
    
    if(xsym != null && ysym != null) {
      Expression<Double> symReturn = ite(lt(xsym, ysym), ysym, xsym);
      env.setReturnAttribute(symReturn);
    }
    return Math.max(x, y);
  }
  
  @MJI
  @SymbolicPeer
  public long abs__J__J(MJIEnv env, int clsRef, long a) {
    Expression<Long> asym = argAttribute(env, BuiltinTypes.SINT64);
    if(asym != null) {
      Expression<Long> symReturn = ite(gte(asym, new Constant<>(BuiltinTypes.SINT64, 0L)), asym, new UnaryMinus<>(asym));
      env.setReturnAttribute(symReturn);
    }
    return Math.abs(a);
  }

  @MJI
  @SymbolicPeer
  public double sin__D__D(MJIEnv env, int clsRef, double a) {
    attachSymbolicReturnFunc(env, MathFunctions.SIN);
    return Math.sin(a);
  }

  @MJI
  @SymbolicPeer
  public double cos__D__D(MJIEnv env, int clsRef, double a) {
    attachSymbolicReturnFunc(env, MathFunctions.COS);
    return Math.cos(a);
  }

  @MJI
  @SymbolicPeer
  public double tan__D__D(MJIEnv env, int clsRef, double a) {
    attachSymbolicReturnFunc(env, MathFunctions.TAN);
    return Math.tan(a);
  }

  @MJI
  @SymbolicPeer
  public double asin__D__D(MJIEnv env, int clsRef, double a) {
    checkRange(env, a, MINUS_ONE_D, PLUS_ONE_D, false);
    double res = Math.asin(a);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.ASIN);
    return res;
  }

  @MJI
  @SymbolicPeer
  public double acos__D__D(MJIEnv env, int clsRef, double a) {
    checkRange(env, a, MINUS_ONE_D, PLUS_ONE_D, false);
    double res = Math.acos(a);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.ACOS);
    return res;
  }

  @MJI
  public double atan__D__D(MJIEnv env, int clsRef, double a) {
    double res = Math.atan(a);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.ATAN);
    return Math.atan(a);
  }

  @MJI
  @SymbolicPeer  
  public double exp__D__D(MJIEnv env, int clsRef, double a) {
    double res = Math.exp(a);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.EXP);
    return res;
  }

  @MJI
  @SymbolicPeer
  public double log__D__D(MJIEnv env, int clsRef, double a) {
    double res = Math.log(a);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.LOG);
    return res;
  }

  @MJI
  public double log10__D__D(MJIEnv env, int clsRef, double a) {
    double res = Math.log10(a);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.LOG10);
    return Math.log10(a);
  }
  
  @MJI
  public long round__D__J(MJIEnv env, int clsRef, double a) {
    attachSymbolicReturnFunc(env, MathFunctions.ROUND);
    return Math.round(a);
  }

  @MJI
  @SymbolicPeer
  public double sqrt__D__D(MJIEnv env, int clsRef, double a) {
    double res = Math.sqrt(a);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.SQRT);
    return res;
  }


  @MJI
  public double cbrt__D__D(MJIEnv env, int clsRef, double a) {
    return Math.cbrt(a);
  }

  @MJI
  public double IEEEremainder__DD__D(MJIEnv env, int clsRef, double f1, double f2) {
    return Math.IEEEremainder(f1, f2);
  }

  @MJI
  @SymbolicPeer
  public double ceil__D__D(MJIEnv env, int clsRef, double a) {
    Expression<Double> asym = argAttribute(env, BuiltinTypes.DOUBLE);
    if(asym != null) {
      Expression<Double> symReturn = new UnaryMinus<>(
          CastExpression.create(
              CastExpression.create(
                  new UnaryMinus<>(asym), BuiltinTypes.INTEGER),
              BuiltinTypes.DOUBLE));
      env.setReturnAttribute(symReturn);
    }
    return Math.ceil(a);
  }

  @MJI
  @SymbolicPeer
  public double floor__D__D(MJIEnv env, int clsRef, double a) {
    Expression<Double> asym = argAttribute(env, BuiltinTypes.DOUBLE);
    if(asym != null) {
      Expression<Double> symReturn = CastExpression.create(
              CastExpression.create(asym, BuiltinTypes.INTEGER),
              BuiltinTypes.DOUBLE);
      env.setReturnAttribute(symReturn);
    }
    return Math.floor(a);
  }

  @MJI
  public double rint__D__D(MJIEnv env, int clsRef, double a) {
    return Math.rint(a);
  }

  @MJI
  @SymbolicPeer  
  public double atan2__DD__D(MJIEnv env, int clsRef, double y, double x) {
    double res = Math.atan2(y,x);
    if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.ATAN2, y, x);
    return res;
  }

  @MJI
  public double pow__DD__D(MJIEnv env, int clsRef, double a, double b) {
    double res = Math.pow(a,b);
    //if(!Double.isNaN(res))
      attachSymbolicReturnFunc(env, MathFunctions.POW, a, b);
    /*else {
      System.out.println("PROBLEM");
    }*/
    return res;
  }

  @MJI
  public double random____D(MJIEnv env, int clsRef) {
    return Math.random();
  }

  @MJI
  public double ulp__D__D(MJIEnv env, int clsRef, double d) {
    return Math.ulp(d);
  }

  @MJI
  public float ulp__F__F(MJIEnv env, int clsRef, float f) {
    return Math.ulp(f);
  }

  @MJI
  public double sinh__D__D(MJIEnv env, int clsRef, double x) {
    return Math.sinh(x);
  }

  @MJI
  public double cosh__D__D(MJIEnv env, int clsRef, double x) {
    return Math.cosh(x);
  }

  @MJI
  public double tanh__D__D(MJIEnv env, int clsRef, double x) {
    return Math.tanh(x);
  }

  @MJI
  public double hypot__DD__D(MJIEnv env, int clsRef, double x, double y) {
    return Math.hypot(x, y);
  }

  @MJI
  public double expm1__D__D(MJIEnv env, int clsRef, double x) {
      return Math.expm1(x);
  }

  @MJI
  public double log1p__D__D(MJIEnv env, int clsRef, double x) {
      return Math.log1p(x);
  }

  @MJI
  public double copySign__DD__D(MJIEnv env, int clsRef, double magnitude, double sign) {
    return Math.copySign(magnitude, sign);
  }

  @MJI
  public float copySign__FF__F(MJIEnv env, int clsRef, float magnitude, float sign) {
    return Math.copySign(magnitude, sign);
  }

  @MJI
  public int getExponent__F__I(MJIEnv env, int clsRef, float f) {
    return Math.getExponent(f);
  }

  @MJI
  public int getExponent__D__I(MJIEnv env, int clsRef, double d) {
    return Math.getExponent(d);
  }

  @MJI
  public double nextAfter__DD__D(MJIEnv env, int clsRef, double start, double direction) {
    return Math.nextAfter(start, direction);
  }

  @MJI
  public float nextAfter__FD__F(MJIEnv env, int clsRef, float start, double direction) {
    return Math.nextAfter(start, direction);
  }

  @MJI
  public double nextUp__D__D(MJIEnv env, int clsRef, double d) {
    return Math.nextUp(d);
  }

  @MJI
  public float nextUp__F__F(MJIEnv env, int clsRef, float f) {
    return Math.nextUp(f);
  }


  @MJI
  public double scalb__DI__D(MJIEnv env, int clsRef, double d, int scaleFactor) {
    return Math.scalb(d, scaleFactor);
  }

  @MJI
  public float scalb__FI__F(MJIEnv env, int clsRef, float f, int scaleFactor) {
    return Math.scalb(f, scaleFactor);
  }
  
}
