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
 * JDart version of the native peer class for {@link java.lang.Integer}.
 * <p>
 * The default implementation of the {@link Integer#valueOf(int)} method (which
 * is implicitly used for autoboxing) is to cache {@link Integer} objects corresponding
 * to <tt>int</tt> values in a certain, commonly used range. However, this also
 * means that any symbolic information (JPF attributes) is not maintained.
 * <p>
 * The native peer method for {@link Integer#valueOf(int)} employs caching only when
 * no symbolic information is present. Otherwise, new {@link Integer} objects are
 * always created.
 */
public class JPF_java_lang_Integer extends gov.nasa.jpf.vm.JPF_java_lang_Integer {


  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#parseInt__Ljava_lang_String_2I__I(gov.nasa.jpf.vm.MJIEnv, int, int, int)
   */
  @Override
  @MJI
  public int parseInt__Ljava_lang_String_2I__I(MJIEnv arg0, int arg1, int arg2,
      int arg3) {
    return super.parseInt__Ljava_lang_String_2I__I(arg0, arg1, arg2, arg3);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#parseInt__Ljava_lang_String_2__I(gov.nasa.jpf.vm.MJIEnv, int, int)
   */
  @Override
  @MJI
  public int parseInt__Ljava_lang_String_2__I(MJIEnv arg0, int arg1, int arg2) {
    return super.parseInt__Ljava_lang_String_2__I(arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#toBinaryString__I__Ljava_lang_String_2(gov.nasa.jpf.vm.MJIEnv, int, int)
   */
  @Override
  @MJI
  public int toBinaryString__I__Ljava_lang_String_2(MJIEnv env, int objref,
      int val) {
    return super.toBinaryString__I__Ljava_lang_String_2(env, objref, val);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#toHexString__I__Ljava_lang_String_2(gov.nasa.jpf.vm.MJIEnv, int, int)
   */
  @Override
  @MJI
  public int toHexString__I__Ljava_lang_String_2(MJIEnv env, int objref, int val) {
    return super.toHexString__I__Ljava_lang_String_2(env, objref, val);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#toOctalString__I__Ljava_lang_String_2(gov.nasa.jpf.vm.MJIEnv, int, int)
   */
  @Override
  @MJI
  public int toOctalString__I__Ljava_lang_String_2(MJIEnv env, int objref,
      int val) {
    return super.toOctalString__I__Ljava_lang_String_2(env, objref, val);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#toString__II__Ljava_lang_String_2(gov.nasa.jpf.vm.MJIEnv, int, int, int)
   */
  @Override
  @MJI
  public int toString__II__Ljava_lang_String_2(MJIEnv env, int objref, int val,
      int radix) {
    return super.toString__II__Ljava_lang_String_2(env, objref, val, radix);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#toString__I__Ljava_lang_String_2(gov.nasa.jpf.vm.MJIEnv, int, int)
   */
  @Override
  @MJI
  public int toString__I__Ljava_lang_String_2(MJIEnv env, int objref, int val) {
    return super.toString__I__Ljava_lang_String_2(env, objref, val);
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Integer#valueOf__I__Ljava_lang_Integer_2(gov.nasa.jpf.vm.MJIEnv, int, int)
   */
  @MJI
  @Override
  public int valueOf__I__Ljava_lang_Integer_2 (MJIEnv env, int clsRef, int val) {
    Object[] attrs = env.getArgAttributes();
    if(attrs == null)
      return super.valueOf__I__Ljava_lang_Integer_2(env, clsRef, val);
    
    Object valAttr = attrs[0];
    if(valAttr == null)
      return super.valueOf__I__Ljava_lang_Integer_2(env, clsRef, val);
    
    ThreadInfo ti = env.getThreadInfo();
    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Integer");
    ElementInfo ei = ti.getHeap().newObject(ci, ti);
    FieldInfo fi = ci.getDeclaredInstanceField("value");
    ei.setIntField(fi, val);
    ei.setFieldAttr(fi, valAttr);
    return ei.getObjectRef();
  }

}
