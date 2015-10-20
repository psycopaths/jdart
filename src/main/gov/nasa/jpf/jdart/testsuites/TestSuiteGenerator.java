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
package gov.nasa.jpf.jdart.testsuites;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.jdart.CompletedAnalysis;
import gov.nasa.jpf.jdart.config.ConcolicMethodConfig;
import gov.nasa.jpf.jdart.config.ParamConfig;
import gov.nasa.jpf.jdart.constraints.Path;
import gov.nasa.jpf.util.TemplateBasedCompiler;
import gov.nasa.jpf.vm.ClassPath;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TestSuiteGenerator {
  
  private TestSuite suite;
  
  private String suiteName;
  
  private String packageName;
  
  private String outDir;

  public TestSuiteGenerator(TestSuite suite, String suiteName, String packageName, String outDir) {
    this.suite = suite;
    this.suiteName = suiteName;
    this.packageName = packageName;
    this.outDir = outDir;
  }  
  
  public void generate() throws IOException {

    TemplateBasedCompiler compiler = new TemplateBasedCompiler(new File(outDir));
    
    int parts = 0;
    for (TestSubSuite sub : suite) {
      Map<String,Object> subInfo = new HashMap<>();
      subInfo.put("tests", sub.getTests());
                 
      compiler.addDynamicSource(packageName, this.suiteName + (parts++), subInfo,
        TestSuiteGenerator.class.getResourceAsStream("/gov/nasa/jpf/jdart/testsuites/SubSuite.st"));            
    }    
    
    // generate suite file
    Map<String,Object> suiteInfo = new HashMap<String,Object>();
    //suiteInfo.put("package", packageName);
    //suiteInfo.put("suite", suiteName);
    List<String> partNames = new ArrayList<String>();
    for (int i=0;i<parts;i++) {
      partNames.add( this.suiteName + i);
    }
    suiteInfo.put("parts", partNames);

    compiler.addDynamicSource(packageName, this.suiteName, suiteInfo,
      TestSuiteGenerator.class.getResourceAsStream("/gov/nasa/jpf/jdart/testsuites/TestSuite.st"));            
        
  }
  
  public void run() {
    throw new IllegalStateException("not implemented yet.");
  }
  
  public static TestSuiteGenerator fromAnalysis(CompletedAnalysis analysis, Config conf) {
    String dir = conf.getString("jdart.tests.dir");
    String pkg = conf.getString("jdart.tests.pkg");
    ConcolicMethodConfig mc =analysis.getMethodConfig();
    String suiteName = conf.getString("jdart.tests.suitename", "Tests" + 
        mc.getMethodName().substring(0, 1).toUpperCase() + 
        mc.getMethodName().substring(1));
    
    boolean staticMeth = true;
    try {
      String[] paths = conf.getStringArray("classpath");
      URL[] urls = new URL[paths.length];
      int i = 0;
      for(String p : paths)
        urls[i++] = new File(p).toURI().toURL();
      
      URLClassLoader uc = new URLClassLoader(urls);
      Class<?> cls = uc.loadClass(mc.getClassName());
      //Class<?> cls = uc.loadClass(conf.getTarget());
      //overloading not supported -- if necessary, make map from types in
      //jdart method spec to parametertypes in Class.getmethod
      Method[] ms = cls.getMethods();
      Method targetMethod = null;
      for(Method m : ms) {
        if(m.getName().equals(mc.getMethodName())) {
          targetMethod = m;
          break;
        }
      }
      uc.close();
      assert targetMethod != null;
      
      if(!Modifier.isStatic(targetMethod.getModifiers())) {
        staticMeth = false;
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
    
    String callBase = (staticMeth) ? mc.getClassName() + "." + mc.getMethodName() : mc.getMethodName();

    ArrayList<TestCase> tests = new ArrayList<>();
    for (Path p : analysis.getConstraintsTree().getAllPaths()) {
      Valuation val = p.getValuation();
      if (val == null) {
        // dont know cases
        continue;
      }

      String call = callBase + "(";
      if (mc.getParams().size() > 0) {
        int i = 0;
        for (ParamConfig pc : mc.getParams()) {
          Object objVal = val.getValue(pc.getName());
          if(objVal == null) {//the parameter is treated as concrete
            objVal = analysis.getInitParams()[i];
          }
          call += objVal + ((objVal instanceof Float) ? "f" : "") + ",";
          i++;
        }      
        call = call.substring(0, call.length() -1);
      }
      call += ")";
      MethodWrapper mw;
      if(staticMeth)
        mw = new MethodWrapper(call, "true");
      else {
        MethodChecks mcs = new MethodChecks();
        mcs.setClassName(mc.getClassName());
        //mcs.setClassName(conf.getTarget());
        mw = new MethodWrapper(call, "true", mcs);
      }
      TestCase tc = new TestCase(mw);
      tests.add(tc);
    }
    
    TestSuite suite = new TestSuite(tests);
    return new TestSuiteGenerator(suite, suiteName, pkg, dir);    
  }
  
}
