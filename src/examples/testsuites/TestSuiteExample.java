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
package testsuites;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.jdart.testsuites.MethodChecks;
import gov.nasa.jpf.jdart.testsuites.MethodWrapper;
import gov.nasa.jpf.jdart.testsuites.TestCase;
import gov.nasa.jpf.jdart.testsuites.TestSuite;
import gov.nasa.jpf.jdart.testsuites.TestSuiteGenerator;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.LogManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 *
 */
public class TestSuiteExample implements JPFShell {
  
  private Config conf;  
  
  public TestSuiteExample(Config conf) {
    this.conf = conf;
    LogManager.init(conf);
    logger = JPF.getLogger("psyco");    
  }
  
  private JPFLogger logger;

  public void start(String[] strings) {
    
    try {
      
      logger.info("START...");

      
      MethodWrapper m1 = new MethodWrapper(
              "java.util.List list0 = new java.util.ArrayList();", "true",
              new MethodChecks(new String[] {"assertNotNull(list0)", "assertTrue(list0.isEmpty())"} ));
      
      MethodWrapper m2 = new MethodWrapper("list0.add(\"TEST\")", "list0.size() > 0");
      
      TestCase test0 = new TestCase(m1, m2);   

      // second test case
      MethodWrapper m3 = new MethodWrapper("(new java.util.ArrayList()).clear();", "true",
              new MethodChecks("java.lang.NullPointerException"));
      
      TestCase test1 = new TestCase(m3);  
      
      // create suite   
      Collection<TestCase> tests = new ArrayList<TestCase>();   
      tests.add(test0);
      tests.add(test1);   
      TestSuite suite = new TestSuite(tests,1);
      
      File tmpSrcDir = File.createTempFile("jdart", "tests");
      tmpSrcDir.delete();
      tmpSrcDir.mkdir();
      
      // generate
      TestSuiteGenerator gen = new TestSuiteGenerator(
              suite, "DemoSuite", "temp", tmpSrcDir.getAbsolutePath() );
      
      try {
        gen.generate();
      } catch (IOException e) {
        e.printStackTrace();
      }  
        
    } catch (IOException ex) {
      Logger.getLogger(TestSuiteExample.class.getName()).log(Level.SEVERE, null, ex);
    }  
      
  }  
  
}
