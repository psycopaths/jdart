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
package features.nhandler;

import java.util.Random;
public class Input {

  public int foo(int i) {
    System.err.println("\n-------- In foo! Parameter = " + i);
    Random rand = new Random();
    int x = 0; //rand.nextInt(i);
    System.out.println("x: " + x);
    if (x > 100) {
      assert false;
    }
    
    String text = "_J_";
    char c = text.charAt(i);
    System.out.println("c: " + c);
    if (c == 'A') {
      assert false;
    }
    
    return i;
 }

  public static void main(String[] args) {
    System.out.println("-------- In main!");
    Input i = new Input();
    i.foo(1);

  }
}
