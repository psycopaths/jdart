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
import gov.nasa.jpf.jdart.config.ConcolicConfig;
import gov.nasa.jpf.jdart.termination.TerminationStrategy;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.SimpleProfiler;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This singleton is the heart of the concolic execution framework. From
 * here all method analyses are started and maintained. This class is
 * - the facade to jdart
 * - the backend for the listener and perturbator
 * 
 */
public final class ConcolicExplorer {

  /**
   * logger
   */
  private JPFLogger logger = JPF.getLogger("jdart");
  
  /**
   * current method analysis
   */
  private ConcolicMethodExplorer analysis;
  
  /**
   * configuration of the symbolic analysis
   */
  private ConcolicConfig config;

  
  private Map<String,List<CompletedAnalysis>> completedAnalyses
    = new HashMap<>();
  
  /* ******************************************************************************
   * 
   * method analysis
   * 
   */

  /**
   * is there an active analysis?
   * 
   * @return 
   */
  public boolean hasCurrentAnalysis() {
    return (this.analysis != null);
  }

  
  /**
   * 
   */
  public void completeAnalysis() {
    if(this.analysis == null)
      throw new IllegalStateException("Cannot complete analysis when none is running!");
        
    // restore parameters and globals
    logger.finer("Completed analysis: " + this.analysis);
    String id = analysis.getId();
    
    List<CompletedAnalysis> caList = completedAnalyses.get(id);
    if(caList == null) {
      caList = new ArrayList<>();
      completedAnalyses.put(id, caList);
    }
    
    caList.add(analysis.finish());
    methodExplorers.add(analysis);
    analysis = null;
  }
  
  /**
   * starts a new analysis if 
   * (1) currently no analysis is running, and
   * (2) the invoked method has not been analyzed before.
   * 
   * @param insn
   * @param state
   * @param ti
   * @return 
   */
  public ConcolicMethodExplorer newAnalysis(String id, MethodInfo mi, StackFrame sf) {
    //logger.finest("ConcolicExplorer.newAnalysis()");
    // analyze methods only once and only if
    // no other analysis is currently active
    if(mi == null) {
      throw new IllegalStateException("Cannot start concolic analysis on native methods!");
    }
    
    if(this.analysis != null) {
      throw new IllegalStateException("Cannot start new analysis, already running!");
    }

    // start new analysis
    analysis = new ConcolicMethodExplorer(config, id, mi);
    ThreadInfo ti = VM.getVM().getCurrentThread();
    analysis.initializeMethod(ti, sf);
    
    analysis.makeCurrentAnalysis(ti);
    
    return analysis;    
  }
  
  public boolean isRootFrame(StackFrame sf) {
    return analysis.isRootFrame(sf);
  }
  
  
  /* ******************************************************************************
   * 
   * path constraints ...
   * 
   */
  
  
  /**
   * start a new path in the symbolic execution. This method is called
   * immediately before the execution of the symbolic method starts. 
   * It pre-initialized all parameters and globals (the ones that have been
   * instantiated already). Only in the first run of the method it does not do 
   * anything since there is already a set of values that can be used for
   * execution.
   */
  public void newPath(StackFrame sf) {
    logger.finest("ConcolicExplorer.newPath()");
    
    // sanity check, if no analysis is present: don't do anything
    if (this.analysis == null) {
      throw new IllegalStateException("Cannot start new analysis path when no analysis is running!");
    }
    
    // prepare execution
    SimpleProfiler.start("JDART-symbolic-path-execution");
    
    analysis.newPath(sf);
    // new set of globals ...
    //this.persistentGlobals = new SymbolicGlobals(this.config.getMinMax());    
  }
  

  public void completePathOk(ThreadInfo ti) {
    if(analysis == null)
      throw new IllegalStateException("Cannot complete paths when no analysis is running!");
    analysis.completePathOk(ti);
  }
  

  public void completePathError(ThreadInfo ti) {
    if(analysis == null)
      throw new IllegalStateException("Cannot complete paths when no analysis is running!");
    analysis.completePathError(ti);
  }

  

  /* ******************************************************************************
   * 
   * interface to choice generators ...
   * 
   */
  
  /**
   * checks if current execution has more choices. return true only if 
   * there are choices left
   * 
   * @return 
   */
  public boolean hasMoreChoices() {        
    if(analysis == null)
      throw new IllegalStateException("ConcolicExplorer asked for more choices, but no analysis is running");
    
    TerminationStrategy t = getTermination();
    if (t.isDone()) {
      logger.severe("Terminating exploration prematurely: " + t.getReason());
      return false;
    }
    
    
    return analysis.hasMoreChoices();
  }
  
  public Map<String,List<CompletedAnalysis>> getCompletedAnalyses() {
    return completedAnalyses;
  }
  
  /* ******************************************************************************
   * 
   * api for retrieving collected data
   * 
   */  
  
  public ConcolicMethodExplorer getCurrentAnalysis() {
    return this.analysis;
  }
  
  
  public ConcolicExplorer() {
  }
  
  public ConcolicExplorer(ConcolicConfig config) {
    this.config = config;
  }


  public CompletedAnalysis getFirstCompletedAnalysis(String id) {
    return completedAnalyses.get(id).get(0);
  }


  public void configure(ConcolicConfig cc) {
    this.config = cc;
  }

  // LEGACY API
  
  private final List<ConcolicMethodExplorer> methodExplorers = new ArrayList<>();
  
  @Deprecated
  public List<ConcolicMethodExplorer> getMethodExplorers() {
    return methodExplorers;
  }

  private TerminationStrategy getTermination() {
    return this.config.getTerminationStrategy();
  }

  boolean isConfigured() {
    return this.config != null;
  }
}
