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
package gov.nasa.jpf.jdart.constraints;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.ExpressionVisitor;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.expressions.AbstractExpression;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.constraints.types.NumericType;
import gov.nasa.jpf.constraints.types.Type;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * comparison between numbers
 */
public class NumericCMP extends AbstractExpression<Integer> {

  private final Expression<?> left;
  private final Expression<?> right;

  public NumericCMP(Expression<?> left, Expression<?> right) {
    assert left.getType() instanceof NumericType;
    assert right.getType() instanceof NumericType;
    this.left = left;
    this.right = right;
  }
  
  @Override  
  public Integer evaluate(Valuation values) {
    return doEvaluate(left, right, values);      
  }
  
  private static <L,R> int doEvaluate(Expression<L> left, Expression<R> right, Valuation values) {
    NumericType<L> lt = (NumericType<L>)left.getType();
    NumericType<R> rt = (NumericType<R>)right.getType();
    
    L lv = left.evaluate(values);
    R rv = right.evaluate(values);
    
    BigDecimal lnum = lt.decimalValue(lv), rnum = rt.decimalValue(rv);
    
    return lnum.compareTo(rnum);
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NumericCMP other = (NumericCMP) obj;
    if (this.left != other.left && (this.left == null || !this.left.equals(other.left))) {
      return false;
    }
    if (this.right != other.right && (this.right == null || !this.right.equals(other.right))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 37 * hash + (this.left != null ? this.left.hashCode() : 0);
    hash = 37 * hash + (this.right != null ? this.right.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "(" + this.left.toString() + " CMP " + this.right.toString() +")";
  }
  
  /**
   * @return the left
   */
  public Expression<?> getLeft() {
    return this.left;
  }

  /**
   * @return the right
   */
  public Expression<?> getRight() {
    return this.right;
  }

  @Override
  public void print(Appendable a, int flags) throws IOException {
    a.append('(');
    left.print(a, flags);
    a.append(" CMP ");
    right.print(a, flags);
    a.append(')');
  }

  @Override
  public <R, D> R accept(ExpressionVisitor<R, D> arg0, D arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void collectFreeVariables(Collection<? super Variable<?>> vars) {
    left.collectFreeVariables(vars);
    right.collectFreeVariables(vars);
  }

  @Override
  public Expression<?> duplicate(Expression<?>[] newChildren) {
    assert newChildren.length == 2;
    
    if(identical(newChildren, left, right))
      return this;
    
    return new NumericCMP(newChildren[0], newChildren[1]);
  }

  @Override
  public Expression<?>[] getChildren() {
    return new Expression[]{left, right};
  }

  @Override
  public Type<Integer> getType() {
    return BuiltinTypes.SINT32;
  }

  @Override
  public void printMalformedExpression(Appendable a, int flags) throws IOException {
    // Do nothing
  }
}
