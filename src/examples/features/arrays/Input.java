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
package features.arrays;

public class Input {
  
  static public int m1(char[] c, int n) {
    String str = new String(c);
    System.out.println("Parameters - " + str + " " + n);
    int state = 0;
//    if(c == null || c.length == 0)  {
//      return -1;
//    }
    for(int i =0; i < c.length; i++) {
      if(c[i] == '[') state = 1;
      else if (state == 1 & c[i] == '{') state = 2;
      else if (state == 2 & c[i] == '<') state = 3;
      else if (state == 3 & c[i] == '*')  {
        state = 4;
        if(c.length == 15) {
          state = state  + n;
        }
      }  
    }
    return 1;
  }
  
  static public void m2(int i, char[] c) {
    String str = new String(c);
    System.out.println("In TestMe2. Parameters = " + str + " " + i);
    if (i == 0) {
      if (c[0] == c[1]) {
        System.out.println("c[0] == c[1]!");
      } else {
        System.out.println("c[0] != c[1]!");        
      }
    } else if ((i >= 1) && (i <= c.length - 2) && c[i] != c[i + 1]) {
      System.out.println("c[i] != c[i + 1]!");
    } else
      ;
//      assert(false);
  }
  
  public void m3(int i, double[] d) {
    System.out.println("In TestMe3. Parameters = " + i + " " + d[0] + " " + d[1]);
    int k;
    if (i >= 0 && i < 2 && d[i] == 3.141) {
      System.out.println("i >= 0 and i < 2 and d[0] == 3.141");
    } else if (i == 2) {
      for (k = 0; k < i; k++)
        if (d[k] == d[k + 1])
          System.out.println("k = " + k + " d[k] == d[k + 1]");
        else
          System.out.println("k = " + k + " d[k] != d[k + 1]");
    } else
      assert(false);
  }
  
  static public void m4(int i, float d0, float d1, float d2) {
    System.out.println("In TestMe4. Parameters = " + i + " " + d0 + " " + d1 + " " + d2);
    int k;
    if (i >= 0 && i < 2 && d1 == 3.141) {
      System.out.println("i >= 0 and i < 2 and d1 == 3.141");
    } else if (i == 2) {
      for (k = 0; k < i; k++)
        if (k == 0) {
          if (d0 == d1)
            System.out.println("k = " + k + " d0 == d1");
          else
            System.out.println("k = " + k + " d0 != d1");
        } else {
          if (d1 == d2)
            System.out.println("k = " + k + " d1 == d2");
          else
            System.out.println("k = " + k + " d1 != d2");
        }
    } else
      assert(false);
  }
  
  public static void main(String[] args) {
    System.out.println("-------- In main!");
    char[] c = {'a', 'b', '{', '}', 'c', 'd'};
    m1(c, 0);
    Input uber = new Input();
    double[] d = {1.0, 2, 3, 4, 5, 6, 7, 8};

    m2(10, c);
    
    try {
      uber.m3(1, d);
    } catch (Throwable t) {
      System.err.println(t.toString());
    }

    try {
      float d0 = 1;
      float d1 = 2;
      float d2 = 3;
      m4(1, d0, d1, d2);
    } catch (Throwable t) {
      System.err.println(t.toString());
    }
  }
}
