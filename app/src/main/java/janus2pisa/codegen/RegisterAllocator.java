package janus2pisa.codegen;

import janus2pisa.codegen.exceptions.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
    init free Registers we reserve 3 reserved register and the others for general purpose :
    - r0 : constant 0
    - r1 : stack pointer
    - r2 : return offset
*/
public class RegisterAllocator {
  private int numberOfRegisters;
  private Set<Register> freeRegisters;
  private Set<Register> committedRegisters;
  private Set<Register> garbageRegisters;

  public RegisterAllocator(int numberOfRegisters) {
    if (numberOfRegisters < 16) {
      throw new IllegalArgumentException("Number of registers must be at least 16");
    }
    this.numberOfRegisters = numberOfRegisters;
    committedRegisters = new HashSet<Register>();
    garbageRegisters = new HashSet<Register>();

    this.freeRegisters =
        java.util.stream.IntStream.range(3, this.numberOfRegisters)
            .mapToObj(i -> new Register("R" + i, 0))
            .collect(java.util.stream.Collectors.<Register>toSet());
  }

  public RegisterAllocator() {
    this(32);
  }

  public Register getFreeRegister() {
    if (freeRegisters.isEmpty()) {
      throw new RegisterAllocatorException();
    } else {
      Iterator<Register> it = freeRegisters.iterator();
      Register reg = it.next();
      it.remove();
      return reg;
    }
  }

  public void commitRegister(Register reg) {
    this.committedRegisters.add(reg);
  }

  public void toGarbage(Register reg) {
    if (committedRegisters.remove(reg)) {
      garbageRegisters.add(reg);
    } else {
      throw new RegisterAllocatorException("Register " + reg + " is not committed");
    }
  }

  public void freeRegister(Register reg) {
    if (committedRegisters.remove(reg) && garbageRegisters.remove(reg)) {
      freeRegisters.add(reg);
    } else {
      throw new RegisterAllocatorException("Register " + reg + " is not committed or garbage");
    }
  }

  public boolean isFree(Register reg) {
    return this.freeRegisters.contains(reg);
  }

  public boolean isCommitted(Register reg) {
    return this.committedRegisters.contains(reg);
  }

  public boolean isGarbage(Register reg) {
    return this.garbageRegisters.contains(reg);
  }
}
