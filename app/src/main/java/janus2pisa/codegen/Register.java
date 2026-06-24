package janus2pisa.codegen;

public class Register implements Comparable<Register> {
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
  public int compareTo(Register other) {
    String numThis = this.name.replaceAll("\\D+", "");
    String numOther = other.name.replaceAll("\\D+", "");
    int idThis = numThis.isEmpty() ? 0 : Integer.parseInt(numThis);
    int idOther = numOther.isEmpty() ? 0 : Integer.parseInt(numOther);
    return Integer.compare(idThis, idOther);
  }

  @Override
  public String toString() {
    return this.name;
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
