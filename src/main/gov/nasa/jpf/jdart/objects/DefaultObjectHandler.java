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
package gov.nasa.jpf.jdart.objects;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.StaticElementInfo;
import gov.nasa.jpf.vm.FieldInfo;

/**
 * Default symbolic object handler. Makes all fields symbolic.
 * 
 *
 */
class DefaultObjectHandler implements SymbolicObjectHandler {

  private static transient JPFLogger logger = JPF.getLogger("jdart");
  
  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#initialize(gov.nasa.jpf.vm.ClassInfo)
   */
  @Override
  public boolean initialize(ClassInfo ci) {
    return !ci.isPrimitive() && !ci.isArray(); 
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#annotateObject(gov.nasa.jpf.vm.ElementInfo, java.lang.String, gov.nasa.jpf.jdart.objects.SymbolicObjectsContext)
   */
  @Override
  public void annotateObject(ElementInfo ei, String name,
      SymbolicObjectsContext ctx) {
    ClassInfo ci = ei.getClassInfo();
    logger.finest("Annotating object of class " + ci.getName());
    FieldInfo[] fis;
    if(ei instanceof StaticElementInfo)
      fis = ci.getDeclaredStaticFields();
    else
      fis = ci.getDeclaredInstanceFields();
    for(FieldInfo fi : fis) {
      ctx.processField(ei, fi, name + "." + fi.getName());
    }
  }

}
