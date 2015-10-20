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
package summaries.cev;

public class CEV {

  public CEV() {}
    
    Spacecraft spacecraft_V1 = new Spacecraft();
    
  public void reset(int component) {
  	spacecraft_V1.reset(component);
  }
  
  public void srbIgnition () {
  }
  
  public void failure (int tminus) {
    if (tminus <= 5) {
    	assert false: "PAD abort";
    } else {
    	assert false: "Hold launch";
    }
  }

  public void stage1Separation () {
  	spacecraft_V1.doStage1Separation();
  }

  public  void abort (int altitude, int controlMotorFired) {
  	if (!spacecraft_V1.isDoneStage1()) {
  		spacecraft_V1.doStage1Abort(altitude, controlMotorFired);
  		if (controlMotorFired == 1)
  			spacecraft_V1.doLowActiveAbort();
  		else
  			spacecraft_V1.doLowPassiveAbort();
  	} else if (!spacecraft_V1.isDoneStage2()) {
  		spacecraft_V1.doStage2Abort(altitude);
  		if (controlMotorFired == 0)
  			spacecraft_V1.doLowPassiveAbort();
  	}
  	assert false: "Mission aborted";
  }

  public  void stage2Separation () {
  	spacecraft_V1.doStage2Separation();
  }
  
  public  void tliBurn() {
  	spacecraft_V1.readyForTliBurn();
  }
  
  public  void enterOrbitOps(int earthSensorFailure) {
    if (earthSensorFailure == 1) {
      assert false: "Earth sensor failure. Cannot enter orbit ops";
    }
  }

  public  void deOrbit() {
  	if (spacecraft_V1.readyForDeorbit())
  		spacecraft_V1.internalReset();
  }
  
  public  void teiBurn() {
  	spacecraft_V1.readyForTeiBurn();
  }
  
  public  void lasJettison (int altitude) {
  	spacecraft_V1.doLASjettison(altitude);
  }
  
  public  void lsamRendezvous() {
    if (spacecraft_V1.readyForLSAMrendezvous())
    	spacecraft_V1.doLSAMrendezvous();
  }
  
  public  void loiBurn() {
  }

  public  void doEdsSeparation () {
  	spacecraft_V1.doEDSseparation();
  }
  
  public  void doSMSeparation () {
  	spacecraft_V1.doSMseparation();
  }

  public  void lsamAscentBurn () {
  	spacecraft_V1.doLSAMascentBurn();
  }
  
  public  void lsamAscentRendezvous () {
  	spacecraft_V1.doLSAMascentRendezvous();
  }
  
  public  void eiBurn (int hasCMimbalance, int hasRCSfailure) {
  	spacecraft_V1.doEiBurn(hasCMimbalance, hasRCSfailure);
  }
  
   
}
