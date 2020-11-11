package tomasulogui;

public abstract class FunctionalUnit {
  PipelineSimulator simulator;
  ReservationStation[] stations = new ReservationStation[2];
  public enum state { INACTIVE, EXECUTING, RAISING_HAND, CDB }
  state activity = state.INACTIVE;
  int currentSlot = 0;
  int timeLeft = -1;
  int result = -1;

  public FunctionalUnit(PipelineSimulator sim) {
    simulator = sim;
  }
 
  public void squash() {
    stations[0] = null;
    stations[1] = null;
    activity = state.INACTIVE;
    timeLeft = -1;
  }
  public void squashAll() {
    // Grab all units
    this.simulator.getALU().squash();
    this.simulator.getBranchUnit().squash();
    this.simulator.getDivider().squash();
    this.simulator.getMult().squash();
    this.simulator.getLoader().squashAll();
    this.simulator.getCDB().squashAll();
  }

  public abstract int calculateResult(int station);

  public abstract int getExecCycles();

  public state getActivity() {
    return this.activity;
  }

  public int getResultTag() {
    return this.stations[currentSlot].getDestTag();
  }

  public int getResult() {
    return this.result;
  }

  public void setStateCDB() {
    this.activity = state.CDB;
  }

  public boolean isFull(){
    if(stations[0] != null && stations[1] != null){
      return true; 
    }
    else return false;
  }

  public void execCycle(CDB cdb) {
    //todo - start executing, ask for CDB, etc.

    if (stations[0] != null) stations[0].snoop(cdb);
    if (stations[1] != null) stations[1].snoop(cdb);

    // if an instruction got put on the cdb, set the res station to null
    if (activity == state.CDB){
      activity = state.INACTIVE;
      stations[currentSlot] = null;
      currentSlot = (currentSlot + 1) % 2;
    }

    int otherSlot = (currentSlot + 1) % 2;
    if (activity == state.INACTIVE) { 
      if (this.stations[currentSlot] != null &&
          this.stations[currentSlot].getData1Valid() &&
          this.stations[currentSlot].getData2Valid())
      {
        activity = state.EXECUTING;
        timeLeft = this.getExecCycles();
      }
      else if (this.stations[otherSlot] != null && 
          this.stations[otherSlot].getData1Valid() && 
          this.stations[otherSlot].getData2Valid())
      {
        currentSlot = otherSlot;
        activity = state.EXECUTING;
        timeLeft = this.getExecCycles();
      }
    }

    if (activity == state.EXECUTING)
      timeLeft--; // This makes sense

    // if no one is executing, and if there are instructions to execute, pick one

    // Execute an instruction if you can
    if (timeLeft == 0) {
      timeLeft = -1;
      this.result = this.calculateResult(currentSlot);
      this.activity = this instanceof BranchUnit ?  state.INACTIVE : state.RAISING_HAND;
    }
  }

  public void acceptIssue(IssuedInst inst) {
    // todo - fill in reservation station (if available) with data from inst

    int slot = (stations[currentSlot] == null) ? currentSlot : (currentSlot + 1) % 2;

    this.stations[slot] = new ReservationStation(simulator);

    this.stations[slot].tag1 = inst.getRegSrc1Tag();
    this.stations[slot].tag2 = inst.getRegSrc2Tag();
    this.stations[slot].data1 = inst.getRegSrc1Value();
    this.stations[slot].data1Valid = inst.getRegSrc1Valid();
    this.stations[slot].destTag = inst.getRegDestTag();
    this.stations[slot].function = inst.getOpcode();

    switch (inst.getOpcode()) {
      case ADDI:
      case ANDI:
      case ORI:
      case XORI:
      case SLL:
      case SRA:
      case SRL:
      case STORE:
        this.stations[slot].data2 = inst.getImmediate();
        this.stations[slot].data2Valid = true;
        break;
      default:
        this.stations[slot].data2 = inst.getRegSrc2Value();
        this.stations[slot].data2Valid = inst.getRegSrc2Valid();
        break;
    }

    // Branch stuff
    this.stations[slot].address = inst.getBranchTgt();
    this.stations[slot].predictedTaken = inst.getBranchPrediction();
    // Set later??
    // int addressTag;
    // boolean addressValid = false; 
  }
}
