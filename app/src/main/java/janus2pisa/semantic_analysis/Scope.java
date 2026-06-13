package janus2pisa.semantic_analysis;

import java.util.HashMap;
import java.util.Map;

public abstract class Scope {

  protected final Map<String, Symbol> symbols = new HashMap<>();
  protected final Scope enclosingScope;

  public Scope(Scope enclosingScope) {
    this.enclosingScope = enclosingScope;
  }

  public void define(Symbol sym) {
    symbols.put(sym.getName(), sym);
  }

  public Symbol resolve(String name) {
    Symbol s = symbols.get(name);
    if (s != null) {
      return s;
    }
    if (enclosingScope != null) {
      return enclosingScope.resolve(name);
    }
    return null;
  }

  public Symbol rec_resolve_until_function_scope(String name) {
    Symbol s = symbols.get(name);
    if (s != null) {
      return s;
    }
    // if not here, check any enclosing scope
    if (!(this instanceof ProcedureScope) && enclosingScope != null) {
      return enclosingScope.rec_resolve_until_function_scope(name);
    }
    return null; // not found
  }

  public Symbol resolve_recursively(String name) {
    Symbol s = symbols.get(name);
    if (s != null) {
      return s;
    }
    // if not here, check any enclosing scope
    if (enclosingScope != null) {
      return enclosingScope.resolve_recursively(name);
    }
    return null; // not found
  }

  public Scope getEnclosingScope() {
    return enclosingScope;
  }

  public int getNestingLevel() {
    if (enclosingScope == null) {
      return 0;
    } else if (this instanceof ProcedureScope) {
      return 1 + enclosingScope.getNestingLevel();
    } else return enclosingScope.getNestingLevel();
  }

  @Override
  public String toString() {
    return symbols.toString();
  }
}
