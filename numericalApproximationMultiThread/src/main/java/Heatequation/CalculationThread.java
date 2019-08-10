package Heatequation;
import Heatequation.Cells.CellArea;
import Heatequation.Cells.Coordinates;
import Heatequation.Cells.FluidCell;
import Heatequation.Cells.Junction;

import java.io.Serializable;
import java.util.List;

public class CalculationThread extends Thread implements Serializable {
    protected Space space;
    protected List<Coordinates> solidCells;
    protected List<Coordinates> fluidCells;
    protected int numberSteps;
    protected status status;
    public enum status{
        SOLID_CALC, SOLID_OVERWRITE, INITIALIZE_PARTICLEFLOW, APPLY_DIFF_AND_UPLIFT, CALCULATE_INERTIA, APPLY_INERTIA, FLUID_OVERWRITE, FILL_BOUNDARIES, SOLVE_EQUATION, NORMAILZE_CELLS, FINISH
    };

    public CalculationThread(Space space, List<Coordinates> solidCells,List<Coordinates> fluidCells , int steps){
        this.space = space;
        this.solidCells = solidCells;
        this.fluidCells = fluidCells;
        this.numberSteps = steps;
        this.status = status.SOLID_CALC;

    }

    public status getStatus(){return this.status;}


    public void run(List<SystemOfEquations> equationsList, List<CellArea> fluidAreaList){
        if (this.status==status.SOLID_CALC) {
            this.solidCalculation();
            this.status = status.SOLID_OVERWRITE;
        }else if (this.status==status.SOLID_OVERWRITE) {
            this.overwriteOldSolidValues();
            this.status = status.INITIALIZE_PARTICLEFLOW;
        }else if (this.status==status.INITIALIZE_PARTICLEFLOW) {
            this.initParticleFlowCalculation();
            this.status = status.APPLY_DIFF_AND_UPLIFT;
        }else if (this.status==status.APPLY_DIFF_AND_UPLIFT) {
            this.applyDiffussionAndUplift();
            this.status = status.CALCULATE_INERTIA;
        }else if (this.status == status.CALCULATE_INERTIA){
            this.calulateInertiaParticleFlow();
            this.status = status.APPLY_INERTIA;
        }else if (this.status == status.APPLY_INERTIA){
            this.applyInertiaParticleFlow();
            this.status = status.FLUID_OVERWRITE;
        }else if(this.status== status.FLUID_OVERWRITE){
            this.overwriteOldFluidValues();
            this.status = status.FILL_BOUNDARIES;
        }else if (this.status== status.FILL_BOUNDARIES) {
            this.fillBoundaries(equationsList, fluidAreaList);
            this.status = status.SOLVE_EQUATION;
        }else if (this.status== status.SOLVE_EQUATION) {
            this.solveEquation(equationsList, fluidAreaList);
            this.status = status.NORMAILZE_CELLS;
        }else if (this.status==status.NORMAILZE_CELLS) {
            this.normalizeCells(equationsList, fluidAreaList);
            this.status = status.FINISH;
        }else if (this.status==status.FINISH) {
            this.finishNormalization();
            this.status = status.SOLID_CALC;
        }
            //TODO: Change type if fluid calculation is implemented

            stop();
        }

    protected void solveEquation(List<SystemOfEquations> equationsList, List<CellArea> fluidAreaList) {
        for (Coordinates eachCoord: this.fluidCells){
            FluidCell eachCell = this.space.allCells.getCell(eachCoord).getAsFluidCell();

            //get number of area
            int index = 0;
            try {
                index = this.getEquationIndexForCell(fluidAreaList, eachCell);
                equationsList.get(index).calcResultFor(eachCoord);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    protected void overwriteOldSolidValues(){
            for (Coordinates solidCell: this.solidCells){
                space.allCells.getCell(solidCell).setOldValue();
            }
        }

    protected void initParticleFlowCalculation(){

        for(Coordinates fluidCell: this.fluidCells){
            space.allCells.getCell(fluidCell).getAsFluidCell().initializeNormalization(0);
        }


    }



    protected void overwriteOldFluidValues(){

            for (Coordinates fluidCell: this.fluidCells){
                space.allCells.getCell(fluidCell).getAsFluidCell().normalizeNumberParticlesAndTemperature();
                space.allCells.getCell(fluidCell).getAsFluidCell().resetInertiaParticleFlow();
            }

        }


    protected void solidCalculation(){
        space.calcSolidCells(solidCells);

    }

    protected void applyDiffussionAndUplift(){
        space.applyDiffussionAndUpliftOnCells(fluidCells);

    }

    protected int getEquationIndexForCell(List<CellArea> areaList, FluidCell cell) throws Exception{
        for(int index = 0; index < areaList.size(); index++){
            if (cell.getArea() == areaList.get(index)){
                return index;

            }
        }
        throw new Exception("Equation index for cell not found - cell not found in any area");
    }


    protected void fillBoundaries(List<SystemOfEquations> equationsList, List<CellArea> areaList){
        for (Coordinates eachCoord: this.fluidCells){
            FluidCell eachCell = this.space.allCells.getCell(eachCoord).getAsFluidCell();

            //get number of area
            int index = 0;
            try {
                index = this.getEquationIndexForCell(areaList, eachCell);
                equationsList.get(index).addToBoundaries(eachCoord);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    protected void initializeNormalization(){
        for(Coordinates coords : this.fluidCells){
            try {
                //this.space.allCells.getCell(coords).getAsFluidCell().initializeNormalization(equationList.get(this.getEquationIndexForCell(areaList,this.space.allCells.getCell(coords).getAsFluidCell())).getResultForCoordinates(coords));
                this.space.allCells.getCell(coords).getAsFluidCell().initializeNormalization(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected void initializeNormalizationForAllCellsInArea(CellArea area){
        for(Coordinates coords : area.coords){
            try {
                //this.space.allCells.getCell(coords).getAsFluidCell().initializeNormalization(equationList.get(this.getEquationIndexForCell(areaList,this.space.allCells.getCell(coords).getAsFluidCell())).getResultForCoordinates(coords));
                this.space.allCells.getCell(coords).getAsFluidCell().initializeNormalization(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void normalizeCells(List<SystemOfEquations> equationList, List<CellArea> areaList){
        Coordinates logCoords = new Coordinates(2,4,2);
        //set self and initialize calculaton
        //this.initializeNormalization();

        //add each particle from neighbor cells
        for(Coordinates coords : this.fluidCells){
            try {
                normalizeCell(coords, equationList.get(this.getEquationIndexForCell(areaList,this.space.allCells.getCell(coords).getAsFluidCell())));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }




    }

    protected void finishNormalization() {
        Coordinates logCoords = new Coordinates(2,4,2);
        //normailize all cells
        for (Coordinates coords : this.fluidCells) {

            if (coords.equals(logCoords)) {
                this.space.logFluidCell("before normailization", logCoords);

            }

            this.space.allCells.getCell(coords).getAsFluidCell().normalizeNumberParticlesAndTemperature();

            if (coords.equals(logCoords)) {
                this.space.logFluidCell("after normailization", logCoords);

            }
        }

    }

    protected void normalizeCell(Coordinates centerCell, SystemOfEquations systemOfEquations){

        Coordinates logCoords = new Coordinates(2,4,2);

        FluidCell cell  = this.space.allCells.getCell(centerCell).getAsFluidCell();
        if (centerCell.equals(logCoords)){
            try {
                for(Junction eachJunction: systemOfEquations.area.getOutgoingJunctionsForCell(centerCell)){
                    this.space.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "result " + eachJunction + " : " + systemOfEquations.getResultForJunction(eachJunction));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /*
        try {
            cell.addToNumberParticlesForTemperatureCalculationDuringNormalization(-systemOfEquations.getResultForCoordinates(centerCell), cell.getLastValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        for (Junction eachJunction: systemOfEquations.area.getOutgoingJunctionsForCell(centerCell)){
            //add temperatures and particle flow to cell
            FluidCell targetCell;
            FluidCell sourceCell;
            try {
                if (eachJunction.getFrom() == eachJunction.getTo()){
                    //virtual cell
                    double particleFlow = systemOfEquations.getResultForJunction(eachJunction)/space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell().getNumberOfVirtualBorders();
                    if (particleFlow >0){
                        particleFlow/= space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell().getLastValue();
                        } else {
                        particleFlow/= space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell().getTemperatureOfBorderCell();
                    }
                    for (Coordinates.direction eachVirtualBorderDirection: space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell().getBordercellDirections()){
                        space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell().addToNumberParticlesForTemperatureCalculationDuringNormalization(particleFlow, space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell().getLastValue());
                    }
                } else {
                    if (systemOfEquations.getResultForJunction(eachJunction) > 0) {
                        targetCell = this.space.allCells.getCell(eachJunction.getTo()).getAsFluidCell();
                        sourceCell = this.space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell();
                    } else {
                        targetCell = this.space.allCells.getCell(eachJunction.getFrom()).getAsFluidCell();
                        sourceCell = this.space.allCells.getCell(eachJunction.getTo()).getAsFluidCell();
                    }

                    double result = systemOfEquations.getAbsoluteResultForJunction(eachJunction);
                    double particleFlow = result / sourceCell.getLastValue();


                    targetCell.addToNumberParticlesForTemperatureCalculationDuringNormalization(particleFlow, sourceCell.getLastValue());
                    sourceCell.addToNumberParticlesForTemperatureCalculationDuringNormalization(-particleFlow, sourceCell.getLastValue());
                }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //add flow from virtual cells
                if (cell.isBorderCell()) {
                    //cell.addToNumberParticlesForTemperatureCalculationFromVirtualBorderCell(systemOfEquations.getResultForVirtualCell(centerCell));
                }


                if (centerCell.equals(logCoords)) {
                    try {
                        this.space.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "result " + eachJunction + ": " + systemOfEquations.getResultForJunction(eachJunction));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
    }

    protected void calulateInertiaParticleFlow() {
        for (Coordinates eachCell: this.fluidCells){
            //space.calculateInertiaParticleFlowForCell(eachCell);

        }

    }

    protected void applyInertiaParticleFlow() {
        for (Coordinates eachCell: this.fluidCells){
            //space.applyInertiaParticleFlowForCell(eachCell);

        }

    }
}
