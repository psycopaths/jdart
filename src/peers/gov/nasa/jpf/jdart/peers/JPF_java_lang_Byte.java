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
 * JDart version of the native peer class for {@link java.lang.Byte}. Refer to
 * {@link JPF_java_lang_Integer} for the rationale behind this redefinition.
 *
 */
public class JPF_java_lang_Byte extends gov.nasa.jpf.vm.JPF_java_lang_Byte {

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.vm.JPF_java_lang_Byte#valueOf__B__Ljava_lang_Byte_2(gov.nasa.jpf.vm.MJIEnv, int, byte)
   */
  @MJI
  @Override
  public int valueOf__B__Ljava_lang_Byte_2 (MJIEnv env, int clsRef, byte val) {
    Object[] attrs = env.getArgAttributes();
    if(attrs == null)
      return super.valueOf__B__Ljava_lang_Byte_2(env, clsRef, val);
    
    Object valAttr = attrs[0];
    if(valAttr == null)
      return super.valueOf__B__Ljava_lang_Byte_2(env, clsRef, val);
    
    ThreadInfo ti = env.getThreadInfo();
    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Byte");
    ElementInfo ei = ti.getHeap().newObject(ci, ti);
    FieldInfo fi = ci.getDeclaredInstanceField("value");
    ei.setByteField(fi, val);
    ei.setFieldAttr(fi, valAttr);
    return ei.getObjectRef();
  }

}
