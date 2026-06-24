package janus2pisa.codegen.exceptions;

public class InverterException extends RuntimeException {
  public InverterException() {
    super("cannot invert");
  }

  public InverterException(String message) {
    super(message);
  }
}
