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

import java.util.LinkedList;

/**
 * Allows to handle {@LinkedList}s symbolically (i.e., like arrays).
 * 
 *
 */
class LinkedListHandler implements SymbolicObjectHandler {

  private FieldInfo firstField;
  private FieldInfo nextField;
  private FieldInfo itemField;
  
  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#initialize(gov.nasa.jpf.vm.ClassInfo)
   */
  @Override
  public boolean initialize(ClassInfo ci) {
    if(!LinkedList.class.getName().equals(ci.getName()))
      return false;
    firstField = ci.getDeclaredInstanceField("first");
    ClassInfo[] inners = ci.getInnerClassInfos();
    ClassInfo nodeCi = null;
    for(int i = 0; i < inners.length; i++) {
      ClassInfo ici = inners[i];
      if("Node".equals(ici.getSimpleName())) {
        nodeCi = ici;
        break;
      }
    }
    assert nodeCi != null;
    nextField = nodeCi.getDeclaredInstanceField("next");
    itemField = nodeCi.getDeclaredInstanceField("item");
    
    return true;
  }
  
  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#annotateObject(gov.nasa.jpf.vm.ElementInfo, java.lang.String, gov.nasa.jpf.jdart.objects.SymbolicObjectsContext)
   */
  @Override
  public void annotateObject(ElementInfo ei, String name, SymbolicObjectsContext ctx) {
    int firstRef = ei.getReferenceField(firstField);
    Heap heap = ctx.getHeap();
    ElementInfo curr = heap.get(firstRef);
    
    int i = 0;
    
    while(curr != null) {
      int itemRef = curr.getReferenceField(itemField);
      ElementInfo item = heap.get(itemRef);
      if(item != null) {
        ctx.processObject(item, name + "[" + i + "]");
      }
      i++;
      int nextRef = curr.getReferenceField(nextField);
      curr = heap.get(nextRef);
    }
  }
  
  

}
