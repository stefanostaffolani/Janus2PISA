package janus2pisa.codegen.exceptions;

public class CodeGenerationException extends RuntimeException {
  public CodeGenerationException() {
    super("Code Generation Error");
  }

  public CodeGenerationException(String message) {
    super(message);
  }
}
