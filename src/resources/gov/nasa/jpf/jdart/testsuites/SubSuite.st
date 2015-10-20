package <package>;

import junit.framework.*;

public class <class> extends TestCase {

<tests:{t | 

  public void test<i0>() throws Throwable {

  <t.methods:{m | 
    // step <i0>    
    if (!(<m.precondition>)) {
      fail("Precondition not met: <m.precondition>");
    \}
    <if(m.check.instanceMeth)>
    <m.check.className> obj = new <m.check.className>();
    <endif>
    
    <if(m.checks.exception)>
    try {
      <if(m.check.instanceMeth)>obj.<endif><m.call>;
      fail("Expected exception of type <m.check.exceptionClass>");
    \} catch (<m.check.exceptionClass> e) {
    \}
    <else>
      <if(m.check.instanceMeth)>obj.<endif><m.call>;
    <endif>
    // perform checks 
    <if(m.check.checks)>
      <m.check.checkCalls:{a | 
      <a>;
      }>
    <endif>
  }>
  \} // end of test case <i0>

}>

\}