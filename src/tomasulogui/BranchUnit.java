package tomasulogui;

public class BranchUnit
        extends FunctionalUnit {

    public static final int EXEC_CYCLES = 1;

    public BranchUnit(PipelineSimulator sim) {
        super(sim);
    }

    public int calculateResult(int station) {
        // Grab Parameters
        boolean taken = false;
        int param1 = this.stations[station].getData1();
        int param2 = this.stations[station].getData2();

        // Calculate Result
        switch (this.stations[station].getFunction()) {
            case BEQ:
                taken = param1 == param2;
                break;
            case BNE: 
                taken = param1 != param2;
                break; 
            case BGEZ:
                taken = param1 >= 0;
                break;
            case BGTZ:
                taken = param1 > 0;
                break;
            case BLEZ:
                taken = param1 <= 0;
                break;
            case BLTZ:
                taken = param1 < 0;
                break;
            case JR:
            case JALR:
                this.simulator.getROB().getEntryByTag(this.stations[station].getDestTag()).setWriteValue(param1);
            case J:
            case JAL:
                taken = true;
                break;
            default:
                System.out.println("Branch Unit Passed Invalid Branch Type: " + this.stations[station].getFunction());
                break;
        }

        // Updating the ROB to reflect result
        this.simulator.getROB().getEntryByTag(this.stations[station].getDestTag()).setBranchTaken(taken);
        this.simulator.getROB().getEntryByTag(this.stations[station].getDestTag()).setComplete();
        this.stations[station] = null;
        return 0;
    }

    public int getExecCycles() {
        return EXEC_CYCLES;
    }
}
