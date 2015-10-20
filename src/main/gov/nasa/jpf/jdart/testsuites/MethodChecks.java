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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MethodChecks {
  
  private boolean exception = false;
  
  private String exceptionClass = null;
  
  private boolean instanceMeth = false;
  private String className = null;
  
  
  private boolean checks = false;
  
  private List<String> checkCalls = new ArrayList<String>(); 

  public MethodChecks() {
  }
  
  public MethodChecks(String[] checks) {
    for (String c : checks) {
      addCheck(c);
    }
  }  
  
  public MethodChecks(String exception) {
    setExpectedException(exception);    
  }  

  public MethodChecks(String excetion, String[] checks) {
    this(checks);
    setExpectedException(excetion);
  }    
  public final void addCheck(String check) {
    this.checkCalls.add(check);
    this.checks = true;
  } 
  
  public final void setExpectedException(String className) {
    this.exception = true;
    this.exceptionClass = className;
  }
  
  public final void setClassName(String className) {
    this.instanceMeth = true;
    this.className = className;
  }

  /**
   * @return the exception
   */
  public boolean isException() {
    return exception;
  }

  /**
   * @return the exceptionClass
   */
  public String getExceptionClass() {
    return exceptionClass;
  }

  /**
   * @return the checks
   */
  public boolean isChecks() {
    return checks;
  }

  /**
   * @return the checkCalls
   */
  public List<String> getCheckCalls() {
    return checkCalls;
  }

  public boolean isInstanceMeth() {
    return instanceMeth;
  }

  public String getClassName() {
    return className;
  }
    
  
}
