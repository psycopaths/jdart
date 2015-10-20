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
package gov.nasa.jpf.jdart;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.constraints.types.NumericType;
import gov.nasa.jpf.constraints.types.Type;
import gov.nasa.jpf.constraints.util.ExpressionUtil;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.Types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ConcolicUtil {

  /**
   * pair of concrete and symbolic value ...
   */
  public static class Pair<T> {

    public static <T> Pair<T> create(T conc, Expression<T> symb) {
      return new Pair<T>(conc, symb);
    }
    
    public final T conc;
    public final Expression<T> symb;

    public Pair(T conc, Expression<T> symb) {
      this.symb = symb;
      this.conc = conc;
    }
    
    public boolean isConcrete() {
      return (symb instanceof Constant);
    }

    @Override
    public String toString() {
      return this.conc + " [" + this.symb + "]";
    }
  }
  /*
   * **********************************************************************************************
   *
   * anonymous variables
   *
   */

  public static synchronized Variable<Double> getAnonymousDouble() {
    return Variable.createAnonymous(BuiltinTypes.DOUBLE);
  }

  public static synchronized Variable<Float> getAnonymousFloat() {
    return Variable.createAnonymous(BuiltinTypes.FLOAT);
  }

  public static synchronized Variable<Integer> getAnonymousInt() {
    return Variable.createAnonymous(BuiltinTypes.SINT32);
  }

  /*
   * **********************************************************************************************
   *
   * push and pop
   *
   */
  
  public static Pair<Boolean> peekBoolean(StackFrame sf) {
    return peekBoolean(sf, 0);
  }
  
  public static Pair<Boolean> peekBoolean(StackFrame sf, int stackOffset) {
    Expression<?> symb = (Expression<?>)sf.getOperandAttr(stackOffset, Expression.class);
    int val = sf.peek();
    boolean conc = (val != 0);
    if(symb == null) {
      symb = ExpressionUtil.boolConst(conc);
    }
    else if(!BuiltinTypes.BOOL.equals(symb.getType())) {
      if(symb.getType() instanceof NumericType) {
        NumericType<?> nt = (NumericType<?>)symb.getType();
        Constant<?> zero = Constant.createCasted(nt, nt.getDefaultValue()); // 0
        symb = new NumericBooleanExpression(symb, NumericComparator.NE, zero);
      }
      else
        throw new IllegalStateException();
    }
    return Pair.create(conc, symb.requireAs(BuiltinTypes.BOOL));
  }
  
  public static Pair<Boolean> popBoolean(StackFrame sf) {
    Pair<Boolean> result = peekBoolean(sf);
    sf.pop();
    return result;
  }
  
  
  public static Pair<Double> popDouble(StackFrame sf) {
    Pair<Double> result = peekDouble(sf);
    sf.popDouble();
    return result;
  }
  
  public static Pair<Double> peekDouble(StackFrame sf) {
    return peekDouble(sf, 0);
  }
  
  public static Pair<Double> peekDouble(StackFrame sf, int stackOffset) {
    Expression<?> symb = (Expression<?>)sf.getOperandAttr(stackOffset+1, Expression.class);
    double conc = sf.peekDouble(stackOffset);
    if (symb == null) {
      symb = Constant.create(BuiltinTypes.DOUBLE, conc);
    }
    return Pair.create(conc, symb.requireAs(BuiltinTypes.DOUBLE));
  }

  public static void pushDouble(Pair<Double> pair, StackFrame sf) {
    sf.pushDouble(pair.conc);
    sf.setLongOperandAttr(pair.symb);
  }

  public static Pair<Long> popLong(StackFrame sf) {
    Pair<Long> result = peekLong(sf);
    sf.popLong();
    return result;
  }
  
  public static Pair<Long> peekLong(StackFrame sf) {
    return peekLong(sf, 0);
  }
  
  public static Pair<Long> peekLong(StackFrame sf, int stackOffset) {
    Expression<?> symb = (Expression<?>) sf.getOperandAttr(stackOffset+1, Expression.class);
    long conc = sf.peekLong(stackOffset);
    if (symb == null) {
      symb = Constant.create(BuiltinTypes.SINT64, conc);
    }
    return Pair.create(conc, symb.requireAs(BuiltinTypes.SINT64));
  }

  public static void pushLong(Pair<Long> pair, StackFrame sf) {
    sf.pushLong((long) pair.conc);
    sf.setLongOperandAttr(pair.symb);
  }

  public static Pair<Float> popFloat(StackFrame sf) {
    Pair<Float> result = peekFloat(sf);
    sf.popFloat();
    return result;
  }
  
  public static Pair<Float> peekFloat(StackFrame sf) {
    return peekFloat(sf, 0);
  }
  
  public static Pair<Float> peekFloat(StackFrame sf, int stackOffset) {
    Expression<?> symb = (Expression<?>) sf.getOperandAttr(stackOffset, Expression.class);
    float conc = sf.peekFloat(stackOffset);
    if (symb == null) {
      symb = Constant.create(BuiltinTypes.FLOAT, conc);
    }
    return Pair.create(conc, symb.requireAs(BuiltinTypes.FLOAT));
  }

  public static void pushFloat(Pair<Float> pair, StackFrame sf) {
    sf.push(Types.floatToInt((float) pair.conc), false);
    sf.setOperandAttr(pair.symb);
  }
  
  public static Pair<Integer> popInt(StackFrame sf) {
    Pair<Integer> result = peekInt(sf);
    sf.pop();
    return result;
  }
  

  public static Pair<Integer> peekInt(StackFrame sf, int stackOffset) {
    Expression<?> symb = (Expression<?>) sf.getOperandAttr(stackOffset, Expression.class);
    int conc = sf.peek(stackOffset);
    Expression<Integer> isymb;
    if (symb == null) {
      isymb = Constant.create(BuiltinTypes.SINT32, conc);
    }
    else if (symb.getType().equals(BuiltinTypes.SINT32)) {
      isymb = symb.requireAs(BuiltinTypes.SINT32);
    }
    else {
      isymb = CastExpression.create(symb, BuiltinTypes.SINT32);
    }
    return Pair.create(conc, isymb);
  }
  
  public static Pair<Integer> peekInt(StackFrame sf) {
    return peekInt(sf, 0);
  }
  
  
  public static void pushInt(Pair<Integer> pair, StackFrame sf) {
    sf.push((int) pair.conc, false);
    sf.setOperandAttr(pair.symb);
  }
  
  public static void pushByte(Pair<Byte> pair, StackFrame sf) {
    pushByte(pair.conc, pair.symb, sf);
  }
  
  public static void pushByte(byte conc, Expression<Byte> symb, StackFrame sf) {
    sf.push(conc);
    sf.setOperandAttr(symb);
  }

  public static void pushShort(Pair<Short> pair, StackFrame sf) {
    pushShort(pair.conc, pair.symb, sf);
  }
  
  public static void pushShort(short conc, Expression<Short> symb, StackFrame sf) {
    sf.push(conc);
    sf.setOperandAttr(symb);
  }
  
  public static void pushChar(Pair<Character> pair, StackFrame sf) {
    pushChar(pair.conc, pair.symb, sf);
  }
  
  
  public static void pushChar(char conc, Expression<Character> symb, StackFrame sf) {
    sf.push(conc);
    sf.setOperandAttr(symb);
  }
  
  public static <T> Expression<T> peekIntType(StackFrame sf, int stackOffset, T concVal, Type<T> type) {
    Expression<?> symb = sf.getOperandAttr(stackOffset, Expression.class);
    Expression<T> tsymb = null;
    if(symb == null) {
      tsymb = Constant.create(type, concVal);
    }
    else if(type.equals(symb.getType())) {
      tsymb = symb.requireAs(type);
    }
    else if(symb instanceof CastExpression) {
      // check for widening/narrowing
      // note that the reverse, narrowing/widening is NOT value-preserving!
      CastExpression<?,?> ce = (CastExpression<?,?>)symb;
      if(ce.getType().equals(BuiltinTypes.SINT32) && ce.getCasted().getType().equals(type))
        tsymb = ce.getCasted().requireAs(type);
    }
    if(tsymb == null) {
      tsymb = CastExpression.create(symb, type);
    }
    
    return tsymb;
  }
  
  public static Pair<Byte> peekByte(StackFrame sf) {
    return peekByte(sf, 0);
  }
  
  public static Pair<Byte> peekByte(StackFrame sf, int stackOffset) {
    Byte b = (byte)sf.peek();
    Expression<Byte> symb = peekIntType(sf, stackOffset, b, BuiltinTypes.SINT8);
    return Pair.create(b, symb);
  }
  
  public static Pair<Byte> popByte(StackFrame sf) {
    Pair<Byte> res = peekByte(sf);
    sf.pop();
    return res;
  }
  
  public static Pair<Short> peekShort(StackFrame sf) {
    return peekShort(sf, 0);
  }
  
  public static Pair<Short> peekShort(StackFrame sf, int stackOffset) {
    Short s = (short)sf.peek();
    Expression<Short> symb = peekIntType(sf, stackOffset, s, BuiltinTypes.SINT16);
    return Pair.create(s, symb);
  }
  
  public static Pair<Short> popShort(StackFrame sf) {
    Pair<Short> res = peekShort(sf);
    sf.pop();
    return res;
  }
  
  public static Pair<Character> peekChar(StackFrame sf) {
    return peekChar(sf, 0);
  }
  
  public static Pair<Character> peekChar(StackFrame sf, int stackOffset) {
    Character c = (char)sf.peek();
    Expression<Character> symb = peekIntType(sf, stackOffset, c, BuiltinTypes.UINT16);
    return Pair.create(c, symb);
  }
  
  public static Pair<Character> popChar(StackFrame sf) {
    Pair<Character> res = peekChar(sf);
    sf.pop();
    return res;
  }

  public static Object peek(StackFrame frame, int stackPos, Class<?> type) {
    if (type.equals(Float.class) || type.equals(float.class)) {
      return frame.peekFloat(stackPos);
    }

    if (type.equals(Long.class) || type.equals(long.class)) {
      return frame.peekLong(stackPos);
    }

    if (type.equals(Double.class) || type.equals(double.class)) {
      return frame.peekDouble(stackPos);
    }
    
    if (type.equals(Boolean.class) || type.equals(boolean.class)) {
      int i = frame.peek(stackPos);
      return i!=0;
    }
    // default behavior
    return frame.peek(stackPos);
  }
  
  
  
  public static void setOperandAttr(StackFrame sf, int stackPos, Type<?> type, Object value) {
    if(BuiltinTypes.SINT64.equals(type) || BuiltinTypes.DOUBLE.equals(type))
      stackPos++;
    sf.setOperandAttr(stackPos, value);
  }

  
  public static <T> void setOperand(StackFrame sf, int stackPos, Type<T> type, T value) {
    if (BuiltinTypes.FLOAT.equals(type)) {
      sf.setOperand(stackPos, Types.floatToInt( (Float) value), false);
    }
    else if (BuiltinTypes.SINT64.equals(type)) {
      long l = (Long) value;            
      sf.setOperand(stackPos, Types.loLong(l) ,false);
      sf.setOperand(stackPos+1, Types.hiLong(l) ,false);
    }
    else if (BuiltinTypes.DOUBLE.equals(type)) {
      double l = (Double) value;
      sf.setOperand(stackPos, Types.loDouble(l) ,false);
      sf.setOperand(stackPos+1, Types.hiDouble(l) ,false);
    }
    else if (BuiltinTypes.BOOL.equals(type)) {
      sf.setOperand(stackPos, (Boolean) value ? 1 : 0 ,false);
    }
    else if (BuiltinTypes.SINT16.equals(type)) {
      sf.setOperand(stackPos, (Short)value, false);
    }
    else if (BuiltinTypes.SINT8.equals(type)) {
      sf.setOperand(stackPos, (Byte)value, false);
    }
    else if (BuiltinTypes.UINT16.equals(type)) {
      sf.setOperand(stackPos, (Character)value, false);
    }
    // default behavior
    else {
      sf.setOperand(stackPos, (Integer) value ,false);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getArrayElement(ElementInfo ei, int slotId, Type<T> type) {
    if (BuiltinTypes.FLOAT.equals(type)) {
      return (T)Float.valueOf(ei.getFloatElement(slotId));
    }
    if (BuiltinTypes.SINT64.equals(type)) {
      return (T)Long.valueOf(ei.getLongElement(slotId));
    }
    if (BuiltinTypes.DOUBLE.equals(type)) {
      return (T)Double.valueOf(ei.getDoubleElement(slotId));
    }
    if (BuiltinTypes.SINT32.equals(type)) {
      return (T)Integer.valueOf(ei.getIntElement(slotId));
    }
    if (BuiltinTypes.SINT16.equals(type)) {
      return (T)Short.valueOf(ei.getShortElement(slotId));
    }
    if (BuiltinTypes.SINT8.equals(type)) {
      return (T)Byte.valueOf(ei.getByteElement(slotId));
    }
    if (BuiltinTypes.BOOL.equals(type)) {
      return (T)Boolean.valueOf(ei.getBooleanElement(slotId)); 
    }
    if (BuiltinTypes.UINT16.equals(type)) {
      return (T)Character.valueOf(ei.getCharElement(slotId));
    }
    // default behavior
    throw new RuntimeException("type " + type + " is not supported currently");
  }
  
  public static <T> void setArrayElement(ElementInfo ei, int slotId, Type<T> type, T value) {
    if (BuiltinTypes.FLOAT.equals(type)) {
      ei.setFloatElement(slotId, (Float)value);
    }
    else if (BuiltinTypes.SINT64.equals(type)) {
      ei.setLongElement(slotId, (Long)value);
    }
    else if (BuiltinTypes.DOUBLE.equals(type)) {
      ei.setDoubleElement(slotId, (Double)value);
    }
    else if (BuiltinTypes.SINT32.equals(type)) {
      ei.setIntElement(slotId, (Integer)value);
    }
    else if (BuiltinTypes.SINT16.equals(type)) {
      ei.setShortElement(slotId, (Short)value);
    }
    else if (BuiltinTypes.SINT8.equals(type)) {
      ei.setByteElement(slotId, (Byte)value);
    }
    else if (BuiltinTypes.BOOL.equals(type)) {
      ei.setBooleanElement(slotId, (Boolean)value);
    }
    else if (BuiltinTypes.UINT16.equals(type)) {
      ei.setCharElement(slotId, (Character)value);
    }
    else {
      throw new RuntimeException("type " + type.getName() + " is not supported currently");
    }
  }
  
  
  public static <T> void setField(ElementInfo ei, FieldInfo fi, Type<T> type, T value) {
    if (BuiltinTypes.FLOAT.equals(type)) {
      ei.setFloatField(fi, (Float)value);
    }
    else if (BuiltinTypes.SINT64.equals(type)) {
      ei.setLongField(fi, (Long)value);
    }
    else if (BuiltinTypes.DOUBLE.equals(type)) {
      ei.setDoubleField(fi, (Double)value);
    }
    else if (BuiltinTypes.SINT32.equals(type)) {
      ei.setIntField(fi, (Integer)value);
    }
    else if (BuiltinTypes.SINT16.equals(type)) {
      ei.setShortField(fi, (Short)value);
    }
    else if (BuiltinTypes.SINT8.equals(type)) {
      ei.setByteField(fi, (Byte)value);
    }
    else if (BuiltinTypes.BOOL.equals(type)) {
      ei.setBooleanField(fi, (Boolean)value);
    }
    else {
      throw new RuntimeException("type " + type.getName() + " is not supported currently");
    }
  }
  
  public static Type<?> forTypeCode(byte typeCode) {
    switch(typeCode) {
    case Types.T_BOOLEAN:
      return BuiltinTypes.BOOL;
    case Types.T_BYTE:
      return BuiltinTypes.SINT8;
    case Types.T_CHAR:
      return BuiltinTypes.UINT16;
    case Types.T_SHORT:
      return BuiltinTypes.SINT16;
    case Types.T_INT:
      return BuiltinTypes.SINT32;
    case Types.T_LONG:
      return BuiltinTypes.SINT64;
    case Types.T_FLOAT:
      return BuiltinTypes.FLOAT;
    case Types.T_DOUBLE:
      return BuiltinTypes.DOUBLE;
    default:
      throw new IllegalArgumentException("Type code " + typeCode + " is not supported");
    }
  }
  
  public static Type<?> forClassInfo(ClassInfo ci) {
    if(!ci.isPrimitive())
      return null;
    return forName(ci.getName());
  }
  
  public static Type<?> forName(String typeName) {
    if("boolean".equals(typeName)) {
      return BuiltinTypes.BOOL;
    }
    if ("byte".equals(typeName)) {
      return BuiltinTypes.SINT8;
    }
    if ("char".equals(typeName)) {
      return BuiltinTypes.UINT16;
    }
    if ("short".equals(typeName)) {
      return BuiltinTypes.SINT16;
    }
    if ("int".equals(typeName)) {
      return BuiltinTypes.SINT32;
    }
    if ("long".equals(typeName)) {
      return BuiltinTypes.SINT64;
    }
    if ("float".equals(typeName)) {
      return BuiltinTypes.FLOAT;
    }
    if ("double".equals(typeName)) {
      return BuiltinTypes.DOUBLE;
    }
    throw new IllegalArgumentException("Type name " + typeName + " is not supported");
  }

  public static Object fromString(Type type, String string) {
    if(BuiltinTypes.BOOL.equals(type)) {
      return Boolean.parseBoolean(string);
    }
    if(BuiltinTypes.SINT8.equals(type)) {
      return Byte.parseByte(string);
    }
    if(BuiltinTypes.UINT16.equals(type)) {
      return string.charAt(0);
    }
    if(BuiltinTypes.SINT16.equals(type)) {
      return Short.parseShort(string);
    }
    if(BuiltinTypes.SINT32.equals(type)) {
      return Integer.parseInt(string);
    }
    if(BuiltinTypes.SINT64.equals(type)) {
      return Long.parseLong(string);
    }
    if(BuiltinTypes.FLOAT.equals(type)) {
      return Float.parseFloat(string);
    }
    if(BuiltinTypes.DOUBLE.equals(type)) {
      return Double.parseDouble(string);
    }
    throw new IllegalArgumentException("Cannot peek type " + type);  }
    
  
  public static <T> Pair<T> peek(StackFrame sf, Type<T> type) {
    return peek(sf, 0, type);
  }
  
  
  @SuppressWarnings("unchecked")
  public static <T> Pair<T> peek(StackFrame sf, int stackOffset, Type<T> type) {
    if(BuiltinTypes.BOOL.equals(type)) {
      return (Pair<T>)peekBoolean(sf, stackOffset);
    }
    if(BuiltinTypes.SINT8.equals(type)) {
      return (Pair<T>)peekByte(sf, stackOffset);
    }
    if(BuiltinTypes.UINT16.equals(type)) {
      return (Pair<T>)peekChar(sf, stackOffset);
    }
    if(BuiltinTypes.SINT16.equals(type)) {
      return (Pair<T>)peekShort(sf, stackOffset);
    }
    if(BuiltinTypes.SINT32.equals(type)) {
      return (Pair<T>)peekInt(sf, stackOffset);
    }
    if(BuiltinTypes.SINT64.equals(type)) {
      return (Pair<T>)peekLong(sf, stackOffset);
    }
    if(BuiltinTypes.FLOAT.equals(type)) {
      return (Pair<T>)peekFloat(sf, stackOffset);
    }
    if(BuiltinTypes.DOUBLE.equals(type)) {
      return (Pair<T>)peekDouble(sf, stackOffset);
    }
    throw new IllegalArgumentException("Cannot peek type " + type);
  }
  
  
  
  
  
  // LEGACY API

  public static Class<?> parseType(String typename) {

    //System.out.println("type for name: " + typename);
    if (typename.equalsIgnoreCase("con")) {
      return null; // FIXME check
    }

    // primitive types
    if (typename.equalsIgnoreCase("int")) {
      return int.class;
    }
    if (typename.equalsIgnoreCase("short")) {
      return short.class;
    }
    if (typename.equalsIgnoreCase("char")) {
      return char.class;
    }
    if (typename.equalsIgnoreCase("byte")) {
      return byte.class;
    }
    if (typename.equalsIgnoreCase("boolean")) {
      return boolean.class;
    }
    if (typename.equalsIgnoreCase("long")) {
      return long.class;
    }
    if (typename.equalsIgnoreCase("float")) {
      return float.class;
    }
    if (typename.equalsIgnoreCase("double")) {
      return double.class;
    }

    // arrays of primitive types
    if (typename.equalsIgnoreCase("int[]")) {
      return int[].class;
    }
    if (typename.equalsIgnoreCase("short[]")) {
      return short[].class;
    }
    if (typename.equalsIgnoreCase("char[]")) {
      return char[].class;
    }
    if (typename.equalsIgnoreCase("byte[]")) {
      return byte[].class;
    }
    if (typename.equalsIgnoreCase("boolean[]")) {
      return boolean[].class;
    }
    if (typename.equalsIgnoreCase("long[]")) {
      return long[].class;
    }
    if (typename.equalsIgnoreCase("float[]")) {
      return float[].class;
    }
    if (typename.equalsIgnoreCase("double[]")) {
      return double[].class;
    }

    // class ...
    boolean isArray = false;
    if (typename.contains("[")) {
      isArray = true;
      typename = typename.substring(0, typename.indexOf("["));
    }

    Class<?> type = null;
    try {
      type = Class.forName(typename);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException("class " + typename + " not found");
    }

    if (!isArray) {
      return type;
    }

    throw new RuntimeException("arrays of objects are not supported currently");
  }

  
  @Deprecated
  public static boolean isPrimitiveType(Class<?> clazz) {
    return PRIMITIVE_TYPES.contains(clazz);
  }
  
  
  private static final Set<Class<?>> PRIMITIVE_TYPES
    = new HashSet<>(Arrays.asList(
        Boolean.class, Character.class, Byte.class, Short.class,
        Integer.class, Long.class, Float.class, Double.class,
        Void.class,
        boolean.class, char.class, byte.class, short.class,
        int.class, long.class, float.class, double.class,
        void.class));


}
