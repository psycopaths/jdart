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

import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.types.Type;
import gov.nasa.jpf.jdart.ConcolicUtil;
import gov.nasa.jpf.jdart.SymbolicField;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Types;

/**
 * Allows to handle arrays of primitive types symbolically.
 * 
 *
 */
class PrimitiveArrayHandler implements SymbolicObjectHandler {


  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#initialize(gov.nasa.jpf.vm.ClassInfo)
   */
  @Override
  public boolean initialize(ClassInfo ci) {
    if(!ci.isArray())
      return false;
    return ci.getComponentClassInfo().isPrimitive();
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#annotateObject(gov.nasa.jpf.vm.ElementInfo, java.lang.String, gov.nasa.jpf.jdart.objects.SymbolicObjectsContext)
   */
  @Override
  public void annotateObject(ElementInfo ei, String name,
      SymbolicObjectsContext ctx) {
    byte typeCode = Types.getBuiltinType(ei.getClassInfo().getComponentClassInfo().getName());
    Type<?> type = ConcolicUtil.forTypeCode(typeCode);
    
    int size = ei.arrayLength();
    ei.defreeze();
    for(int i = 0; i < size; i++) {
      Variable<?> var = Variable.create(type, name + "[" + i + "]");
      ei.setElementAttr(i, var);
      ctx.processArrayElement(var, ei, i);
    }
    //ei.freeze();
  }

}
