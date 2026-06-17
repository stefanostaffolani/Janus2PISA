package janus2pisa.codegen;

public class Register {
  private final String name;
  private int value;

  public Register(String name, int value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return this.name;
  }

  public int getValue() {
    return this.value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Register " + this.name + ", value : " + this.value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Register)) return false;
    Register r = (Register) o;
    return name.equals(r.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
