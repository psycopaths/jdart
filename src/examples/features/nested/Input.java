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
package features.nested;

public class Input {

  public int foo(int i) {
    System.err.println("\n-------- In foo! Parameter = " + i);

    if (i > 64) {
      System.err.printf("%s\n", "i > 64");
      if ( 5 * i <= 325) {
        System.err.printf("%s\n", "5 * i <= 325");
        if (i != 65) {
          System.err.printf("%s\n", "i != 65");
        } else {
          System.err.printf("%s\n", "i == 65");            
        }
        return i;
      } else if ( i != 66 ) {
        System.err.printf("%s\n", "5 * i > 325 && i != 66");
        return i + 1;
      } else {
        System.err.printf("%s\n", "5 * i > 325 && i == 66");
        assert(false);
      }
    }
    System.err.printf("%s\n", "i <= 64");
    return i + 2;
  }
  
  public void bar(double d) {
    System.err.println("\n-------- In bar! Parameter = " + d);

    if (d >= 3.141) {
      System.err.printf("%s\n", "d >= 3.141");
    } else {
      int i = (int)d + 65;
      int j = foo(i);
      if (j == i) {
        System.err.println("In bar j == i");
      }
    }
  }
  
  public static void main(String[] args) {
    
    double test = -0.5;
    int i = (int)test;
    
    System.out.println("-------- In main! : " + i);
    Input inst = new Input();
    inst.bar(1.732);
  }
}
