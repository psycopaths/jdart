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
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * JDart version of the native peer class for {@link java.lang.Character}. Refer to
 * {@link JPF_java_lang_Integer} for the rationale behind this redefinition.
 *
 */
public class JPF_java_lang_Character extends gov.nasa.jpf.vm.JPF_java_lang_Character {

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#$clinit____V(gov.nasa.jpf.vm.MJIEnv, int)
   */
  @Override
  @MJI
  public void $clinit____V(MJIEnv env, int clsObjRef) {
    // TODO Auto-generated method stub
    super.$clinit____V(env, clsObjRef);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#digit__CI__I(gov.nasa.jpf.vm.MJIEnv, int, char, int)
   */
  @Override
  @MJI
  public int digit__CI__I(MJIEnv env, int clsObjRef, char c, int radix) {
    // TODO Auto-generated method stub
    return super.digit__CI__I(env, clsObjRef, c, radix);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#forDigit__II__C(gov.nasa.jpf.vm.MJIEnv, int, int, int)
   */
  @Override
  @MJI
  public char forDigit__II__C(MJIEnv env, int clsObjRef, int digit, int radix) {
    // TODO Auto-generated method stub
    return super.forDigit__II__C(env, clsObjRef, digit, radix);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#getNumericValue__C__I(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public int getNumericValue__C__I(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.getNumericValue__C__I(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#getType__C__I(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public int getType__C__I(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.getType__C__I(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isDefined__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isDefined__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isDefined__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isDigit__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isDigit__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isDigit__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isISOControl__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isISOControl__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isISOControl__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isIdentifierIgnorable__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isIdentifierIgnorable__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isIdentifierIgnorable__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isJavaIdentifierPart__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isJavaIdentifierPart__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isJavaIdentifierPart__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isJavaIdentifierStart__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isJavaIdentifierStart__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isJavaIdentifierStart__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isJavaLetterOrDigit__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isJavaLetterOrDigit__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isJavaLetterOrDigit__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isJavaLetter__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isJavaLetter__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isJavaLetter__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isLetterOrDigit__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isLetterOrDigit__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isLetterOrDigit__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isLetter__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isLetter__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isLetter__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isLowerCase__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isLowerCase__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isLowerCase__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isSpaceChar__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isSpaceChar__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isSpaceChar__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isSpace__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isSpace__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isSpace__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isTitleCase__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isTitleCase__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isTitleCase__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isUnicodeIdentifierPart__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isUnicodeIdentifierPart__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isUnicodeIdentifierPart__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isUnicodeIdentifierStart__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isUnicodeIdentifierStart__C__Z(MJIEnv env, int clsObjRef,
      char c) {
    // TODO Auto-generated method stub
    return super.isUnicodeIdentifierStart__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isUpperCase__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isUpperCase__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isUpperCase__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#isWhitespace__C__Z(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public boolean isWhitespace__C__Z(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.isWhitespace__C__Z(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#toLowerCase__C__C(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public char toLowerCase__C__C(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.toLowerCase__C__C(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#toTitleCase__C__C(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public char toTitleCase__C__C(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.toTitleCase__C__C(env, clsObjRef, c);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#toUpperCase__C__C(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @Override
  @MJI
  public char toUpperCase__C__C(MJIEnv env, int clsObjRef, char c) {
    // TODO Auto-generated method stub
    return super.toUpperCase__C__C(env, clsObjRef, c);
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Character#valueOf__C__Ljava_lang_Character_2(gov.nasa.jpf.vm.MJIEnv, int, char)
   */
  @MJI
  @Override
  public int valueOf__C__Ljava_lang_Character_2 (MJIEnv env, int clsRef, char val) {
    Object[] attrs = env.getArgAttributes();
    if(attrs == null)
      return super.valueOf__C__Ljava_lang_Character_2(env, clsRef, val);
    
    Object valAttr = attrs[0];
    if(valAttr == null)
      return super.valueOf__C__Ljava_lang_Character_2(env, clsRef, val);
    
    ThreadInfo ti = env.getThreadInfo();
    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Character");
    ElementInfo ei = ti.getHeap().newObject(ci, ti);
    FieldInfo fi = ci.getDeclaredInstanceField("value");
    ei.setCharField(fi, val);
    ei.setFieldAttr(fi, valAttr);
    return ei.getObjectRef();
  }

}
