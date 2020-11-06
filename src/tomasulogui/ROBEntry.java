package tomasulogui;

public class ROBEntry {
  ReorderBuffer rob;

  // TODO - add many more fields into entry
  // I deleted most, and only kept those necessary to compile GUI
  boolean complete = false;
  boolean predictTaken = false;
  boolean mispredicted = false;
  int instPC = -1;
  int writeReg = -1;
  int writeValue = -1;

  IssuedInst.INST_TYPE opcode;

  public ROBEntry(ReorderBuffer buffer) {
    rob = buffer;
  }

  public boolean isComplete() {
    return complete;
  }

  public void setComplete() {
    this.complete = true;
  }

  public boolean branchMispredicted() {
    return mispredicted;
  }

  public boolean getPredictTaken() {
    return predictTaken;
  }

  public int getInstPC() {
    return instPC;
  }

  public IssuedInst.INST_TYPE getOpcode () {
    return opcode;
  }

  public boolean isHaltOpcode() {
    return (opcode == IssuedInst.INST_TYPE.HALT);
  }

  public void setBranchTaken(boolean result) {
  // TODO - maybe more than simple set
  }

  public int getWriteReg() {
    return writeReg;
  }

  public int getWriteValue() {
    return writeValue;
  }

  public void setWriteValue(int value) {
    writeValue = value;
  }

  public void copyInstData(IssuedInst inst, int rearQ) {

    // TODO - This is a long and complicated method, probably the most complex
    // of the project.  It does 2 things:
    // 1. update the instruction
    // 2. update the fields of the ROBEntry

    if (inst.getRegDestUsed()) 
      inst.setRegDestTag(rearQ);
    
    if (inst.getRegSrc1Used()) {
      // Loop through ROB
      int i = rearQ;
      while (i != rob.getFrontQ()) {
        if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegSrc1()) {
          if(this.rob.getEntryByTag(i).isComplete()){
            inst.setRegSrc1Value(this.rob.getEntryByTag(i).getWriteValue());
            inst.setRegSrc1Valid();
          } else {
            inst.setRegSrc1Tag(i);
          }
        }
        i = (i == 0) ? rob.getSize() - 1 : --i;
      }
      if (inst.getRegSrc1Tag() == -1 && !inst.getRegSrc1Valid()){
          inst.setRegSrc1Value(rob.getRegs().getReg(inst.getRegSrc1()));
          inst.setRegSrc1Valid();
      }
    }
    if (inst.getRegSrc2Used()) {
      // Loop through ROB
      int i = rearQ;
      while (i != rob.getFrontQ()) {
        if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegSrc2()) {
          if(this.rob.getEntryByTag(i).isComplete()){
            inst.setRegSrc2Value(this.rob.getEntryByTag(i).getWriteValue());
            inst.setRegSrc2Valid();
          } else {
            inst.setRegSrc2Tag(i);
          }
        }
        i = (i == 0) ? rob.getSize() - 1 : --i;
      }
      if (inst.getRegSrc2Tag() == -1 && !inst.getRegSrc2Valid()){
        inst.setRegSrc2Value(rob.getRegs().getReg(inst.getRegSrc2()));
        inst.setRegSrc2Valid();
      }
    }

    // Fill in branch prediction


    // Update ROB entry fields. 
    this.writeReg = inst.getRegDest();
    this.instPC = inst.getPC();
    this.opcode = inst.getOpcode();
  }
}
