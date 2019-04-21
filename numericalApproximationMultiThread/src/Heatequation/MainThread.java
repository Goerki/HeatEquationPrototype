package Heatequation;

import Heatequation.Cells.Cell;
import Heatequation.Cells.CellArea;
import Heatequation.Cells.Coordinates;

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
            SystemOfEquations tempSystem = new SystemOfEquations(area, space.allCells, space.logger);
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

    private void waitFluidValuesOverwriten(){
        while (!statusReached(4)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    private void waitEquationsFilled(){
        while (!statusReached(5)){
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
        while (!statusReached(6)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    private void waitNormalizationFinished(){
        while (!statusReached(7)){
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
            Coordinates logCoords = new Coordinates(2,2,2);
            this.space.logFluidCell("beginning        ", logCoords);
            //solid calculation , status 1
            this.runAllThreads();
            this.solidCalculation();
            waitSolidCalculationsReady();
            this.space.logFluidCell("afterSolid       ", logCoords);

            //overwrite solid values , status 2
            this.runAllThreads();
            this.overwriteOldSolidValues();
            waitSolidValuesOverwritten();
            this.space.logFluidCell("solidOverwritten ", logCoords);

            //fluid calculation status 3
            this.runAllThreads();
            this.particleFlowCalculation();
            waitFluidCalculationsReady();
            this.space.logFluidCell("fluidCalculation ", logCoords);


            //overwrite fluid values status 4
            this.runAllThreads();
            this.overwriteOldFluidValues();;
            waitFluidValuesOverwriten();
            this.space.logFluidCell("fluidOverwritten ", logCoords);


            //fill all equations status 5
            this.calcPressureForEachArea();
            this.runAllThreads();
            this.fillEquations(this.equationSystemList, this.areas);
            //this.fillBorderBoundaries();
            waitEquationsFilled();
            this.space.logFluidCell("equationsFilled  ", logCoords);

            //solve equations status 5?
            this.solveEquations();
            if (!this.equationSystemList.isEmpty()) {
                this.equationSystemList.get(0).draw();
            }

            this.checkEquationResult(logCoords);

            //this.limitEquations();

            //calc new temperature status 6
            this.runAllThreads();
            this.normalizeCells(this.equationSystemList, this.areas);
            waitCellsNormalized();
            this.space.logFluidCell("cellsNormalized  ", logCoords);


            //overwrite old values - status 7
            this.runAllThreads();
            this.finishNormalization();
            waitNormalizationFinished();
            this.space.logFluidCell("Normalization finished  ", logCoords);



            //for debugging purposes only
            this.calcPressureForEachArea();


            //overwriting old values
            /*
            this.runAllThreads();
            this.overwriteOldSolidValues();
            waitOverwritingOldValuesReady();
            this.space.logFluidCell("overwritten      ", logCoords);

*/
            //end reached and restart
            this.endOfStepReached();
        }
    }

    private void calcPressureForEachArea() {
        for (SystemOfEquations systemOfEquations: this.equationSystemList){
            systemOfEquations.setPressure();

        }
    }

    private void limitEquations() {
        for (SystemOfEquations equations: this.equationSystemList){
            equations.limitEquations();
        }
    }


    private void checkEquationResult(Coordinates coords){

        try {
            SystemOfEquations equations = this.equationSystemList.get(this.getEquationIndexForCell(this.areas,this.space.allCells.getCell(coords).getAsFluidCell()));

            double N1 = equations.getResultForCoordinates(coords);
            double N0 = space.allCells.getCell(coords).getAsFluidCell().getLastNumberParticles();
            double T0 = space.allCells.getCell(coords).getAsFluidCell().getLastValue();
            List<Double> resList = new ArrayList<>();
            List<Double> tempList = new ArrayList<>();
            List<Integer> neighList = new ArrayList<>();
            for (Coordinates adjacentCell : this.space.allCells.getAllAdjacentFluidCells(coords)){
                resList.add(equations.getResultForCoordinates(adjacentCell));
                tempList.add(space.allCells.getCell(coords).getAsFluidCell().getLastValue());
                neighList.add(space.allCells.getNumberOfAdjacentFluidCells(adjacentCell));
            }
            if (this.space.allCells.getCell(coords).getAsFluidCell().isBorderCell()){
                resList.add(equations.getResultForVirtualCell(coords));
                tempList.add(this.space.allCells.getCell(coords).getAsFluidCell().getTemperatureOfBorderCell());
                //neighList.add(this.space.allCells.getCell(coords).getAsFluidCell().getNumberOfVirtualBorders());
                neighList.add(1);

            }


            double finalResult = 0;
                finalResult -= N1*T0;

            for(int i=0; i < resList.size(); i++){

                    finalResult += resList.get(i) * tempList.get(i) / neighList.get(i);

            }

            space.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "final Result: " + finalResult);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void fillBorderBoundaries() {
        for(SystemOfEquations equations: this.equationSystemList){
            equations.fillBorderBoundaries();
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
