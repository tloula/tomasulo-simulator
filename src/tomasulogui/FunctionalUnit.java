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
    int otherSlot = (currentSlot + 1) % 2;
    if (stations[0] != null) stations[0].snoop(cdb);
    if (stations[1] != null) stations[1].snoop(cdb);

    // Handle if an instruction is executing
    if (activity == state.EXECUTING)
      timeLeft--;

    // if an instruction got put on the cdb, set the reservation station to null
    if (activity == state.CDB) {
      activity = state.INACTIVE;
      stations[currentSlot] = null;
      currentSlot = (currentSlot + 1) % 2;
    }

    // If an instruction is not executing, try to start executing one (has both params?)
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
    
    // If it's finished, raise hand
    if (timeLeft == 0) {
      timeLeft = -1;
      this.result = this.calculateResult(currentSlot);
      this.activity = this instanceof BranchUnit ?  state.INACTIVE : state.RAISING_HAND;
    }
  }

  public void acceptIssue(IssuedInst inst) {
    // Figure out which slot is accepting the new instruction and set it's values 
    int slot = (stations[currentSlot] == null) ? currentSlot : (currentSlot + 1) % 2;
    this.stations[slot] = new ReservationStation(simulator);
    this.stations[slot].loadInst(inst);
  }
}
