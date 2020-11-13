package tomasulogui;

public class ReservationStation {
  PipelineSimulator simulator;

  int tag1;
  int tag2;
  int data1;
  int data2;
  boolean data1Valid = false;
  boolean data2Valid = false;
  // destTag doubles as branch tag
  int destTag;
  IssuedInst.INST_TYPE function = IssuedInst.INST_TYPE.NOP;

  // following just for branches
  int addressTag;
  boolean addressValid = false;
  int address;
  boolean predictedTaken = false;

  public ReservationStation(PipelineSimulator sim) {
    simulator = sim;
  }

  public int getDestTag() {
    return destTag;
  }

  public int getData1() {
    return data1;
  }

  public int getData2() {
    return data2;
  }

  public boolean getData1Valid() {
    return data1Valid;
  }

  public boolean getData2Valid() {
    return data2Valid;
  }

  public boolean isPredictedTaken() {
    return predictedTaken;
  }

  public IssuedInst.INST_TYPE getFunction() {
    return function;
  }

  public void snoop(CDB cdb) {
    // If tag1 matches cdb data tag, grab the data
    if (tag1 == cdb.getDataTag() && cdb.getDataValid()) {
      data1 = cdb.getDataValue();
      data1Valid = true;
    }
    // If tag2 matches cdb data tag, grab the data
    if (tag2 == cdb.getDataTag() && cdb.getDataValid()) {
      data2 = cdb.getDataValue();
      data2Valid = true;
    }
  }

  public boolean isReady() {
    return data1Valid && data2Valid;
  }

  public void loadInst(IssuedInst inst) {
    
    // Set data values in reservation stations
    this.tag1 = inst.getRegSrc1Tag();
    this.tag2 = inst.getRegSrc2Tag();
    this.data1 = inst.getRegSrc1Value();
    this.data1Valid = inst.getRegSrc1Valid();
    this.destTag = inst.getRegDestTag();
    this.function = inst.getOpcode();

    // Instructions with an immediate are a little different
    switch (inst.getOpcode()) {
      case ADDI:
      case ANDI:
      case ORI:
      case XORI:
      case SLL:
      case SRA:
      case SRL:
      case STORE:
        this.data2 = inst.getImmediate();
        this.data2Valid = true;
        break;
      default:
        this.data2 = inst.getRegSrc2Value();
        this.data2Valid = inst.getRegSrc2Valid();
        break;
    }

    // Handle Branching
    this.address = inst.getBranchTgt();
    this.predictedTaken = inst.getBranchPrediction();
  }
}
