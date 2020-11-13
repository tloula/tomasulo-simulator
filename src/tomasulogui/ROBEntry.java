package tomasulogui;

import tomasulogui.IssuedInst.INST_TYPE;

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
  int target;

  // Fields for store
  int offset = -1;
  int storeData = -1;
  int storeAddress = -1;
  int storeAddressTag = -1;
  int storeDataTag = -1;

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

  public int getTarget() {
    return target;
  }

  public boolean isHaltOpcode() {
    return (opcode == IssuedInst.INST_TYPE.HALT);
  }

  public void setBranchTaken(boolean taken) {
    this.mispredicted = this.predictTaken ^ taken;
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

  public void setOffset(int os){
    offset = os;
  }

  public void setStoreData(int sd){
    storeData = sd;
  }
  public int getStoreData(){
    return storeData;
  }

  public void setStoreAddress(int sa){
    storeAddress = sa;
  }

  public void setStoreDataTag(int sdt){
    storeDataTag = sdt;
  }

  public void setStoreAddressTag(int sad){
    storeAddressTag = sad;
  }

  public int getOffset() {
    return offset;
  }

  public int getStoreDataTag(){
    return storeDataTag;
  }

  public int getStoreAddress() {
    return storeAddress;
  }

  public int getStoreAddressTag(){
    return storeAddressTag;
  }

  public boolean isBranch() {
    return (
      this.opcode == IssuedInst.INST_TYPE.BEQ ||
      this.opcode == IssuedInst.INST_TYPE.BGEZ ||
      this.opcode == IssuedInst.INST_TYPE.BGTZ ||
      this.opcode == IssuedInst.INST_TYPE.BLEZ ||
      this.opcode == IssuedInst.INST_TYPE.BLTZ ||
      this.opcode == IssuedInst.INST_TYPE.BNE ||
      this.opcode == IssuedInst.INST_TYPE.J ||
      this.opcode == IssuedInst.INST_TYPE.JR ||
      this.opcode == IssuedInst.INST_TYPE.JAL ||
      this.opcode == IssuedInst.INST_TYPE.JALR
    );
  }

  public void copyInstData(IssuedInst inst, int rearQ) {

    // TODO - This is a long and complicated method, probably the most complex
    // of the project.  It does 2 things:
    // 1. update the instruction
    // 2. update the fields of the ROBEntry
    int frontQ = rob.getFrontQ();

    inst.setRegDestTag(rearQ);

    if (inst.getRegSrc1Used()) {
      // Loop through ROB
      int i = rearQ;
      if(rearQ != frontQ){
        do {
          i = (i == 0) ? rob.getSize() - 1 : --i;

          if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegSrc1() && 
              this.rob.getEntryByTag(i).getOpcode() != INST_TYPE.STORE) {
            if(this.rob.getEntryByTag(i).isComplete()) {
              if(inst.getOpcode() == INST_TYPE.STORE){
                this.setStoreAddress(this.rob.getEntryByTag(i).getWriteValue());
              }
              else {
                inst.setRegSrc1Value(this.rob.getEntryByTag(i).getWriteValue());
                inst.setRegSrc1Valid();
              }
            }
            if (inst.getOpcode() == INST_TYPE.STORE) {
              this.setStoreAddressTag(i);
            }
            inst.setRegSrc1Tag(i);
            break;
          }
        } while (i != frontQ);
      }

      if (inst.getOpcode() == INST_TYPE.STORE && this.getStoreAddress() == -1 && this.getStoreAddressTag() == -1) {
        this.setStoreAddress(rob.getRegs().getReg(inst.getRegSrc1()));
      }
      else if (inst.getRegSrc1Tag() == -1 && !inst.getRegSrc1Valid()){
          inst.setRegSrc1Value(rob.getRegs().getReg(inst.getRegSrc1()));
          inst.setRegSrc1Valid();
      }
    } else {
      inst.setRegSrc1Valid();
    }

    if (inst.getRegSrc2Used()) {
      // Loop through ROB
      int i = rearQ;
      if (rearQ != frontQ) {
        do {
          i = (i == 0) ? rob.getSize() - 1 : --i;

          if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegSrc2()) {
            if(this.rob.getEntryByTag(i).isComplete()) {
              inst.setRegSrc2Value(this.rob.getEntryByTag(i).getWriteValue());
              inst.setRegSrc2Valid();
            }
            if (inst.getOpcode() == INST_TYPE.STORE) {
              this.setStoreDataTag(i);
            }
            inst.setRegSrc2Tag(i);
            break;
          }
        } while (i != frontQ);
      }
      if (inst.getRegSrc2Tag() == -1 && !inst.getRegSrc2Valid()) {
        inst.setRegSrc2Value(rob.getRegs().getReg(inst.getRegSrc2()));
        inst.setRegSrc2Valid();
      }
    } else {
      inst.setRegSrc2Valid();
    }

    // Handle stores separately
    if (inst.getOpcode() == INST_TYPE.STORE) {

      int i = rearQ;
      if (rearQ != frontQ) {
        do {
          i = (i == 0) ? rob.getSize() - 1 : --i;

          if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegDest() && 
              this.rob.getEntryByTag(i).getOpcode() != INST_TYPE.STORE) {
            if(this.rob.getEntryByTag(i).isComplete()) 
              this.setStoreData(this.rob.getEntryByTag(i).getWriteValue());
            else this.setStoreDataTag(i);
            break;
          }
        } while (i != frontQ);
      }
      if (this.getStoreData() == -1 && this.getStoreDataTag() == -1) {
        if(inst.getRegDest() == 31){
          this.setStoreDataTag(31); // If JAL is followed by STORE R31, breaks, so we tell it to grab R31 on retire          
        }
        else{
          this.setStoreData(rob.getRegs().getReg(inst.getRegDest()));
        }
      }

      this.setOffset(inst.getImmediate());
    }

    // Fill in branch prediction
    this.rob.getSimulator().getBTB().predictBranch(inst);
    this.target = inst.getBranchTgt();
    this.predictTaken = inst.getBranchPrediction();

    // Update ROB entry fields. 
    this.writeReg = inst.getRegDest();
    this.instPC = inst.getPC();
    this.opcode = inst.getOpcode();
  }
}
