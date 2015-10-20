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

import static java.lang.Math.min;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class TestSuite implements Iterable<TestSubSuite> {

  private int subSuiteSize = 1000;

  private List<TestCase> testCases = new LinkedList<TestCase>();
  
  public TestSuite(Collection<TestCase> tests) {
    this.testCases.addAll(tests);
  }

  public TestSuite(Collection<TestCase> tests, int subSuiteSize) {
    this.testCases.addAll(tests);
    this.subSuiteSize = subSuiteSize;
  }
 
  
  public Iterator<TestSubSuite> iterator() {    
    List<TestSubSuite> subSuites = new LinkedList<TestSubSuite>();
    int offset = 0;
    for (int i=0;i<this.testCases.size(); i+= this.subSuiteSize) { 
      TestSubSuite sub = new TestSubSuite(this.testCases.subList(
              offset, min(offset+this.subSuiteSize, this.testCases.size()) ));
      subSuites.add(sub);
      offset += this.subSuiteSize;
    }    
    return subSuites.iterator();
  }
  
  
}
