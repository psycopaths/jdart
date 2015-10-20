package <package>;

public class <class> {

  <interfaceMethods, constructors:{m, c |
  public static final class Exec_<m.id> {
    // use for default values
    <m.params:{p |
    private static <p.type> <p.name>;
    }>
    public static void main(String[] args) {
	  <if(c)>
      new <m.className>(
    <else>
      <m.className> testObject = new <m.className>(); // has to have default constructor
      testObject.<m.methodName>(
    <endif> 
        <trunc(m.params):{p | <p.name>,
        }>
        <if(m.params)><last(m.params).name><endif>
      );
    \}
  \}
  }>
}
