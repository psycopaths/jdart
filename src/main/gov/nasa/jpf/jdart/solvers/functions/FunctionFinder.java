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

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.expressions.AbstractExpressionVisitor;
import gov.nasa.jpf.constraints.expressions.BitvectorExpression;
import gov.nasa.jpf.constraints.expressions.BitvectorNegation;
import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.Negation;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericCompound;
import gov.nasa.jpf.constraints.expressions.PropositionalCompound;
import gov.nasa.jpf.constraints.expressions.QuantifierExpression;
import gov.nasa.jpf.constraints.expressions.UnaryMinus;
import gov.nasa.jpf.constraints.expressions.functions.Function;
import gov.nasa.jpf.constraints.expressions.functions.FunctionExpression;
import gov.nasa.jpf.constraints.types.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author falk
 */
public class FunctionFinder extends AbstractExpressionVisitor<Expression, Collection<FunctionExpression>> {

  private final Map<String, Variable> known = new HashMap<>();

  
  private final Set<Function> knownFunctions;
  
  public FunctionFinder(Set<Function> knownFunctions) {
    this.knownFunctions = knownFunctions;
  }
  
  public Map<String, Variable> getKnown() {
    return this.known;
  }
  
  public Expression<Boolean> getFunctionExpressions(Expression<Boolean> e, Collection<FunctionExpression> list) {
    return visit(e, list);
  }
    
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.api.Variable, D)
   */
  @Override
  public <E> Variable<E> visit(Variable<E> v, Collection<FunctionExpression> data) { 
    return v;
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.Constant, D)
   */
  @Override
  public <E> Constant<E> visit(Constant<E> c,Collection<FunctionExpression> data) { 
    return c;
  }
    
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.Negation, D)
   */
  @Override
  public Negation visit(Negation n,Collection<FunctionExpression> data) { 
    return new Negation(visit(n.getNegated(), data)); 
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.NumericExpressionExpression, D)
   */
  @Override
  public <E>
  NumericBooleanExpression visit(NumericBooleanExpression n,Collection<FunctionExpression> data) { 
    Expression left = visit(n.getLeft(), data);
    Expression right = visit(n.getRight(), data); 
    return new NumericBooleanExpression(left, n.getComparator(), right);
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.CastExpression, D)
   */
  @Override
  public <F,E>
  CastExpression<F,E> visit(CastExpression<F,E> cast,Collection<FunctionExpression> data) { 
    return (CastExpression<F, E>) cast.duplicate(new Expression[] {visit(cast.getCasted(), data) });
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.NumericCompound, D)
   */
  @Override
  public <E>
  NumericCompound<E> visit(NumericCompound<E> n,Collection<FunctionExpression> data) { 
    Expression left = visit(n.getLeft(), data);
    Expression right = visit(n.getRight(), data); 
    return new NumericCompound(left, n.getOperator(), right);

  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.PropositionalCompound, D)
   */
  @Override
  public PropositionalCompound visit(PropositionalCompound n,Collection<FunctionExpression> data) {
    Expression left = visit(n.getLeft(), data);
    Expression right = visit(n.getRight(), data); 
    return new PropositionalCompound(left, n.getOperator(), right);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.UnaryMinus, D)
   */
  @Override
  public <E>
  UnaryMinus<E> visit(UnaryMinus<E> n,Collection<FunctionExpression> data) { 
    return new UnaryMinus(visit(n.getNegated(), data)); 
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.QuantifierExpression, D)
   */
  @Override
  public QuantifierExpression visit(QuantifierExpression q,Collection<FunctionExpression> data) { 
    throw new IllegalStateException("not implemented!");
  }
  
  @Override
  public <E> Expression visit(FunctionExpression<E> f, Collection<FunctionExpression> data) { 
    
    // first process arguments ...
    Expression[] args = new Expression[f.getArgs().length];
    int i = 0;
    for (Expression e : f.getArgs()) {
      args[i++] = visit(e, data);
    }
    
    FunctionExpression feNew = new FunctionExpression(f.getFunction(), args);
    if(this.knownFunctions.contains(feNew.getFunction())) {
      String key = feNew.toString();
      Variable var = known.get(key);
      if (var == null) {
        data.add(feNew);
        var = getVariable(f.getFunction().getReturnType());
        this.known.put(key, var);     
      }
      return var;
    } else {
      return feNew;
    }
  }
  
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.BitvectorExpression, java.lang.Object)
   */
  @Override
  public <E> BitvectorExpression<E> visit(BitvectorExpression<E> bv,Collection<FunctionExpression> data) {
    Expression left = visit(bv.getLeft(), data);
    Expression right = visit(bv.getRight(), data); 
    return new BitvectorExpression(left, bv.getOperator(), right);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.BitvectorNegation, java.lang.Object)
   */
  @Override
  public <E> BitvectorNegation<E> visit(BitvectorNegation<E> n, Collection<FunctionExpression> data) {
    return new BitvectorNegation<>(visit(n.getNegated(), data)); 
  }  
  
  private int idx = 0;
  
  private Variable getVariable(Type t) {
    idx++;
    Variable v = new Variable(t ,"__fct_" + idx);
    return v;
  }
  
}
