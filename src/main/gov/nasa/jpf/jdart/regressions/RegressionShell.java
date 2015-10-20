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
package gov.nasa.jpf.jdart.regressions;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.jdart.ConcolicExplorer;
import gov.nasa.jpf.jdart.JDart;
import gov.nasa.jpf.util.LogManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Regression Test Shell. This shell is only used internally for regression tests.
 * It resides within src/main for classpath reasons.
 * 
 */
public class RegressionShell implements JPFShell {
  
  private final Config config;
  
  public RegressionShell(Config conf) {
    this.config = conf;
    LogManager.init(conf);
  }
  
  @Override
  public void start(String[] strings) {
    boolean pass = false;
    try {
      pass = run();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getLogger(RegressionShell.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace();
    }
    error(pass);
  }

  private boolean run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    config.setTarget(config.getProperty("regression.target"));
    JDart dart = new JDart(config, false);    
    ConcolicExplorer exp = dart.run();
    
    Class clazz = Class.forName(config.getProperty("regression.oracle"));
    TestOracle o = (TestOracle)clazz.newInstance();    
    return o.verdict(exp);
  } 
  
  private void error(boolean pass) {
    System.err.println("JDart Regression " + config.getProperty("regression.name"));
    System.err.println("  target:  " + config.getProperty("regression.target"));
    System.err.println("  oracle:  " + config.getProperty("regression.oracle"));
    System.err.println("  verdict: " + (pass ? "pass" : "fail"));        
  }  
}
