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
import gov.nasa.jpf.perturb.OperandPerturbator;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;

/**
 * returns JDartChoiceGenerators. No actual perturbation is done.
 * 
 */
public class ConcolicPerturbator implements OperandPerturbator {

  /**
   * logger 
   */
  private JPFLogger logger = JPF.getLogger("jdart");
  
  /**
   * info about perturbed method
   */
  private MethodInfo methodInfo = null;
  
  private final ConcolicExplorer explorer;
  
  private final String id;
 
  
  public ConcolicPerturbator (Config conf, String keyPrefix){
    logger.finest("Producer(): " + keyPrefix);
    id = keyPrefix.substring(8);
    explorer = JDart.getConcolicExplorer(conf);
  }
  
  

  /**
   * returns a JDartChoiceGenerator
   * 
   * @param id
   * @param frame
   * @param refObject
   * @return 
   */
  @Override
  public ChoiceGenerator<?> createChoiceGenerator(
          String id, StackFrame frame, Object refObject) {
  
    logger.finest("createCG");
    if(explorer.hasCurrentAnalysis()) {
      logger.finest("Denying choice generator creation because concolic analysis is already running ...");
      return null;
    }

    // We expect that the refObject in this case will be a MethodInfo object
    // Set it so that we can create valuation vectors
    assert refObject instanceof MethodInfo : 
            "wrong refObject type for GenericDataAbstractor: " + 
            refObject.getClass().getName();
    
    MethodInfo mi = (MethodInfo)refObject;

    // remember method info
    setMethodInfo(mi, frame);
    
    explorer.newAnalysis(this.id, mi, frame);

    // create new choice generator
    return new JDartChoiceGenerator(id, (MethodInfo)refObject, explorer);
  }
  
  /**
   * nothing to do inside this method. all the action is taken from inside the
   * concolic listener.
   * 
   * @param cg
   * @param frame
   * @return 
   */
  @Override
  public boolean perturb(ChoiceGenerator<?>cg, StackFrame frame) {    
    logger.finest("Producer.perturb(): " + methodInfo.getFullName());    
    
    // TODO: works for now. check if this is the expected behavior.
    if(explorer.hasMoreChoices()) {
      explorer.newPath(frame);
      return true;
    }
    return false;
  }

  @Override
  public Class<? extends ChoiceGenerator<?>> getChoiceGeneratorType(){
    logger.finest("Producer.getChoiceGeneratorType()");    
    return JDartChoiceGenerator.class;
  }

  /*
   * private helper
   */
  private void setMethodInfo(MethodInfo m, StackFrame frame) {
    // init happened already? 
    if (this.methodInfo != null)
      return;

    this.methodInfo = m;
  }
}
