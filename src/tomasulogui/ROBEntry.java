package tomasulogui;

import tomasulogui.IssuedInst.INST_TYPE;

public class ROBEntry {
  ReorderBuffer rob;

  boolean complete = false;
  boolean branch = false;
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

  public boolean isBranch() {
    return branch;
  }

  public void setBranchTaken(boolean taken) {
    this.mispredicted = this.predictTaken ^ taken;
  }

  public boolean isComplete() {
    return complete;
  }
  public void setComplete() {
    this.complete = true;
  }

  public int getWriteReg() {
    return writeReg;
  }
  public void setWriteReg(int reg) {
    this.writeReg = reg;
  }

  public int getWriteValue() {
    return writeValue;
  }
  public void setWriteValue(int value) {
    writeValue = value;
  }

  public int getOffset() {
    return offset;
  }
  public void setOffset(int os) {
    offset = os;
  }

  public int getStoreData() {
    return storeData;
  }
  public void setStoreData(int sd) {
    storeData = sd;
  }

  public int getStoreAddress() {
    return storeAddress;
  }
  public void setStoreAddress(int sa) {
    storeAddress = sa;
  }

  public int getStoreAddressTag() {
    return storeAddressTag;
  }
  public void setStoreAddressTag(int sad) {
    storeAddressTag = sad;
  }

  public int getStoreDataTag() {
    return storeDataTag;
  }
  public void setStoreDataTag(int sdt) {
    storeDataTag = sdt;
  }

  public void copyInstData(IssuedInst inst, int rearQ) {
    int frontQ = rob.getFrontQ();

    inst.setRegDestTag(rearQ);

    // If using source 1 reg, check ROB to see if it should be forwarded
    if (inst.getRegSrc1Used()) {
      int i = rearQ;
      if(rearQ != frontQ){

        // Loop through ROB
        do {
          i = (i == 0) ? rob.getSize() - 1 : --i;

          // It it's found in the ROB
          if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegSrc1() && 
              this.rob.getEntryByTag(i).getOpcode() != INST_TYPE.STORE) {
            
            // Grab the value if complete
            if (this.rob.getEntryByTag(i).isComplete()) {
              if (inst.getOpcode() == INST_TYPE.STORE) { // Stores are a pain
                this.setStoreAddress(this.rob.getEntryByTag(i).getWriteValue());
              }
              else {
                inst.setRegSrc1Value(this.rob.getEntryByTag(i).getWriteValue());
                inst.setRegSrc1Valid();
              }
            }
            // Otherwise grab the tag
            if (inst.getOpcode() == INST_TYPE.STORE) { // Stores are a pain
              this.setStoreAddressTag(i);
            }
            inst.setRegSrc1Tag(i);
            break;
          }
        } while (i != frontQ);
      }

      // Data was not found in ROB, grab data from register file
      if (inst.getOpcode() == INST_TYPE.STORE && this.getStoreAddress() == -1 && this.getStoreAddressTag() == -1) {
        this.setStoreAddress(rob.getRegs().getReg(inst.getRegSrc1()));
      }
      else if (inst.getRegSrc1Tag() == -1 && !inst.getRegSrc1Valid()) {
          inst.setRegSrc1Value(rob.getRegs().getReg(inst.getRegSrc1()));
          inst.setRegSrc1Valid();
      }
    } else {
      inst.setRegSrc1Valid();
    }

    // If using source 2 reg, check ROB to see if it should be forwarded
    if (inst.getRegSrc2Used()) {

      int i = rearQ;
      if (rearQ != frontQ) {

        // Loop through ROB
        do {
          i = (i == 0) ? rob.getSize() - 1 : --i;

          // If it's found in the rob
          if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegSrc2()) {

            // Grab the value if complete
            if (this.rob.getEntryByTag(i).isComplete()) {
              inst.setRegSrc2Value(this.rob.getEntryByTag(i).getWriteValue());
              inst.setRegSrc2Valid();
            }
            if (inst.getOpcode() == INST_TYPE.STORE) { // I hate stores
              this.setStoreDataTag(i);
            }
            // Otherwise grab the tag
            inst.setRegSrc2Tag(i);
            break;
          }
        } while (i != frontQ);
      }

      // Data was not found in ROB, grab data from register file
      if (inst.getRegSrc2Tag() == -1 && !inst.getRegSrc2Valid()) {
        inst.setRegSrc2Value(rob.getRegs().getReg(inst.getRegSrc2()));
        inst.setRegSrc2Valid();
      }
    } else {
      inst.setRegSrc2Valid();
    }

    // Separately handle the data value that store is saving 
    if (inst.getOpcode() == INST_TYPE.STORE) {

      int i = rearQ;
      if (rearQ != frontQ) {

        // Loop through ROB
        do {
          i = (i == 0) ? rob.getSize() - 1 : --i;

          // If register is found, and complete, grab value, else grab tag
          if (this.rob.getEntryByTag(i).getWriteReg() == inst.getRegDest() && 
              this.rob.getEntryByTag(i).getOpcode() != INST_TYPE.STORE) {
            if (this.rob.getEntryByTag(i).isComplete())
              this.setStoreData(this.rob.getEntryByTag(i).getWriteValue());
            else
              this.setStoreDataTag(i);
            break;
          }
        } while (i != frontQ);
      }

      // Data was not found in ROB, grab from register file
      if (this.getStoreData() == -1 && this.getStoreDataTag() == -1) {
        // If JAL is followed by STORE R31, it breaks, so we tell it to grab R31 on retire  
        if (inst.getRegDest() == 31) this.setStoreDataTag(31);        
        else this.setStoreData(rob.getRegs().getReg(inst.getRegDest()));
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
    this.branch = inst.isBranch();
  }
}
