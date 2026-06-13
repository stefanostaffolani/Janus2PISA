package janus2pisa.semantic_analysis;

public class ArraySymbol extends VariableSymbol {
  int length;

  public ArraySymbol(String name, Scope scope, int offset, int length) {
    super(name, scope, offset);
    this.length = length;
  }

  @Override
  public String toString() {
    return "Array Var: " + super.toString() + ", len :" + this.length;
  }

  public int getLength() {
    return length;
  }
}
