package janus2pisa.semantic_analysis;

public class BlockScope extends Scope {

  public BlockScope(Scope enclosingScope) {
    super(enclosingScope);
  }

  @Override
  public String toString() {
    return "B:" + symbols.toString();
  }
}
