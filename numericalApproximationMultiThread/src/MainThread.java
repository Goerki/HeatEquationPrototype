import Cells.Cell;
import Cells.CellArea;
import Cells.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainThread extends CalculationThread {
    int numberSteps;
    List<CalculationThread> threads;
    List<CellArea> areas;
    List<SystemOfEquations> equationSystemList;


    public MainThread(Space space, List<Coordinates> solidCells,List<Coordinates> fluidCells, int numberSteps, List<CellArea> fluidAreas){
        super(space, solidCells, fluidCells, numberSteps);
        this.numberSteps=numberSteps;
        this.threads = new ArrayList<>();
        this.areas = fluidAreas;
        this.createEquationSystems(space, this.areas);

    }


    private void createEquationSystems(Space space, List<CellArea> cellAreas){
        this.equationSystemList = new ArrayList<>();
        for(CellArea area: cellAreas){
            SystemOfEquations tempSystem = new SystemOfEquations(area, space.allCells);
            equationSystemList.add(tempSystem);
        }
    }

    public void setThreads(CalculationThread[] threads) {

        this.threads = new ArrayList<>();
        for(int i=0; i<threads.length;i++){
            this.threads.add(threads[i]);
        }
    }

    private void waitSolidCalculationsReady(){
        while (!statusReached(1)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitSolidValuesOverwritten(){
        while (!statusReached(2)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitFluidCalculationsReady(){
        while (!statusReached(3)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitEquationsFilled(){
        while (!statusReached(4)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitOverwritingOldValuesReady(){
        while (!statusReached(6)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitCellsNormalized(){
        while (!statusReached(5)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private boolean statusReached(int state){
        for(CalculationThread thread: this.threads){
            if (thread.status != state){
                return false;
            }
        }
        return true;
    }

    @Override
    public void run(){
        for (int counter = 0; counter< this.numberSteps; counter++) {
            //solid calculation
            this.runAllThreads();
            this.solidCalculation();
            waitSolidCalculationsReady();

            //overwrite solid values
            this.runAllThreads();
            this.overwriteOldSolidValues();
            waitSolidValuesOverwritten();

            //fluid calculation
            this.runAllThreads();
            this.particleFlowCalculation();
            waitFluidCalculationsReady();

            //fill all equations
            this.runAllThreads();
            this.fillEquations(this.equationSystemList, this.areas);
            waitEquationsFilled();

            //solve equations
            this.solveEquations();

            //calc new temperature
            this.runAllThreads();
            this.normalizeCells(this.equationSystemList, this.areas);
            waitCellsNormalized();




            //overwriting old values
            this.runAllThreads();
            this.overwriteOldSolidValues();
            waitOverwritingOldValuesReady();

            //end reached and restart
            this.endOfStepReached();
        }
    }



    private void endOfStepReached(){
        this.status=0;
            space.increaseNumberCalculatedSteps();
        for (CalculationThread thread: this.threads){
            thread.status=0;
        }
    }

    private void solveEquations(){
        for (SystemOfEquations equations: this.equationSystemList){
            equations.solveEquations();
        }
    }

    private void runAllThreads(){
        for(CalculationThread thread: this.threads){
            thread.run(this.equationSystemList, this.areas);
        }
    }

}
