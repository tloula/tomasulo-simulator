package tomasulogui;

import tomasulogui.IssuedInst.INST_TYPE;

public class ReorderBuffer {
  public static final int size = 30;
  int frontQ = 0;
  int rearQ = 0;

  ROBEntry[] buff = new ROBEntry[size];
  int numRetirees = 0;

  PipelineSimulator simulator;
  RegisterFile regs;
  boolean halted = false;

  public int getFrontQ(){
    return this.frontQ;
  }

  public int getRearQ(){
    return this.rearQ;
  }

  public int getSize() {
    return size;
  }

  public PipelineSimulator getSimulator(){
    return simulator;
  }

  public ReorderBuffer(PipelineSimulator sim, RegisterFile registers) {
    simulator = sim;
    regs = registers;
  }

  public ROBEntry getEntryByTag(int tag) {
    return buff[tag];
  }

  public int getInstPC(int tag) {
    return buff[tag].getInstPC();
  }

  public boolean isHalted() {
    return halted;
  }

  public boolean isFull() {
    return (frontQ == rearQ && buff[frontQ] != null);
  }

  public int getNumRetirees() {
    return numRetirees;
  }

  public RegisterFile getRegs(){
    return regs;
  }

  public boolean retireInst() {
    // 3 cases
    // 1. regular reg dest inst
    // 2. isBranch w/ mispredict
    // 3. isStore
    ROBEntry retiree = buff[frontQ];

    if (retiree == null) {
      return false;
    }

    if (retiree.isHaltOpcode()) {
      halted = true;
      return true;
    }
    
    if (retiree.getOpcode() == INST_TYPE.STORE ||
        retiree.getOpcode() == INST_TYPE.NOP)
      retiree.setComplete();

    boolean shouldAdvance = true;
    boolean squashed = false;

    if (!retiree.isComplete()) {
      shouldAdvance = false; 
    }
    else {
      // If is branch
      if(retiree.isBranch()){
        if(retiree.branchMispredicted()){
          // Handle mispredicted branch
          if (  retiree.getOpcode() == INST_TYPE.JR || // JR type always mispredict for convenience
                retiree.getOpcode() ==INST_TYPE.JALR) {
            this.simulator.setPC(retiree.getWriteValue() + 4);
          } else {
            int newPC = retiree.getPredictTaken() ? retiree.getInstPC() + 4 : retiree.getTarget();
            this.simulator.setPC(newPC);
          }
          this.simulator.getALU().squashAll();
          squashed = true;
        }
        if (  retiree.getOpcode() == INST_TYPE.JAL ||
              retiree.getOpcode() ==INST_TYPE.JALR){
          regs.setReg(31, retiree.getInstPC());
        }
        this.simulator.getBTB().setBranchResult(retiree.getInstPC(), retiree.getPredictTaken() ^ retiree.branchMispredicted());
      }
      // else if is store
      if (retiree.getOpcode() == IssuedInst.INST_TYPE.STORE) {
        if(retiree.getStoreDataTag() == 31)
          retiree.setStoreData(simulator.getROB().getDataForReg(31));
        simulator.getMemory().setIntDataAtAddr(retiree.getStoreAddress() + retiree.getOffset(), retiree.getStoreData());
      }
      // else if is alu/mul/div/nop/load
      else {
        if(retiree.getWriteReg() != -1) regs.setReg(retiree.getWriteReg(), retiree.getWriteValue());
      }

      // if mispredict branch, won't do normal advance
      if (shouldAdvance) {
        numRetirees++;
        buff[frontQ] = null;
        frontQ = (frontQ + 1) % size;
      }

      if (squashed){
        frontQ = 0;
        rearQ = 0;
  
        // Null out everything else so it looks nicer
        for(int i = 0; i < 30; i++){
          buff[i] = null;
        }
        return false; // Might not need this eventually, for now, it avoids the readCDB line
      }
    }

    // Can we move this?
    readCDB(simulator.getCDB());

    return false;
  }

  public void readCDB(CDB cdb) {
    // check entire CDB for someone waiting on this data
    // could be destination reg
    // could be store address source

    if (cdb.getDataValid()) {
      int i = rearQ;
      do {
        i = (i == 0) ? size - 1 : --i;

        if (buff[i].getOpcode() == INST_TYPE.STORE) {
          if (buff[i].getStoreDataTag() == cdb.getDataTag())
            buff[i].setStoreData(cdb.getDataValue());
          if (buff[i].getStoreAddressTag() == cdb.getDataTag())
            buff[i].setStoreAddress(cdb.getDataValue());
        }
      } while (i != frontQ);

      simulator.getROB().getEntryByTag(cdb.getDataTag()).setWriteValue(cdb.getDataValue());
      simulator.getROB().getEntryByTag(cdb.getDataTag()).setComplete();
    }
  }

  public void updateInstForIssue(IssuedInst inst) {
    // the task is to simply annotate the register fields
    // the dest reg will be assigned a tag, which is just our slot#
    // all src regs will either be assigned a tag, read from reg, or forwarded from ROB

    // TODO - possibly nothing if you use my model
    // I use the call to copyInstData below to do 2 things:
    // 1. update the Issued Inst
    // 2. fill in the ROB entry

    // first get a ROB slot
    if (buff[rearQ] != null) {
      throw new MIPSException("updateInstForIssue: no ROB slot avail");
    }
    ROBEntry newEntry = new ROBEntry(this);
    buff[rearQ] = newEntry;
    newEntry.copyInstData(inst, rearQ);

    rearQ = (rearQ + 1) % size;
  }

  public int getTagForReg(int regNum) {
    return (regs.getSlotForReg(regNum));
  }

  public int getDataForReg(int regNum) {
    return (regs.getReg(regNum));
  }

  public void setTagForReg(int regNum, int tag) {
    regs.setSlotForReg(regNum, tag);
  }
}
