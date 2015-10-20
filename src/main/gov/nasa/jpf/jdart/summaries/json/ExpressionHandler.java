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
package gov.nasa.jpf.jdart.summaries.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.parser.ParserUtil;
import gov.nasa.jpf.constraints.util.ExpressionUtil;
import gov.nasa.jpf.constraints.util.TypeUtil;
import java.lang.reflect.Type;
import org.antlr.runtime.RecognitionException;

/**
 *
 */
public class ExpressionHandler implements
        JsonSerializer<Expression<?>>, JsonDeserializer<Expression<?>> {

  @Override
  public JsonElement serialize(Expression t, Type type, JsonSerializationContext jsc) {
    if (t instanceof Variable) {
      Variable v = (Variable)t;
      return new JsonPrimitive("[V]" + v.toString(Variable.INCLUDE_VARIABLE_TYPE));
    }
    else if (!TypeUtil.isBoolSort(t)) {
      return new JsonPrimitive("[A]" + ExpressionUtil.toParseableString(t));
      
    }     
    return new JsonPrimitive("[L]" + ExpressionUtil.toParseableString(t));
  }

  @Override
  public Expression<?> deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
          throws JsonParseException {
    try {
      String s = je.getAsJsonPrimitive().getAsString();      
      if (s.startsWith("[V]")) {
        return ParserUtil.parseVariable(s.substring(3));        
      }
      else if (s.startsWith("[A]")) {
        return ParserUtil.parseArithmetic(s.substring(3));
      }      
      return ParserUtil.parseLogical(s.substring(3));
    } catch (RecognitionException ex) {
      throw new JsonParseException(ex);
    }
  }
}
