package janus2pisa.semantic_analysis;

public class ProcedureSymbol extends Symbol {
  private int paramLength;
  private String label;

  public ProcedureSymbol(String name, Scope scope, int paramLength, String label) {
    super(name, scope);
    this.paramLength = paramLength;
    this.label = label;
  }

  public String getLabel() {
    return this.label;
  }

  public int getParamLength() {
    return this.paramLength;
  }

  @Override
  public String toString() {
    return "Func:" + super.toString() + ":" + paramLength;
  }
}
