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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.jdart.CompletedAnalysis;
import gov.nasa.jpf.jdart.ConcolicExplorer;
import gov.nasa.jpf.jdart.JDart;
import gov.nasa.jpf.jdart.config.ConcolicMethodConfig;
import gov.nasa.jpf.jdart.constraints.ConstraintsTree;
import gov.nasa.jpf.jdart.constraints.Path;
import gov.nasa.jpf.jdart.constraints.PathResult;
import gov.nasa.jpf.util.TemplateBasedCompiler;
import gov.nasa.jpf.jdart.summaries.json.ExpressionHandler;
import gov.nasa.jpf.jdart.summaries.json.SubClassHandler;
import gov.nasa.jpf.jdart.summaries.json.VariableHandler;
import gov.nasa.jpf.util.JPFLogger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * collection of summaries and corresponding jdart configuration.
 * 
 */
public class SummaryStore {
  
  private static transient final JPFLogger logger = JPF.getLogger("psyco");  

  private final Config config;

  private final Map<String, MethodSummary> summaries;
  
  private transient final Map<String, ConcolicMethodConfig> configs;
  
  private final Valuation initial;  
  
  private SummaryStore(
          Config config, 
          Map<String, MethodSummary> summaries, 
          Map<String, ConcolicMethodConfig> configs,
          Valuation initial) {
    this.config = config;
    this.summaries = summaries;
    this.configs = configs;
    this.initial = initial;
  }
  
  private SummaryStore(
          Config config, 
          Map<String, MethodSummary> summaries, 
          Valuation initial) {
    this.config = config;
    this.summaries = summaries;
    this.initial = initial;

    this.configs = new HashMap<>();
    SummaryConfig cfg = new SummaryConfig(config);
    for (ConcolicMethodConfig c : cfg.getSummaryMethods()) {
      this.configs.put(c.getId(), c);
    }    
  }
    
  /* ***************************************************************************
   * 
   * API
   * 
   */  
  
  public Valuation getInitialValuation() {
    return initial;
  }
  
  public ConcolicMethodConfig getConcolicMethodConfig(String id) {
    return configs.get(id);
  }
  
  public Set<String> getConcolicMethodIds() {
    return configs.keySet();
  }
  
  public Collection<ConcolicMethodConfig> getConcolicMethodConfigs() {
    return configs.values();
  }
  
  public MethodSummary getSummary(String id) {
    return summaries.get(id);
  }
  
  public MethodSummary getSummary(ConcolicMethodConfig cmc) {
    return getSummary(cmc.getId());
  }
  
  /* ***************************************************************************
   * 
   * Persistence
   * 
   */
  
  public static void toJson(SummaryStore store, File f) throws IOException {
    Gson gson = getGson();  
    String s = toJson(store);
    FileWriter fw = new FileWriter(f);
    fw.write(s);
    fw.flush();
    fw.close();
  }
  
  public static String toJson(SummaryStore store) {
    Gson gson = getGson();  
    return gson.toJson(store);
  }

  public static SummaryStore fromJson(InputStream is) {
    Gson gson = getGson();
    return gson.fromJson(new InputStreamReader(is), SummaryStore.class);
  }
  
  public static SummaryStore fromJson(String json) {
    Gson gson = getGson();  
    return gson.fromJson(json, SummaryStore.class);
  }
  
  private static Gson getGson() {
    Gson gson = (new GsonBuilder())
            .registerTypeAdapter(PathResult.class, new SubClassHandler<PathResult>())
            .registerTypeAdapter(Expression.class, new ExpressionHandler())
            .registerTypeAdapter(Variable.class, new VariableHandler())
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();  
    
    return gson;
  }

  /* ***************************************************************************
   * 
   * Summary generation
   * 
   */
    
  public static SummaryStore create(Config config) 
          throws IOException {
    
    SummaryConfig cfg = new SummaryConfig(config);
    return create(config, cfg.getSummaryMethods());
  }
      
  public static SummaryStore create(Config config, Collection<ConcolicMethodConfig> cmc) 
          throws IOException {
    
    TemplateBasedCompiler compiler = new TemplateBasedCompiler();    
    Map<String, Object> attribs = new HashMap<>();
    ArrayList<Boolean> constr = new ArrayList<>();
    for (ConcolicMethodConfig c : cmc) {
      constr.add(c.getMethodName().equals("<init>"));
    }
    attribs.put("interfaceMethods", (Object) cmc);
    attribs.put("constructors", (Object) constr);
    
    InputStream tplIs = MethodSummarizer.class.getResourceAsStream(
            "/gov/nasa/jpf/jdart/summaries/SummaryGeneration.st");
    
    compiler.addDynamicSource("__jpf_jdart.summarizer", "SummaryGenerator", attribs, tplIs);    
    File classPath = compiler.compile(config);
        
    config.prepend("classpath", classPath.getAbsolutePath(), ",");
    config.remove("shell");
    
    Map<String, MethodSummary> summaries = new HashMap<>();
    Map<String, ConcolicMethodConfig> methods = new HashMap<>();
    Valuation initial = new Valuation();
    
    for (ConcolicMethodConfig c : cmc) {
      methods.put(c.getId(), c);
    }

    for(ConcolicMethodConfig mw : cmc) {
      MethodSummary summary = summarize(mw, config);
      initial.putAll(summary.getPartialInitialValuation());
      summaries.put(mw.getId(), summary);
      logger.fine("Summary " + mw.getId() + ":");
      logger.fine("\n" + summary);
    }

    return new SummaryStore(config, summaries, methods, initial);
  }

  private static MethodSummary summarize(ConcolicMethodConfig mw, Config c) {
    c.setTarget("__jpf_jdart.summarizer.SummaryGenerator$Exec_" + mw.getId());
    c.setProperty("concolic.method", mw.getId());
    
    JDart jdart = new JDart(c, false);
    ConcolicExplorer cex = jdart.run();
    
    CompletedAnalysis ca = cex.getFirstCompletedAnalysis(mw.getId());
    ConstraintsTree ct = ca.getConstraintsTree();      
    MethodSummary summary = new MethodSummary(mw.getId(), ct, ca.getInitialValuation());

    c.remove("target");
    c.remove("concolic.method");
            
    return summary;
  }
  
}
