package janus2pisa.semantic_analysis;

public class Symbol {

  private String name;
  private Scope scope;

  public Symbol(String name, Scope scope) {
    this.name = new String(name);
    this.scope = scope;
  }

  public String getName() {
    return this.name;
  }

  public String toString() {
    return this.getName();
  }
}
