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
package gov.nasa.jpf.jdart.solvers.functions;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.ConstraintSolver;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.ValuationEntry;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.expressions.functions.Function;
import gov.nasa.jpf.constraints.expressions.functions.FunctionExpression;
import gov.nasa.jpf.constraints.expressions.functions.math.BooleanDoubleFunction;
import gov.nasa.jpf.constraints.expressions.functions.math.MathFunctions;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.AcosProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.AsinProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.Atan2Properties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.CosProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.ExpProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.FunctionProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.IsNaNProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.LogProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.SinProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.SqrtProperties;
import gov.nasa.jpf.constraints.expressions.functions.math.axioms.TanProperties;
import gov.nasa.jpf.util.JPFLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FunctionSolverContext extends SolverContext {
    
  /**
   * stack frame
   */
  private static class StackElement {    

    /**
     * expressions at this level
     */
    final ArrayList<Expression<Boolean>> exprsn = new ArrayList<>();

    /**
     * function properties to be asserted
     */
    final Set<FunctionProperties> properties = new HashSet<>();

    /**
     * function expressions added at this level
     */
    final Set<FunctionExpression> functions = new HashSet<>();

    /**
     * cached elements added at this level
     */
    final ArrayList<String> cached = new ArrayList<>();
  }
     
  /**
   * solver stack
   */
  private final ArrayList<StackElement> stack = new ArrayList<>();
  
  /** 
   * top of stack
   */
  private StackElement current;
 
  /**
   * logger
   */
  private final JPFLogger logger = JPF.getLogger("functions");
    
  /**
   * table function properties
   */
  private final Map<Function, FunctionProperties> lookup = new HashMap<>();
    
  /**
   * expression visitor for pre-processing 
   */
  private final FunctionFinder finder;
  
  /**
   * actual solver context
   */
  private final SolverContext ctx;
  
  /** 
   * make assertions on the domains of functions?
   */
  private final boolean useDomainBounds;

  /** 
   * make assertions on the range of functions?
   */
  private final boolean useRangeBounds;
    
  /** 
   * use function definitions?
   */
  private final boolean useDefinitions;
  
  /**
   * instantiate definitions for every occurrence ?
   */
  private final boolean instantiate;
 
  private final int precisionElem = 11;//11; //46;
  
  private final int precisionFun = 5; //15;
    
  /**
   * cache of already asserted expressions
   */
  private final Map<String, Boolean> cache = new HashMap<>();
  
  public FunctionSolverContext(SolverContext ctx, boolean useDomainBounds, boolean useRangeBounds,
          boolean useDefinitions, boolean instantiate) {
    this.ctx = ctx;
    this.useDomainBounds = useDomainBounds;
    this.useRangeBounds = useRangeBounds;
    this.useDefinitions = useDefinitions;
    this.instantiate = instantiate;
    
    // load function tables
    // TODO: maybe expose precision as parameter? 
    
    SinProperties sin = new SinProperties(precisionElem);
    lookup.put(MathFunctions.SIN, sin);
    lookup.put(MathFunctions.COS, new CosProperties(sin));
    lookup.put(MathFunctions.TAN, new TanProperties(precisionElem));
    lookup.put(MathFunctions.ASIN, new AsinProperties(precisionElem));
    lookup.put(MathFunctions.ACOS, new AcosProperties());

    SqrtProperties sqrt = new SqrtProperties(precisionFun);
    lookup.put(MathFunctions.SQRT,sqrt); 
    lookup.put(MathFunctions.ATAN2, new Atan2Properties(precisionElem, sqrt));
    lookup.put(MathFunctions.LOG, new LogProperties(precisionFun)); 
    lookup.put(MathFunctions.EXP, new ExpProperties(10)); 
    lookup.put(BooleanDoubleFunction.DOUBLE_IS_NAN, new IsNaNProperties());

    
    finder = new FunctionFinder(lookup.keySet());
    
    // init stack
    current = new StackElement();
    stack.add(current);
  }
    
  @Override
  public void push() {
    ctx.push();
    current = new StackElement();
    stack.add(current);
  }

  @Override
  public void pop(int n) {
    for (int i=0; i<n; i++) {
      // remove cached function expressions
      for (FunctionExpression fe : current.functions) {
        String key = fe.toString();
        assert finder.getKnown().containsKey(key);        
        this.finder.getKnown().remove(key);
      }
      // remove other cached expressions
      for (String key : current.cached) {
        cache.remove(key);
      }
      // pop stack frame
      stack.remove(stack.size()-1);
      current = stack.get(stack.size()-1);
    }
    // call pop on real context
    ctx.pop(n);
  }

  @Override
  public ConstraintSolver.Result solve(Valuation vltn) {
    
    if (this.current.cached.isEmpty()) {
      logger.finer("[FSOLVER][BASE]" +ConstraintSolver.Result.UNSAT + 
              ". No original conjunct. ");
      return ConstraintSolver.Result.UNSAT;              
    }
    
    // collect all needed function properties from stack
    Set<FunctionProperties> props = getDefinitions();
    ConstraintSolver.Result res;
    Valuation vals = new Valuation();
    
    // first try to use range bounds only    
    if (useRangeBounds) {
      logger.finest("trying range bounds only.");    
      ctx.push();

      if (instantiate) {
        for (FunctionExpression fe : getFunctions()) {
          String key = fe.toString();
          Variable var = this.finder.getKnown().get(key);      
          FunctionProperties prop = this.lookup.get(fe.getFunction());      
          ctx.add(prop.getRangeBounds(var));
        }
      } 
      else {    
        for (FunctionProperties fp : props) {
          logger.finest("  --- adding " + fp.getClass().getName());    
          ctx.add(fp.getRangeBounds());
        }
      }

      res = ctx.solve(vals);
      stripCopy(vals, vltn);
      ctx.pop();
      if (res == ConstraintSolver.Result.UNSAT) {
        logger.finer("[FSOLVER][RANGES] " + res);      
        return res;
      }

      if (!useDefinitions) {
        if (res == ConstraintSolver.Result.DONT_KNOW) {
          logger.finer("[FSOLVER][RANGES] " + res);      
          return res;
        }

        return verifyValuation(vltn, true, "[RANGES]");
      }

      if (res == ConstraintSolver.Result.SAT) {
        res = verifyValuation(vltn, false, "");
        if (res == ConstraintSolver.Result.SAT) {
          logger.finer("[FSOLVER][RANGES] " + res);
          return res;
        }
      }
    }
    
    // now try using function definitions too
    logger.finest("trying to use function definitions.");    
    ctx.push();
    
    if (instantiate) {
      for (FunctionExpression fe : getFunctions()) {
        String key = fe.toString();
        Variable var = this.finder.getKnown().get(key);      
        FunctionProperties prop = this.lookup.get(fe.getFunction());

        ctx.add(prop.getDefinition(var, fe.getArgs()));
      }
    } 
    else {
    
      boolean cos = false;
      boolean sin = false;
      boolean acos = false;
      boolean asin = false;
      boolean atan2 = false;
      boolean sqrt = false;
      for (FunctionProperties fp : props) {
        logger.finest("  --- adding " + fp.getClass().getName());    
        ctx.add(fp.getDefinition());
        if (fp.getClass().equals(CosProperties.class)) {
          cos = true;
        }
        if (fp.getClass().equals(SinProperties.class)) {
          sin = true;
        }
        if (fp.getClass().equals(AsinProperties.class)) {
          asin = true;
        }
        if (fp.getClass().equals(AcosProperties.class)) {
          acos = true;
        }
        if (fp.getClass().equals(Atan2Properties.class)) {
          atan2 = true;
        }
        if (fp.getClass().equals(SqrtProperties.class)) {
          sqrt = true;
        }
      }    

      if (cos && !sin) {
        logger.finest("  --- adding " + lookup.get(MathFunctions.SIN).getClass().getName());    
        ctx.add(lookup.get(MathFunctions.SIN).getDefinition());
      }

      if (acos && !asin) {
        logger.finest("  --- adding " + lookup.get(MathFunctions.ASIN).getClass().getName());    
        ctx.add(lookup.get(MathFunctions.ASIN).getDefinition());
      }

      if (atan2 && !sqrt) {
        logger.finest("  --- adding " + lookup.get(MathFunctions.SQRT).getClass().getName());    
        ctx.add(lookup.get(MathFunctions.SQRT).getDefinition());
      }
    }
    
    //System.out.println(ctx);   
    
    res = ctx.solve(vals);
    stripCopy(vals, vltn);    
    ctx.pop();

    if (res != ConstraintSolver.Result.SAT) {      
      logger.finer("[FSOLVER][DEFS] " +  ConstraintSolver.Result.DONT_KNOW + " (" + res + ")");      
      return ConstraintSolver.Result.DONT_KNOW;
    }

    return verifyValuation(vltn, true, "[DEFS]");
  }

  
  private ConstraintSolver.Result verifyValuation(Valuation val, boolean log, String text) {
    try {
      if (evaluate(val)) {
        if (log) logger.finer("[FSOLVER]" + text + " " + ConstraintSolver.Result.SAT);              
        return ConstraintSolver.Result.SAT;
      } 
      if (log) logger.finer("[FSOLVER]" + text + " " +ConstraintSolver.Result.DONT_KNOW + 
              ". Solution diverges in Java: " + val);
      return ConstraintSolver.Result.DONT_KNOW;    
    } catch (UnsupportedOperationException e) {
    }         
    if (log) logger.finer("[FSOLVER]" + text + " " +ConstraintSolver.Result.DONT_KNOW + 
            ". Error during evaluation." );      
    return ConstraintSolver.Result.DONT_KNOW;    
  }
  
  @Override
  public void add(List<Expression<Boolean>> list) { 
    for (Expression<Boolean> e : list) {
      addExpression(e);
    }
  }
  
  private void addExpression(Expression<Boolean> e) {
    
    Collection<FunctionExpression> list = new ArrayList<>();
    Expression<Boolean> eNew = finder.getFunctionExpressions(e, list);

    String ekey = eNew.toString();    
    if (cache.containsKey(ekey)) {
      return;
    }
          
    // iterate over new function expressions
    for (FunctionExpression fe : list) {

      // add to function cache
      current.functions.add(fe);
      
      // assert (fct == var) 
      String key = fe.toString();      
      assert finder.getKnown().containsKey(key);
      if (!instantiate) {
        Variable var = finder.getKnown().get(key);      
        ctx.add(new NumericBooleanExpression(var, NumericComparator.EQ, fe));
      }
      // remember corresponding function properties
      FunctionProperties p = this.lookup.get(fe.getFunction());
      if (p == null) {
        logger.warning("Discovered unsupported function: " + fe.getFunction());
        System.out.println(fe);
        continue;
      }
      current.properties.add(p);

      // assert domain bounds on var
      if (useDomainBounds) {
        ctx.add(p.getDomainBounds(fe.getArgs()));
      }
    }
    
    // assert modified expr and  add to cache, 
    // remember orig. for later evaluation
    ctx.add(eNew);    
    current.exprsn.add(e);   
    current.cached.add(ekey);
    cache.put(ekey, true);
  } 
          

  @Override
  public void dispose() {
    ctx.dispose();
  }
  
  private Set<FunctionProperties> getDefinitions() {
    Set<FunctionProperties> props = new HashSet<>();
    for (StackElement se : stack) {
      props.addAll(se.properties);
    }
    return props;
  }
  
  private Set<FunctionExpression> getFunctions() {
    Set<FunctionExpression> props = new HashSet<>();
    for (StackElement se : stack) {
      props.addAll(se.functions);
    }
    return props;
  }
  
  
  private boolean evaluate(Valuation vltn) {
    for (StackElement se : stack) {
      for (Expression<Boolean> expr : se.exprsn) {
        if (!expr.evaluate(vltn)) {
          logger.finest(vltn + " |/= " + expr);
          return false;
        }
      }
    }
    return true;
  }    
    
  private void stripCopy(Valuation from, Valuation to) {
    for (ValuationEntry e : from.entries()) {
      if (!e.getVariable().getName().startsWith("__fct") &&
              !e.getVariable().getName().startsWith("__axiom")) {
        to.addEntry(e);
      }
    }
  }
  
}
