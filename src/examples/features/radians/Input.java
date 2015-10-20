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
package features.radians;

public class Input {

  public void foo(int i) {
    System.err.println("\n-------- In foo! Parameter = " + i);
    String classPath = System.getProperties().getProperty("java.class.path");
    System.out.println("Class path found: " + classPath);

    double x = java.lang.Math.toRadians(i);
    if (x > 0)
      System.out.println("Radians is positively radiant!");
    else
      System.out.println("Radians is highly negative on all topics$%@#^%");
  }
  
  public static void main(String[] args) {
    System.out.println("-------- In main!");
    Input inst = new Input();
    inst.foo(400);
    }
  }
