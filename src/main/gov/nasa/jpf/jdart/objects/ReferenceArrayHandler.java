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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;

/**
 * Allows to handle arrays of reference types symbolically.
 * 
 *
 */
class ReferenceArrayHandler implements SymbolicObjectHandler {

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#initialize(gov.nasa.jpf.vm.ClassInfo)
   */
  @Override
  public boolean initialize(ClassInfo ci) {
    return ci.isReferenceArray(); 
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#annotateObject(gov.nasa.jpf.vm.ElementInfo, java.lang.String, gov.nasa.jpf.jdart.objects.SymbolicObjectsContext)
   */
  @Override
  public void annotateObject(ElementInfo ei, String name,
      SymbolicObjectsContext ctx) {
    int size = ei.arrayLength();
    
    Heap heap = ctx.getHeap();
    for(int i = 0; i < size; i++) {
      int elemRef = ei.getReferenceElement(i);
      ElementInfo elem = heap.get(elemRef);
      if(elem != null) {
        ctx.processElement(ei, i, name + "[" + i + "]", false);
      }
    }
  }

}
