package janus2pisa.codegen.exceptions;

public class InterpreterException extends RuntimeException {
  public InterpreterException() {
    super("Unable to interpret");
  }

  public InterpreterException(String message) {
    super(message);
  }
}
