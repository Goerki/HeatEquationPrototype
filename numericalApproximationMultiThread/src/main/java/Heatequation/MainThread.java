package Heatequation;

import Heatequation.Cells.CellArea;
import Heatequation.Cells.Coordinates;
import org.apache.commons.math3.analysis.function.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainThread extends CalculationThread  {
    long numberSteps;
    List<CalculationThread> threads;
    List<CellArea> areas;
    List<SystemOfEquations> equationSystemList;
    int nextStepOfSnapshot;
    int numberSnapshots;



    public MainThread(Space space, List<Coordinates> solidCells,List<Coordinates> fluidCells, int numberSteps, List<CellArea> fluidAreas){
        super(space, solidCells, fluidCells, numberSteps);
        this.numberSteps=numberSteps;
        this.threads = new ArrayList<>();
        this.areas = fluidAreas;
        this.createEquationSystems(space, this.areas);
        this.numberSnapshots = space.history.length;
        nextStepOfSnapshot = (int)this.numberSteps/this.numberSnapshots;
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
        while (!statusReached(status.SOLID_OVERWRITE)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitSolidValuesOverwritten(){
        while (!statusReached(status.INITIALIZE_PARTICLEFLOW)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitParticleFlowCalculationInitialized(){
        while (!statusReached(status.APPLY_DIFF_AND_UPLIFT)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitDiffussionAndUpliftApplied(){
        while (!statusReached(status.CALCULATE_INERTIA)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitFluidValuesOverwriten(){
        while (!statusReached(status.FILL_EQUATIONS)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    private void waitEquationsFilled(){
        while (!statusReached(status.NORMAILZE_CELLS)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitOverwritingOldValuesReady(){
        while (!statusReached(status.FINISH)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitCellsNormalized(){
        while (!statusReached(status.FINISH)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    private void waitNormalizationFinished(){
        while (!statusReached(status.SOLID_CALC)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private boolean statusReached(CalculationThread.status state){
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
            Coordinates logCoords = new Coordinates(2,4,2);


            this.space.logFluidCell("beginning        ", logCoords);
            //solid calculation , status 1
            this.runAllThreads();
            this.solidCalculation();
            waitSolidCalculationsReady();
            this.space.logFluidCell("afterSolid       ", logCoords);

            this.calcPressureForEachArea();
            this.calcPressureCalculationFailureForAllEquations();
            System.out.print("after solid calculation");
            //this.areas.get(0).printPressureForAllCells(space);

            //for debugging purposes only

            //overwrite solid values , status 2

            this.runAllThreads();
            this.overwriteOldSolidValues();
            waitSolidValuesOverwritten();
            this.space.logFluidCell("solidOverwritten ", logCoords);


            this.runAllThreads();
            this.initParticleFlowCalculation();
            waitParticleFlowCalculationInitialized();
            this.space.logFluidCell("flow initialized ", logCoords);




            //for debugging purposes only
            //this.calcPressureForEachArea();

            //uplift and diffussion status 3

            this.runAllThreads();
            this.applyDiffussionAndUplift();
            waitDiffussionAndUpliftApplied();
            this.space.logFluidCell("fluidCalculation ", logCoords);
            System.out.print("after solid calculation");
            this.areas.get(0).printPressureForAllCells(space);


            //for debugging purposes only
            //this.calcPressureForEachArea();

            //calculate Inertia Particle Flow status 4

            this.runAllThreads();
            this.calulateInertiaParticleFlow();
            waitInertialParticleFlowCalculated();
            this.space.logFluidCell("fluidCalculation ", logCoords);



            //for debugging purposes only
            //this.calcPressureForEachArea();

            this.resetAllParticleFlows();


            //calculate Inertia Particle Flow status 5

            this.runAllThreads();
            this.applyInertiaParticleFlow();
            waitInertialParticleFlowApplied();
            this.space.logFluidCell("fluidCalculation ", logCoords);
            this.areas.get(0).printPressureForAllCells(space);


            //for debugging purposes only
            //this.calcPressureForEachArea();

            //overwrite fluid values status 6
            this.runAllThreads();
            this.overwriteOldFluidValues();;
            waitFluidValuesOverwriten();
            this.space.logFluidCell("fluidOverwritten ", logCoords);





            //fill all equations status 7
            //this.calcPressureForEachArea();
            this.setAllAveragesInEquations();

            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "\n\nbefore flow from border");
            this.areas.get(0).printPressureForAllCells(space);
            this.applyParticleFlowFromBorderCellsToAllAreas();



            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "\n\nbefore equation");
            //this.calcPressureForEachArea();
            //this.calcPressureCalculationFailureForAllEquations();
            this.areas.get(0).printPressureForAllCells(space);

            this.runAllThreads();
            this.fillEquations(this.equationSystemList, this.areas);
            //this.fillBorderBoundaries();
            waitEquationsFilled();
            this.space.logFluidCell("equationsFilled  ", logCoords);



            //for debugging purposes only
            //this.calcPressureForEachArea();

            //solve equations status

            this.solveEquations();
            if (!this.equationSystemList.isEmpty()) {
                this.equationSystemList.get(0).draw();
            }


            this.checkEquationResult(logCoords);
            //non functional:
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "sum of all results: " + this.equationSystemList.get(0).getSumOfAllResults());


            this.initializeNormalizationForAllAreas();

            //this.limitEquations();

            //calc new temperature status 8

            this.runAllThreads();
            this.normalizeCells(this.equationSystemList, this.areas);
            waitCellsNormalized();
            this.space.logFluidCell("cellsNormalized  ", logCoords);



            //overwrite old values - status 9
            this.runAllThreads();
            this.finishNormalization();
            waitNormalizationFinished();
            this.space.logFluidCell("Normalization finished  ", logCoords);



            //for debugging purposes only
            //this.calcPressureForEachArea();
            this.areas.get(0).printPressureForAllCells(space);
            //this.applyPressureForEachArea();

            //this.areas.get(0).printPressureForAllCells(space);

            //overwriting old values
            /*
            this.runAllThreads();
            this.overwriteOldSolidValues();
            waitOverwritingOldValuesReady();
            this.space.logFluidCell("overwritten      ", logCoords);

*/
            //end reached and restart
             this.endOfStepReached(counter);
        }
    }

    private void waitInertialParticleFlowApplied() {
        while (!statusReached(status.FLUID_OVERWRITE)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitInertialParticleFlowCalculated() {
        while (!statusReached(status.APPLY_INERTIA)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void resetAllParticleFlows() {
        for (CellArea area: areas){
            if (area.isFluid()){
                for (Coordinates coord: area.coords){
                    this.space.allCells.getCell(coord).getAsFluidCell().resetParticleFlow();
                }
            }
        }
    }

    private void initializeNormalizationForAllAreas(){
        for (CellArea eachArea: this.areas){
            if (eachArea.isFluid()){
                initializeNormalizationForAllCellsInArea(eachArea);
            }
        }
    }


    public void applyParticleFlowFromBorderCellsToAllAreas(){
        for (CellArea area: this.areas){
            if(area.isFluid() && area.isIsobar()){
                area.applyParticleFlowFromBorderCells(this.space);
            }
        }
    }

    private void calcPressureForEachArea() {
        for (SystemOfEquations systemOfEquations: this.equationSystemList){
            systemOfEquations.setPressure();
            //systemOfEquations.applyPressure();

        }
    }

    private void applyPressureForEachArea() {
        for (SystemOfEquations systemOfEquations: this.equationSystemList){
            systemOfEquations.setPressure();
            //systemOfEquations.verifyPressureForEachCell();
            systemOfEquations.applyPressure();

        }
    }

    private void calcPressureCalculationFailureForAllEquations() {
        for (SystemOfEquations systemOfEquations: this.equationSystemList){
            if (!systemOfEquations.isIsobar()) {
                systemOfEquations.calcPressureCalculationFailure();
                //systemOfEquations.verifyPressureForEachCell();
                //systemOfEquations.applyPressure();

            }
        }
    }





    private void setAllAveragesInEquations(){
        for (SystemOfEquations eachSystem: this.equationSystemList){
            eachSystem.setEnergySum();

        }
    }


    private void checkEquationResult(Coordinates coords){
/*
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
                tempList.add(space.allCells.getCell(adjacentCell).getAsFluidCell().getLastValue());
                neighList.add(space.allCells.getNumberOfAdjacentFluidCells(adjacentCell));
            }
            if (this.space.allCells.getCell(coords).getAsFluidCell().isBorderCell()){
                //resList.add(equations.getResultForVirtualCell(coords));
                //tempList.add(this.space.allCells.getCell(coords).getAsFluidCell().getTemperatureOfBorderCell());
                //neighList.add(this.space.allCells.getCell(coords).getAsFluidCell().getNumberOfVirtualBorders());
                }


            double finalResult = 0;
                finalResult = - N1*T0/space.allCells.getNumberOfAdjacentFluidCells(coords);

            for(int i=0; i < resList.size(); i++){
                    finalResult += resList.get(i) * tempList.get(i)/neighList.get(i);
            }

            //linke seite:
            double ideal = this.space.areas.get(1).getPressure()*this.space.getCellLength()*this.space.getCellLength()*this.space.getCellLength()/space.allCells.gasConstant;
            double lastStep = N0*T0;
            double leftSide = ideal - lastStep;
            space.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "final Result: "+leftSide + " = " + finalResult);


        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }

    private void fillBorderBoundaries() {
        for(SystemOfEquations equations: this.equationSystemList){
            equations.fillBorderBoundaries();
        }
    }


    private void endOfStepReached(int stepNumber){
        this.status=status.SOLID_CALC;
            space.increaseNumberCalculatedSteps();
        for (CalculationThread thread: this.threads){
            thread.status=status.SOLID_CALC;
        }
        //DEBUG
        this.space.allCells.calcAverages();
        this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "global number of particles: " +this.space.allCells.getNumberParticles() );

        if (stepNumber >= this.nextStepOfSnapshot){
            this.space.saveSnapshotInHistroy();
            this.nextStepOfSnapshot+= (int)this.numberSteps/this.numberSnapshots;
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
