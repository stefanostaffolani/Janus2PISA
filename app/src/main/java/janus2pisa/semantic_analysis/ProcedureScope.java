package janus2pisa.semantic_analysis;

public class ProcedureScope extends Scope {
  public ProcedureScope(Scope enclosingScope) {
    super(enclosingScope);
  }

  @Override
  public String toString() {
    return "F:" + symbols.toString();
  }
}
