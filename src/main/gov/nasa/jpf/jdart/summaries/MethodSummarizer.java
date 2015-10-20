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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.jdart.config.ConcolicMethodConfig;

import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.LogManager;
import java.io.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPF Shell for the summarizer.
 * 
 */
public class MethodSummarizer implements JPFShell {

  private final Config config;
  private final JPFLogger logger;  

  private Collection<ConcolicMethodConfig> targets = null;
  
  public MethodSummarizer(Config conf) {
    this.config = conf;
    LogManager.init(conf);
    logger = JPF.getLogger("jdart");
  }

  @Override
  public void start(String[] strings) {
    try {
      run();
    } catch (IOException ex) {
      Logger.getLogger(MethodSummarizer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private void run() throws IOException {

    SummaryStore store = SummaryStore.create(config);    
 
    if (!config.hasValue("jdart.summarystore")) {      
      System.err.println(SummaryStore.toJson(store));
      return;
    }
       
    File file = new File(config.getString("jdart.summarystore"));    
    SummaryStore.toJson(store, file);
    System.err.println("Output written to " + file);
  }

}
