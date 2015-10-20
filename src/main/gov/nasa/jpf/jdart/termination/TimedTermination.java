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
package gov.nasa.jpf.jdart.termination;


/**
 * Executes for a set time limit.<br><br>
 * 
 * Because of the way jFuzz checks when to stop, if the time limit is
 * reached during and execution the current task will finish before this
 * causes the execution to terminate.
 */
public class TimedTermination extends TerminationStrategy {

  private final long startTimeMillis;
  private final long runTimeMillis;

  public TimedTermination(int hours) {
    this (hours, 0);
  }

  public TimedTermination(int hours, int minutes) {
    this (hours, minutes, 0);
  }

  public TimedTermination(int hours, int minutes, int seconds) {
    this (hours, minutes, seconds, 0);
  }

  public TimedTermination(int hours, int minutes, int seconds, int millis) {
    runTimeMillis = (hours * 3600000l) + (minutes * 60000l) + (seconds * 1000l) + millis;
    startTimeMillis = System.currentTimeMillis();
  }

  @Override
  public boolean isDone() {
    return (startTimeMillis + runTimeMillis) < System.currentTimeMillis();
  }

  @Override
  public String getReason() {
    if ((startTimeMillis + runTimeMillis) >= System.currentTimeMillis())
      return "Resolved all paths!";
    else
      return "Time limit Expired";
  }
}
