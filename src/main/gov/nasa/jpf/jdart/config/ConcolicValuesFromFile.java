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
package gov.nasa.jpf.jdart.config;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.util.JPFLogger;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

/**
 *
 */
public class ConcolicValuesFromFile extends ConcolicValues {
  
  private final JPFLogger logger = JPF.getLogger("jdart");

  private String nextLine = null;
  
  private BufferedReader reader;
  
  private final MethodSpec spec;
  
  public ConcolicValuesFromFile(String filename, MethodSpec spec) {
    this.spec = spec;
    BufferedReader r = null;
    try {
      r = new BufferedReader(new FileReader(filename));
    } catch (FileNotFoundException ex) {
      logger.log(Level.SEVERE, null, ex);      
    }
    this.reader = r;
    this.nextLine = fetchNextLine();
  }

  @Override
  public boolean hasNext() {
    return nextLine != null;
  }

  @Override
  public Valuation next() {
    Valuation val = parseValuation(nextLine, spec);
    nextLine = fetchNextLine();
    return val;
  }
  
  private String fetchNextLine() {
    if (this.reader == null) {
      return null;
    }
    
    String line = null;
    try {
      line = reader.readLine();
      if (line != null && line.trim().length() < 1) {
        line = null;
      }
    } catch (IOException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
    
    if (line == null) {
      try {
        reader.close();
      } catch (IOException ex) {
        logger.log(Level.SEVERE, null, ex);
      }
      reader = null;
    }
    
    return line;
  }
  
}
