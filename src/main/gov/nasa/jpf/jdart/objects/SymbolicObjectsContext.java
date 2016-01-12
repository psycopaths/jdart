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
import gov.nasa.jpf.jdart.SymbolicArrayElem;
import gov.nasa.jpf.jdart.SymbolicField;
import gov.nasa.jpf.jdart.SymbolicParam;
import gov.nasa.jpf.jdart.SymbolicVariable;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.JPFLogger;

/**
 * Context for storing information about symbolically annotated objects.
 * 
 *
 */
public class SymbolicObjectsContext {
  
  private static transient JPFLogger logger = JPF.getLogger("jdart");
    
  private static final Map<String,SymbolicVariable<?>> globalSymbolics = new HashMap<>();
  
  public static void analyzeStatic(VM vm, ClassInfo ci) {
    SymbolicObjectsContext ctx = new SymbolicObjectsContext(vm.getHeap(), Predicates.alwaysFalse(), Predicates.alwaysFalse(), Predicates.alwaysFalse());
    FieldInfo[] fis = ci.getDeclaredStaticFields();
    ElementInfo sei = ci.getStaticElementInfo();
    for(FieldInfo fi : fis) {
      if(isSymbolicField(fi)) {
        String fqn = ci.getName() + "." + fi.getName();
        ctx.processField(sei, fi, fqn);
      }
    }
    
    globalSymbolics.putAll(ctx.symbolicVars);
  }
  
  public static void analyzeNewInstance(ThreadInfo ti, ElementInfo ei) {
    SymbolicObjectsContext ctx = new SymbolicObjectsContext(ti.getHeap(), Predicates.alwaysFalse(), Predicates.alwaysFalse(), Predicates.alwaysFalse());
    ClassInfo ci = ei.getClassInfo();
    FieldInfo[] fis = ci.getDeclaredInstanceFields();
    for(FieldInfo fi : fis) {
      if(isSymbolicField(fi)) {
        String fqn = ci.getName() + "." + fi.getName();
        ctx.processField(ei, fi, fqn);
      }
    }
    globalSymbolics.putAll(ctx.symbolicVars);
  }
  
  // "Special" handlers for container classes
  private static final SymbolicObjectHandler[] SPECIAL_HANDLERS = {
    new ArrayListHandler(),
    new HashMapHandler(),
    new VectorHandler(),
    new LinkedListHandler()
  };
  
  // Basic handlers
  private static final SymbolicObjectHandler[] BASIC_HANDLERS = {
    new PrimitiveArrayHandler(),
    new ReferenceArrayHandler(),
    new StringHandler(),
    new DefaultObjectHandler()
  };
  
  private final Heap heap;
  private final Predicate<? super String> exclude;
  private final Predicate<? super String> include;
  private final Predicate<? super ClassInfo> excludeSpecial;
  
  private final Map<String,SymbolicVariable<?>> symbolicVars;

  /**
   * Constructor.
   * @param heap the heap
   * @param exclude object identifiers to explicitly exclude
   * @param include object identifiers to explicitly include
   * @param excludeSpecial classe
   */
  public SymbolicObjectsContext(Heap heap, Predicate<? super String> exclude, Predicate<? super String> include,
      Predicate<? super ClassInfo> excludeSpecial) {
    this.heap = heap;
    this.exclude = exclude;
    this.include = include;
    this.excludeSpecial = excludeSpecial;
    this.symbolicVars = new HashMap<>(/*globalSymbolics*/);
  }

  public Heap getHeap() {
    return heap;
  }
  
  private SymbolicObjectHandler lookupHandler(ClassInfo ci) {
    if(!excludeSpecial.apply(ci)) {
      for(SymbolicObjectHandler shndlr : SPECIAL_HANDLERS) {
        if(shndlr.initialize(ci))
          return shndlr;
      }
    }
    
    for(SymbolicObjectHandler hndlr : BASIC_HANDLERS) {
      if(hndlr.initialize(ci))
        return hndlr;
    }
    
    throw new IllegalStateException("Should not happen");
  }
  
  public void processObject(ElementInfo ei, String name) {
    processObject(ei, name, false);
  }
  
  public void processObject(ElementInfo ei, String name, boolean forceSymbolic) {
    if(!forceSymbolic && !makeSymbolic(name)) {
      logger.finest("Not making " + name + " symbolic");
      return;
    }
    logger.finest("Making " + name + " symbolic");
    SymbolicObject attr = ei.getObjectAttr(SymbolicObject.class);
    if(attr != null)
      return;
    attr = new SymbolicObject(name);
    ei.defreeze();
    ei.setObjectAttr(attr);
    doProcessObject(ei, name);
    //ei.freeze();
  }
  
  public void processArrayElement(Variable<?> var, ElementInfo arrayElem, int slotId) {
    logger.finest("processing array element " + var.getName());
    SymbolicArrayElem<?> symVar = new SymbolicArrayElem<>(var, arrayElem, slotId);
    symbolicVars.put(symVar.getVariable().getName(), symVar);
  }
  
  private SymbolicObjectHandler getSymbolicObjectHandler(ClassInfo ci) {
    SymbolicObjectHandler hndlr = ci.getAttr(SymbolicObjectHandler.class);
    if(hndlr == null) {
      hndlr = lookupHandler(ci);
      ci.setAttr(hndlr);
    }
    return hndlr;
  }
  
  private void doProcessObject(ElementInfo ei, String name) {
    ClassInfo ci = ei.getClassInfo();
    SymbolicObjectHandler hndlr = getSymbolicObjectHandler(ci);
    hndlr.annotateObject(ei, name, this);
  }
  
  public void processField(ElementInfo ei, FieldInfo fi, String name) {
    logger.finest("processing field " + name);
    boolean force = isSymbolicField(fi);
    if(fi.isReference()) {
      int ref = ei.getReferenceField(fi);
      ElementInfo elem = heap.get(ref);
      if(elem == null)
        return;
      processObject(elem, name, force);
    }
    else {
      processPrimitiveField(ei, fi, name, force);
    }
  }
  
  private void processPrimitiveField(ElementInfo ei, FieldInfo fi, String name, boolean forceSymbolic) {
    boolean makeSymbolic = makeSymbolic(name);
    logger.finest("processing primitive field " + name + 
            ", force: " + forceSymbolic + ", makeSymbolic: " + makeSymbolic);
    if(!forceSymbolic && !makeSymbolic)
      return;
    if(fi.isStatic() && fi.isFinal()) // static final are most likely constants..
      return;
    Type<?> type = ConcolicUtil.forTypeCode(fi.getTypeCode());
    Variable<?> var = Variable.create(type, name);
    SymbolicField<?> symVar = new SymbolicField<>(var, ei, fi);
    logger.finest("creating variable: " + symVar);
    symbolicVars.put(name, symVar);
    ei.setFieldAttr(fi, var);
  }
  
  private boolean makeSymbolic(String name) {
    if(exclude.apply(name))
      return false;
    return include.apply(name);
  }

  public void processElement(ElementInfo ei, int i, String name, boolean forceSymbolic) {
    if(ei.isReferenceArray()) {
      int ref = ei.getReferenceElement(i);
      ElementInfo elem = heap.get(ref);
      if(elem == null)
        return;
      processObject(elem, name, forceSymbolic);
    }
  }

  public void addStackVar(SymbolicParam<?> sp) {
    symbolicVars.put(sp.getVariable().getName(), sp);
  }
  
  public Collection<SymbolicVariable<?>> getSymbolicVars() {
    return symbolicVars.values();
  }
  
  public static boolean isSymbolicField(FieldInfo fi) {
    AnnotationInfo ai = fi.getAnnotation("gov.nasa.jpf.jdart.Symbolic");
    return (ai != null && ai.valueAsString().equalsIgnoreCase("true"));
  }
}
