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

//
// DISCLAIMER - this file is part of the 'ESAS' demonstration project. As
// such, it is only intended for demonstration purposes, does not contain
// or refer to actual NASA flight software, and is solely derived from
// publicly available information. For further details, please refer to the
// README-ESAS file that is included in this distribution.
//

package summaries.cev;

//import java.util.ArrayList;

/**
 * class keeping a log of model errors (which are not to be confused with
 * modeled HW failures)
 * 
 * advanced topic: use this to show the danger of not closing the state space:
 * if the log list is enabled, it effectively turns off state matching by JPF,
 * unless the 'log' data structure is filtered out by the JPF state management
 */
public class ErrorLog {
  
  //ArrayList<String> log = new ArrayList<String>();
  String error;
  
  public String log (String msg) {
    //log.add(msg);
    error = msg;
    return msg;
  }
  
  public String last () {
    if (error == null) {
      return "no error";
    } else {
      return error;
    }
    
    /**
    if (log.size() > 0){
      return log.get(log.size()-1);
    } else {
      return "no error";
    }
    **/
  }
}
