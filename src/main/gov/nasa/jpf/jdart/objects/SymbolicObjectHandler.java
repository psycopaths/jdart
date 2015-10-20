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
package gov.nasa.jpf.jdart.objects;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;

/**
 * Interface for handlers that enable symbolic handling of objects.
 * 
 *
 */
interface SymbolicObjectHandler {
 
  /**
   * Initializes a class with this handler. Note that this
   * method is called at most once per {@link ClassInfo} object.
   * 
   * Any {@link ClassInfo} object may be passed to this method. If
   * it refers to one of the classes the handler is designed for,
   * initializations should be performed and a value of
   * <tt>true</tt> be returned. Otherwise, <tt>false</tt> should
   * be returned and no action be taken.
   * 
   * @param ci the class info.
   * @return <tt>true</tt> if the handler matches the specified class,
   * <tt>false</tt> otherwise.
   */
  public boolean initialize(ClassInfo ci);
  
  
  /**
   * Annotates an object with symbolic information, using <code>name</code>
   * as the name prefix for this object.
   * 
   * @param ei the object to annotate
   * @param name the symbolic name of this object
   * @param ctx the context
   */
  public void annotateObject(ElementInfo ei, String name, SymbolicObjectsContext ctx);

}
