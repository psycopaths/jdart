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
package gov.nasa.jpf.jdart.solvers.selective;

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
import gov.nasa.jpf.constraints.expressions.functions.FunctionExpression;

/**
 *
 */
public class FunctionFilter extends AbstractExpressionVisitor<Boolean, Void> implements ExpressionFilter {

  @Override
  public boolean submitToSolver(Expression<Boolean> e) {
    return visit(e);
  }
    
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.api.Variable, D)
   */
  @Override
  public <E> Boolean visit(Variable<E> v,Void data) { 
    return true;
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.Constant, D)
   */
  @Override
  public <E> Boolean visit(Constant<E> c,Void data) { 
    return true; 
  }
    
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.Negation, D)
   */
  @Override
  public Boolean visit(Negation n,Void data) { 
    return visit(n.getNegated()); 
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.NumericBooleanExpression, D)
   */
  @Override
  public <E>
  Boolean visit(NumericBooleanExpression n,Void data) { 
    return visit(n.getLeft()) && visit(n.getRight()); 
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.CastExpression, D)
   */
  @Override
  public <F,E>
  Boolean visit(CastExpression<F,E> cast,Void data) { 
    return visit(cast.getCasted());
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.NumericCompound, D)
   */
  @Override
  public <E>
  Boolean visit(NumericCompound<E> n,Void data) { 
    return visit(n.getLeft()) && visit(n.getRight()); 
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.PropositionalCompound, D)
   */
  @Override
  public Boolean visit(PropositionalCompound n,Void data) {
    return visit(n.getLeft()) && visit(n.getRight()); 
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.UnaryMinus, D)
   */
  @Override
  public <E>
  Boolean visit(UnaryMinus<E> n,Void data) { 
    return visit(n.getNegated(), data); 
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.QuantifierExpression, D)
   */
  @Override
  public Boolean visit(QuantifierExpression q,Void data) { 
    return visit(q.getBody(), data); 
  }
  
  @Override
  public <E> Boolean visit(FunctionExpression<E> f,Void data) { 
    return false;
  }
  
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.BitvectorExpression, java.lang.Object)
   */
  @Override
  public <E> Boolean visit(BitvectorExpression<E> bv,Void data) {
    return visit(bv.getLeft()) && visit(bv.getRight()); 
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.constraints.api.ExpressionVisitor#visit(gov.nasa.jpf.constraints.expressions.BitvectorNegation, java.lang.Object)
   */
  @Override
  public <E> Boolean visit(BitvectorNegation<E> n,Void data) {
    return visit(n.getNegated()); 
  }  
}
