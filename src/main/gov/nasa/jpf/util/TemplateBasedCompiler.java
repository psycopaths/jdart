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
package gov.nasa.jpf.util;

import gov.nasa.jpf.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.stringtemplate.v4.ST;

/**
 * Compiles source generated from a string template
 */
public class TemplateBasedCompiler {
  
  private final File tmpSrcDir;
  
  private final List<File> sourceFiles = new ArrayList<>();
  
  
  public TemplateBasedCompiler() throws IOException {
    this.tmpSrcDir = File.createTempFile("jpf-jdart", "src");
    tmpSrcDir.delete();
    tmpSrcDir.mkdir();
    
  }
  
  public TemplateBasedCompiler(File outdir) {
    this.tmpSrcDir = outdir;
    tmpSrcDir.mkdirs();    
  } 
  

  public void addDynamicSource(String packageName, String className, Map<String,Object> attributes, InputStream tplIs) throws IOException {
    StringBuilder sb = new StringBuilder();
    try(BufferedReader r = new BufferedReader(new InputStreamReader(tplIs))) {
      String line;
      while((line = r.readLine()) != null) {
        sb.append(line).append('\n');
      }
    }
    
    ST tpl = new ST(sb.toString());
    
    for(Map.Entry<String, Object> e : attributes.entrySet()) {
      tpl.add(e.getKey(), e.getValue());
    }
    tpl.add("package", packageName);
    tpl.add("class", className);
    
    String packagePath = packageName.replace('.', File.separatorChar);
    
    File outputDir = new File(tmpSrcDir, packagePath);
    outputDir.mkdirs();
    
    File outputFile = new File(outputDir, className + ".java");
    
    try(FileWriter fw = new FileWriter(outputFile)) {
      fw.write(tpl.render());
    }
    
    sourceFiles.add(outputFile);
  }
  
  public File compile(Config config) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    fileManager.setLocation(StandardLocation.SOURCE_PATH, Collections.singleton(tmpSrcDir));
    
    File tmpClassDir = File.createTempFile("jpf-testing", "classes");
    tmpClassDir.delete();
    tmpClassDir.mkdir();
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(tmpClassDir));
    
    File[] cp = config.getPathArray("classpath");
    fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(cp));
    
    CompilationTask task = compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(sourceFiles));
    if(!task.call())
      throw new RuntimeException("Compilation failed");
    
    return tmpClassDir;
  }
  
  
  
}
