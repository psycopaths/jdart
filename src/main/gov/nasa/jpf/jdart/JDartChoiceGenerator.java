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
package gov.nasa.jpf.jdart;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.ObjectList.Iterator;
import gov.nasa.jpf.util.ObjectList.TypedIterator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.Comparator;

/**
 * refers to ConcolicExplorer when asked about more choices.
 */
public class JDartChoiceGenerator implements ChoiceGenerator<Integer> {

  @SuppressWarnings("unused")
  private JPFLogger logger = JPF.getLogger("jdart");
  
  private ThreadInfo threadInfo;
  
  private Instruction instruction;
  
  private boolean cascaded;
  
  private ChoiceGenerator<?> prev;

  private String id;
  
  private final ConcolicExplorer explorer;
  
  
  JDartChoiceGenerator(String id, MethodInfo methodInfo, ConcolicExplorer explorer) {
    // logger.finest("JDartChoiceGenerator() " +id);
    this.id = id;
    this.explorer = explorer;
  }
  
  /**
   * check with symbolic explorer if there are more choices. Only before the 
   * first call to advance, true is answered unconditionally.
   * 
   * @return 
   */
  @Override
  public boolean hasMoreChoices() {
    if(explorer.hasMoreChoices())
      return true;
    explorer.completeAnalysis();
    return false;
  }

  /**
   * 
   */
  @Override
  public void advance() {
    // nothing to do ...
  }

  @Override
  public ChoiceGenerator<?> clone() throws CloneNotSupportedException {
    return this;
  }
  
  
  /* ***************************************************************************
   * 
   * implemented not to raise exceptions
   * 
   */
    
  @Override
  public ChoiceGenerator<?> getPreviousChoiceGenerator() {
    return this.prev;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public void setId(String string) {
    this.id = string;
  }

  @Override
  public void setThreadInfo(ThreadInfo ti) {
    this.threadInfo = ti;
  }

  @Override
  public ThreadInfo getThreadInfo() {
    return this.threadInfo;
  }

  @Override
  public void setInsn(Instruction i) {
    this.instruction = i;
  }

  @Override
  public Instruction getInsn() {
    return this.instruction;
  }

  @Override
  public void setContext(ThreadInfo ti) {
    this.threadInfo = ti;
    this.instruction = ti.getPC();
  }

  @Override
  public String getSourceLocation() {
    return this.instruction.getSourceLocation();
  }

  @Override
  public void setPreviousChoiceGenerator(ChoiceGenerator<?> cg) {
    this.prev = cg;
  }

  @Override
  public void setCascaded() {
    this.cascaded = true;
  }

  @Override
  public boolean isCascaded() {
    return this.cascaded;
  }

  /*
   * copy paste implementation
   */
  @Override
  public ChoiceGenerator<?> getCascadedParent() {
    if (prev != null){
      if (prev.isCascaded()){
        return prev;
      }
    }
    return null;
  }

  /*
   * copy paste implementation
   */
  @Override
  public ChoiceGenerator<?>[] getCascade() {
    int  n = 0;
    for (ChoiceGenerator<?> cg = this; cg != null; cg = cg.getCascadedParent()){
      n++;
    }

    ChoiceGenerator<?>[] a = new ChoiceGenerator<?>[n];
    for (ChoiceGenerator<?> cg = this; cg != null; cg = cg.getCascadedParent()){
      a[--n] = cg;
    }
    
    return a;
  }
  
  /* ***************************************************************************
   * 
   * unimplemented methods
   * 
   */
  @Override
  public ChoiceGenerator<?>[] getAll() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public <C extends ChoiceGenerator<?>> C[] getAllOfType(Class<C> type) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public boolean hasAttr() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public boolean hasAttr(Class<?> type) {
    return false;
  }
  @Override
  public Object getAttr() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public void setAttr(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public void addAttr(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public void removeAttr(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public void replaceAttr(Object o, Object o1) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public <A> A getAttr(Class<A> type) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public <A> A getNextAttr(Class<A> type, Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public Iterator attrIterator() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public <A> TypedIterator<A> attrIterator(Class<A> type) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Class<Integer> getChoiceType() {
    return Integer.class;
  }
  @Override
  public int getTotalNumberOfChoices() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public int getProcessedNumberOfChoices() {
    throw new UnsupportedOperationException("Not supported yet.");
  }  
  @Override
  public void advance(int i) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public void select(int i) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public <T extends ChoiceGenerator<?>> T getPreviousChoiceGeneratorOfType(Class<T> type) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public boolean supportsReordering() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public ChoiceGenerator<Integer> reorder(Comparator<Integer> cmprtr) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public boolean isSchedulingPoint() {
    return false;
  }
  @Override
  public int getIdRef() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public void setIdRef(int i) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public ChoiceGenerator<Integer> randomize() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public ChoiceGenerator<?> deepClone() throws CloneNotSupportedException {
    //throw new UnsupportedOperationException("Not supported yet.");
    return null;
  }  

  @Override
  public void setDone() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public boolean isProcessed() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public boolean isDone() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  @Override
  public void reset() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getNumberOfParents() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  
  //Added after transition to java 8
  
  @Override
  public Integer getNextChoice() {
    //this is necessary in jpf-core v8
    //The interface is strange.
    //Just returning a bogus value
    return new Integer(0);
  }
  
  @Override
  public void setCurrent() {
    //we don't care...
  }

  @Override
  public Integer getChoice(int i) {
    //throw new UnsupportedOperationException("Not supported yet.");
    return null;
  }

  @Override
  public Integer[] getAllChoices(){
    int n = getTotalNumberOfChoices();
    Integer[] a = (Integer[]) new Object[n];
    for (int i=0; i<n; i++){
      Integer c = getChoice(i);
      if (c == null){
        return null; // CG doesn't support choice enumeration
      } else {
        a[i] = c;
      }
    }
    return a;
  }


  @Override
  public Integer[] getProcessedChoices(){
    int n = getProcessedNumberOfChoices();
    Integer[] a = (Integer[]) new Object[n];
    for (int i=0; i<n; i++){
      Integer c = getChoice(i);
      if (c == null){
        return null; // CG doesn't support choice enumeration
      } else {
        a[i] = c;
      }   
    }   
    return a;    
  }


  //copied from choicegeneratorbase
  @Override
     public Integer[] getUnprocessedChoices(){
       int n = getTotalNumberOfChoices();
       int m = getProcessedNumberOfChoices();
       Integer[] a = (Integer[]) new Object[n];
       for (int i=m-1; i<n; i++){
         Integer c = getChoice(i);
         if (c == null){
           return null; // CG doesn't support choice enumeration
         } else {
           a[i] = c;
         }
       }
       return a;    
    }


  private int stateId;
  
  @Override
  public void setStateId(int stateId){
    this.stateId = stateId;

    if (cascaded){
      getCascadedParent().setStateId(stateId);
    }
  }
  

  @Override
  public int getStateId() {
    return this.stateId;
  } 
}
