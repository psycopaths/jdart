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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jdart.objects.SymbolicObjectsContext;
import gov.nasa.jpf.listener.Perturbator;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.SimpleProfiler;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ExceptionInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * listener steering concolic execution.
 */
public class ConcolicListener extends Perturbator {
  
  /**
   * logger
   */
  private JPFLogger logger = JPF.getLogger("jdart");
  
  
  public ConcolicListener(Config conf) {
    super(conf);
    logger.finest("ConcolicListener()");
  }


  @Override
  public void propertyViolated(Search search) {
    ThreadInfo ti = search.getVM().getCurrentThread();
    ConcolicMethodExplorer ca = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    if(ca == null)
      return;
    ca.completePathError(ti);
    ti.clearPendingException();
  }
  

  /**
   * Called on exit of a method. If the method is leaving
   * the root frame of the analysis, finish the analysis
   * at this point.
   *  
   */
  @Override
  public void methodExited(VM vm, ThreadInfo ti, MethodInfo mi) {
    ConcolicMethodExplorer ca = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    if(ca == null)
      return;
    
    if(ca.isRootFrame(ti.getTopFrame())) {
      ExceptionInfo pending = ti.getPendingException();
      if(pending != null) {
        // there is a pending exception
        ca.completePathError(ti);
        ti.clearPendingException();
      }
      else {// normal return
        ca.completePathOk(ti);
      }
      // TODO: currently, this leads to program termination once all paths of our method have been explored.
      //       This is OK in terms of one analysis, but does not allow for multiple concolic analysis.
      ti.breakTransition(true);
    }
    else {
      ca.methodExited(ti, mi);
    }
  }


  /* ***************************************************************************
   * 
   * methods used for debugging
   * 
   */
  

  /* (non-Javadoc)
   * @see gov.nasa.jpf.ListenerAdapter#methodEntered(gov.nasa.jpf.vm.VM, gov.nasa.jpf.vm.ThreadInfo, gov.nasa.jpf.vm.MethodInfo)
   */
  @Override
  public void methodEntered(VM vm, ThreadInfo ti,
      MethodInfo mi) {
    ConcolicMethodExplorer ca = ConcolicMethodExplorer.getCurrentAnalysis(ti);
    if(ca == null)
      return;
    ca.methodEntered(ti, mi);
  }



  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.ListenerAdapter#searchStarted(gov.nasa.jpf.search.Search)
   */
  @Override
  public void searchStarted(Search search) {
    int id = search.getStateId();
    SimpleProfiler.stop("JPF-boot"); // is started in JDart.run()
    logger.finest("ConcolicListener.searchStarted(): " + id);
  }


  /*
   * (non-Javadoc)
   * @see gov.nasa.jpf.ListenerAdapter#searchFinished(gov.nasa.jpf.search.Search)
   */
  @Override
  public void searchFinished(Search search) {
    int id = search.getStateId();
    logger.finest("ConcolicListener.searchFinished(): " + id);
  }

//
//
//  /* (non-Javadoc)
//   * @see gov.nasa.jpf.listener.Perturbator#classLoaded(gov.nasa.jpf.vm.VM, gov.nasa.jpf.vm.ClassInfo)
//   */
  @Override
  public void classLoaded(VM vm, ClassInfo ci) {
    SymbolicObjectsContext.analyzeStatic(vm, ci);
    super.classLoaded(vm, ci);
  }



  /* (non-Javadoc)
   * @see gov.nasa.jpf.ListenerAdapter#objectCreated(gov.nasa.jpf.vm.VM, gov.nasa.jpf.vm.ThreadInfo, gov.nasa.jpf.vm.ElementInfo)
   */
  @Override
  public void objectCreated(VM vm, ThreadInfo ti,
      ElementInfo ei) {
    SymbolicObjectsContext.analyzeNewInstance(ti, ei);
    super.objectCreated(vm, ti, ei);
  }
  
  
}
