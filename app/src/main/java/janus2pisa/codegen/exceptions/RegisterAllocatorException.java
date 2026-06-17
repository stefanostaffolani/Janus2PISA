package janus2pisa.codegen.exceptions;

public class RegisterAllocatorException extends RuntimeException {
  public RegisterAllocatorException() {
    super("No registers available");
  }

  public RegisterAllocatorException(String message) {
    super(message);
  }
}
