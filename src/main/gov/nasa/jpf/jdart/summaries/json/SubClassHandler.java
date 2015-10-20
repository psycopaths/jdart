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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 *
 */
public class SubClassHandler<T> implements
        JsonSerializer<T>, JsonDeserializer<T> {

  private static final String SUB_CLASS = "__SUB_CLASS";

  @Override
  public JsonElement serialize(T t, Type type, JsonSerializationContext jsc) {
    JsonElement je = jsc.serialize(t, t.getClass());
    je.getAsJsonObject().addProperty(SUB_CLASS, t.getClass().getName());
    return je;
  }

  @Override
  public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) 
          throws JsonParseException {
    JsonObject jo = je.getAsJsonObject();
    String className = jo.get(SUB_CLASS).getAsString();
    try {
      Class<?> clazz = Class.forName(className);
      return jdc.deserialize(je, clazz);
    } catch (ClassNotFoundException e) {
      throw new JsonParseException(e);
    }
  }

}
