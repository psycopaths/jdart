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

import java.util.HashMap;

/**
 * Allows to handle {@link HashMap}s symbolically. Element names
 * are either a string representation of a primitive (or wrapper)
 * type, or {@link String}, or a running number prefixed by "#".
 * 
 * 
 *
 */
class HashMapHandler implements SymbolicObjectHandler {
  
  private FieldInfo tableField; // HashMap.table
  private FieldInfo keyField; // HashMap$Entry.key
  private FieldInfo valueField; // HashMap$Entry.value
  private FieldInfo nextField; // HashMap$Entry.next

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#initialize(gov.nasa.jpf.vm.ClassInfo)
   */
  public boolean initialize(ClassInfo ci) {
    if(!HashMap.class.getName().equals(ci.getName()))
      return false;
    
    tableField = ci.getDeclaredInstanceField("table");
    ClassInfo[] inners = ci.getInnerClassInfos();
    ClassInfo entryCi = null;
    for(ClassInfo ici : inners) {
      
      //For Java 7
      //if("Entry".equals(ici.getSimpleName())) {
      //For Java 8
      if("Node".equals(ici.getSimpleName())) {
        entryCi = ici;
        break;
      }
    }
    assert entryCi != null;
    
    //For java 8 (hashmap implementation was changed)
    //For java 7
    keyField = entryCi.getDeclaredInstanceField("key");
    valueField = entryCi.getDeclaredInstanceField("value");
    nextField = entryCi.getDeclaredInstanceField("next");
    return true;
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.jdart.objects.SymbolicObjectHandler#annotateObject(gov.nasa.jpf.vm.ElementInfo, java.lang.String, gov.nasa.jpf.jdart.objects.SymbolicObjectsContext)
   */
  @Override
  public void annotateObject(ElementInfo ei, String name, SymbolicObjectsContext ctx) {
    int tableRef = ei.getReferenceField(tableField);
    Heap heap = ctx.getHeap();
    ElementInfo table = heap.get(tableRef);
    if(table == null) {
      return;
    }
    int siz = table.arrayLength();
    
    int elemId = 0;
    for(int i = 0; i < siz; i++) {
      int entryRef = table.getReferenceElement(i);
      ElementInfo entry = heap.get(entryRef);
      
      while(entry != null) {
        int valueRef = entry.getReferenceField(valueField);
        ElementInfo value = heap.get(valueRef);
        if(value != null) {
          int keyRef = entry.getReferenceField(keyField);
          ElementInfo key = heap.get(keyRef);
          String keyString;
          if(key == null) {
            keyString = "null";
          }
          else if(String.class.getName().equals(key.getClassInfo().getName())) {
            keyString = "\"" + String.valueOf(key.getStringChars()) + "\"";
          }
          else {
            keyString = "#" + elemId;
          }
          ctx.processObject(value, name + "[" + keyString + "]");
        }
        elemId++;
        int nextRef = entry.getReferenceField(nextField);
        entry = heap.get(nextRef);
      }
    }
  }

}
