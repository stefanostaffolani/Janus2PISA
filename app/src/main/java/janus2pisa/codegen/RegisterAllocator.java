package janus2pisa.codegen;

import janus2pisa.codegen.exceptions.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
    init free Registers we reserve 3 reserved register and the others for general purpose :
    - r0 : constant 0
    - r1 : stack pointer
    - r2 : return offset
*/
public class RegisterAllocator {
  private int numberOfRegisters;
  private Deque<Register> freeRegisters;
  private Set<Register> committedRegisters;
  private Set<Register> garbageRegisters;

  public RegisterAllocator(int numberOfRegisters) {
    if (numberOfRegisters < 16) {
      throw new IllegalArgumentException("Number of registers must be at least 16");
    }
    this.numberOfRegisters = numberOfRegisters;
    committedRegisters = new LinkedHashSet<Register>();
    garbageRegisters = new LinkedHashSet<Register>();

    this.freeRegisters = new ArrayDeque<>();
    for (int i = this.numberOfRegisters - 1; i >= 0; i--) {
      freeRegisters.push(new Register("R" + i, 0));
    }
  }

  public RegisterAllocator() {
    this(32);
  }

  public Register getFreeRegister() {
    if (freeRegisters.isEmpty()) {
      throw new RegisterAllocatorException();
    }
    return freeRegisters.pop();
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
    committedRegisters.remove(reg);
    garbageRegisters.remove(reg);
    freeRegisters.push(reg);
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

  public List<Register> GetGarbageRegisters() {
    return this.garbageRegisters.stream().sorted().collect(Collectors.toList());
  }
}
