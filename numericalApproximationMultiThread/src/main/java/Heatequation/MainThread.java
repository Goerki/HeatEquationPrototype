package Heatequation;

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

    private void waitDiffussionAndUpliftApplied(){
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
        while (!statusReached(6)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    private void waitEquationsFilled(){
        while (!statusReached(7)){
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
        while (!statusReached(8)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    private void waitNormalizationFinished(){
        while (!statusReached(9)){
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

            long start = System.currentTimeMillis();
            this.space.logFluidCell("beginning        ", logCoords);
            //solid calculation , status 1
            this.runAllThreads();
            this.solidCalculation();
            waitSolidCalculationsReady();
            this.space.logFluidCell("afterSolid       ", logCoords);
            long duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "solid calculation took " + duration + " ms");


            //overwrite solid values , status 2
            start = System.currentTimeMillis();
            this.runAllThreads();
            this.overwriteOldSolidValues();
            waitSolidValuesOverwritten();
            this.space.logFluidCell("solidOverwritten ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "solid overwrite took " + duration + " ms");



            //uplift and diffussion status 3
            start = System.currentTimeMillis();
            this.runAllThreads();
            this.applyDiffussionAndUplift();
            waitDiffussionAndUpliftApplied();
            this.space.logFluidCell("fluidCalculation ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "calculation of diffussion and uplift took " + duration + " ms");

            //calculate Inertia Particle Flow status 4
            start = System.currentTimeMillis();
            this.runAllThreads();
            this.calulateInertiaParticleFlow();
            waitInertialParticleFlowCalculated();
            this.space.logFluidCell("fluidCalculation ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "calculation of inertia flow took " + duration + " ms");


            start = System.currentTimeMillis();
            this.resetAllParticleFlows();
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "reset of particle flow took " + duration + " ms");

            //calculate Inertia Particle Flow status 5

            start = System.currentTimeMillis();
            this.runAllThreads();
            this.applyInertiaParticleFlow();
            waitInertialParticleFlowApplied();
            this.space.logFluidCell("fluidCalculation ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "fluid calculation took " + duration + " ms");


            //overwrite fluid values status 6
            start = System.currentTimeMillis();
            this.runAllThreads();
            this.overwriteOldFluidValues();;
            waitFluidValuesOverwriten();
            this.space.logFluidCell("fluidOverwritten ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "overwrite of fluid values took " + duration + " ms");



            //fill all equations status 7
            start = System.currentTimeMillis();
            this.calcPressureForEachArea();
            this.setAllAveragesInEquations();

            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "calculation of pressure and averages took " + duration + " ms");

            start = System.currentTimeMillis();
            this.runAllThreads();
            this.fillEquations(this.equationSystemList, this.areas);
            //this.fillBorderBoundaries();
            waitEquationsFilled();
            this.space.logFluidCell("equationsFilled  ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "creation and filling of equations took " + duration + " ms");


            //solve equations status
            start = System.currentTimeMillis();
            this.solveEquations();
            if (!this.equationSystemList.isEmpty()) {
                this.equationSystemList.get(0).draw();
            }
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "solving of the equations took " + duration + " ms");

            this.checkEquationResult(logCoords);

            //this.limitEquations();

            //calc new temperature status 8
            start = System.currentTimeMillis();
            this.runAllThreads();
            this.normalizeCells(this.equationSystemList, this.areas);
            waitCellsNormalized();
            this.space.logFluidCell("cellsNormalized  ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "normalizing of cells took " + duration + " ms");


            //overwrite old values - status 9
            start = System.currentTimeMillis();
            this.runAllThreads();
            this.finishNormalization();
            waitNormalizationFinished();
            this.space.logFluidCell("Normalization finished  ", logCoords);
            duration = System.currentTimeMillis() - start;
            this.space.logger.logMessage(HeatequationLogger.LogLevel.INFO, "finishing normalization took " + duration + " ms");



            //for debugging purposes only
            //this.calcPressureForEachArea();


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

    private void waitInertialParticleFlowApplied() {
        while (!statusReached(5)){
            try {
                TimeUnit.MICROSECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void waitInertialParticleFlowCalculated() {
        while (!statusReached(4)){
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

    private void calcPressureForEachArea() {
        for (SystemOfEquations systemOfEquations: this.equationSystemList){
            systemOfEquations.setPressure();

        }
    }



    private void setAllAveragesInEquations(){
        for (SystemOfEquations eachSystem: this.equationSystemList){
            eachSystem.setEnergySum();
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
                tempList.add(space.allCells.getCell(adjacentCell).getAsFluidCell().getLastValue());
                neighList.add(space.allCells.getNumberOfAdjacentFluidCells(adjacentCell));
            }
            if (this.space.allCells.getCell(coords).getAsFluidCell().isBorderCell()){
                resList.add(equations.getResultForVirtualCell(coords));
                tempList.add(this.space.allCells.getCell(coords).getAsFluidCell().getTemperatureOfBorderCell());
                neighList.add(this.space.allCells.getCell(coords).getAsFluidCell().getNumberOfVirtualBorders());
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
