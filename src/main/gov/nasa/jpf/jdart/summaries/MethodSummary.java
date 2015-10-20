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
package gov.nasa.jpf.jdart.summaries;

import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.jdart.constraints.ConstraintsTree;
import gov.nasa.jpf.jdart.constraints.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A method summary is a collection of paths. 
 */
public class MethodSummary implements Iterable<Path> {
    
  private Collection<Path> okPaths;
  private Collection<Path> errorPaths;
  private Collection<Path> dontKnowPaths;  
  private String methodId;
  
  private Valuation partialInitialValuation = null;
  
    
  public MethodSummary(String mid, ConstraintsTree ct, Valuation partialInitial) {
    this(mid, ct);
    this.partialInitialValuation = partialInitial;
  }
  
  public MethodSummary(String methodId, ConstraintsTree ct) {
    this.okPaths = new ArrayList<>(ct.getCoveredPaths());
    this.errorPaths = new ArrayList<>(ct.getErrorPaths());
    this.dontKnowPaths = new ArrayList<>(ct.getDontKnowPaths());
    this.methodId = methodId;            
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Summary of ");
    sb.append(this.methodId).append("\n");
    for (Path p : okPaths)
      sb.append(p).append("\n");
    for (Path p : errorPaths)
      sb.append(p).append("\n");
    for (Path p : dontKnowPaths)
      sb.append(p).append("\n");
    
    return sb.toString();
  }
  
  /**
   * @return the okPaths
   */
  public Collection<Path> getOkPaths() {
    return okPaths;
  }

  /**
   * @return the errorPaths
   */
  public Collection<Path> getErrorPaths() {
    return errorPaths;
  }

  /**
   * @return the dontKnowPaths
   */
  public Collection<Path> getDontKnowPaths() {
    return dontKnowPaths;
  }

  /**
   * @return the methodId
   */
  public String getMethodId() {
    return methodId;
  }
  
  /**
   * @return the partialInitialValuation
   */
  public Valuation getPartialInitialValuation() {
    return partialInitialValuation;
  }

  @Override
  public Iterator<Path> iterator() {
    List<Path> allpaths = new ArrayList<>();
    allpaths.addAll(this.okPaths);
    allpaths.addAll(this.errorPaths);
    allpaths.addAll(this.dontKnowPaths);
    return allpaths.iterator();
  }
}
