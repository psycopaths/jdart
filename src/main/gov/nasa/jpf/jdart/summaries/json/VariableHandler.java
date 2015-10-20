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
import java.lang.reflect.Type;
import org.antlr.runtime.RecognitionException;

/**
 *
 */
public class VariableHandler implements
        JsonSerializer<Variable<?>>, JsonDeserializer<Variable<?>> {

  @Override
  public JsonElement serialize(Variable<?> v, Type type, JsonSerializationContext jsc) {
    return new JsonPrimitive(v.toString(Variable.INCLUDE_VARIABLE_TYPE));
  }

  @Override
  public Variable<?> deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
          throws JsonParseException {
    try {
      Expression<Boolean> expr = ParserUtil.parseVariable(je.getAsJsonPrimitive().getAsString());
      return (Variable<?>) expr;
    } catch (RecognitionException ex) {
      throw new JsonParseException(ex);
    }
  }
}
