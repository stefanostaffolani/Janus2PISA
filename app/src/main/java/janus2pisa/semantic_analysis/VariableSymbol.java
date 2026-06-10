package janus2pisa.semantic_analysis;

public class VariableSymbol extends Symbol {
  int offset;

  public VariableSymbol(String name, Scope scope, int offset) {
    super(name, scope);
    this.offset = offset;
  }

  @Override
  public String toString() {
    return "Var: " + super.toString() + ", offset: " + offset;
  }

  public int getOffset() {
    return offset;
  }
}
