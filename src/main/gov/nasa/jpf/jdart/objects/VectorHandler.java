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
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Heap;

import java.util.Vector;

/**
 * Allows to handle {@link Vector}s symbolically (i.e., like an array).
 * 
 *
 */
public class VectorHandler implements SymbolicObjectHandler {

  private FieldInfo countField;
  private FieldInfo dataField;
  
  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#initialize(gov.nasa.jpf.vm.ClassInfo)
   */
  @Override
  public boolean initialize(ClassInfo ci) {
    if(!Vector.class.getName().equals(ci.getName()))
      return false;
    
    countField = ci.getDeclaredInstanceField("elementCount");
    dataField = ci.getDeclaredInstanceField("elementData");
    
    return true;
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#annotateObject(gov.nasa.jpf.vm.ElementInfo, java.lang.String, gov.nasa.jpf.jdart.objects.SymbolicObjectsContext)
   */
  @Override
  public void annotateObject(ElementInfo ei, String name, SymbolicObjectsContext ctx) {
    int size = ei.getIntField(countField);
    int edRef = ei.getReferenceField(dataField);
    Heap heap = ctx.getHeap();
    ElementInfo data = heap.get(edRef);
    if(data == null)
      return;
    
    for(int i = 0; i < size; i++) {
      int elemRef = data.getReferenceElement(i);
      ElementInfo elem = heap.get(elemRef);
      if(elem == null)
        continue;
      ctx.processObject(elem, name + "[" + i + "]");
    }
  }
  
  

}
