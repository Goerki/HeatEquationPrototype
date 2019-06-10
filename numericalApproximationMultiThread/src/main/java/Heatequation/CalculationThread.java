package Heatequation;
import Heatequation.Cells.CellArea;
import Heatequation.Cells.Coordinates;
import Heatequation.Cells.FluidCell;

import java.io.Serializable;
import java.util.List;

public class CalculationThread extends Thread implements Serializable {
    protected Space space;
    protected List<Coordinates> solidCells;
    protected List<Coordinates> fluidCells;
    protected int numberSteps;
    protected int status;

    public CalculationThread(Space space, List<Coordinates> solidCells,List<Coordinates> fluidCells , int steps){
        this.space = space;
        this.solidCells = solidCells;
        this.fluidCells = fluidCells;
        this.numberSteps = steps;
        this.status = 0;
    }

    public int getStatus(){return this.status;}


    public void run(List<SystemOfEquations> equationsList, List<CellArea> fluidAreaList){
        if (this.status==0) {
            this.solidCalculation();
        }else if (this.status==1) {
            this.overwriteOldSolidValues();
        }else if (this.status==2) {
            this.applyDiffussionAndUplift();
        }else if (this.status == 3){
            this.calulateInertiaParticleFlow();
        }else if (this.status == 4){
            this.applyInertiaParticleFlow();
        }else if(this.status==5){
            this.overwriteOldFluidValues();
        }else if (this.status==6) {
            this.fillEquations(equationsList, fluidAreaList);
        }else if (this.status==7) {
            this.normalizeCells(equationsList, fluidAreaList);
        }else if (this.status==8) {
            this.finishNormalization();
        }
            //TODO: Change type if fluid calculation is implemented

            stop();
        }




        protected void overwriteOldSolidValues(){
            for (Coordinates solidCell: this.solidCells){
                space.allCells.getCell(solidCell).setOldValue();
            }
            for(Coordinates fluidCell: this.fluidCells){
                space.allCells.getCell(fluidCell).getAsFluidCell().initializeNormalization(0);
            }

            this.status=2;
        }



    protected void overwriteOldFluidValues(){

            for (Coordinates fluidCell: this.fluidCells){
                space.allCells.getCell(fluidCell).getAsFluidCell().normalizeNumberParticlesAndTemperature();
                space.allCells.getCell(fluidCell).getAsFluidCell().resetInertiaParticleFlow();
            }
            this.status=6;
        }


    protected void solidCalculation(){
        space.calcSolidCells(solidCells);
        this.status=1;
    }

    protected void applyDiffussionAndUplift(){
        space.applyDiffussionAndUpliftOnCells(fluidCells);
        this.status=3;
    }

    protected int getEquationIndexForCell(List<CellArea> areaList, FluidCell cell) throws Exception{
        for(int index = 0; index < areaList.size(); index++){
            if (cell.getArea() == areaList.get(index)){
                return index;

            }
        }
        throw new Exception("Equation index for cell not found - cell not found in any area");
    }


    protected void fillEquations(List<SystemOfEquations> equationsList, List<CellArea> areaList){
        for (Coordinates eachCoord: this.fluidCells){
            FluidCell eachCell = this.space.allCells.getCell(eachCoord).getAsFluidCell();

            //get number of area
            int index = 0;
            try {
                index = this.getEquationIndexForCell(areaList, eachCell);
                equationsList.get(index).addToEquations(eachCoord, areaList.get(index));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        this.status = 7;
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
        Coordinates logCoords = new Coordinates(2,2,2);
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


         this.status = 8;

    }

    protected void finishNormalization() {
        Coordinates logCoords = new Coordinates(2,2,2);
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
        this.status = 9;
    }

    protected void normalizeCell(Coordinates centerCell, SystemOfEquations systemOfEquations){

        Coordinates logCoords = new Coordinates(2,2,2);

        FluidCell cell  = this.space.allCells.getCell(centerCell).getAsFluidCell();
        if (centerCell.equals(logCoords)){
            try {
                this.space.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "result center:" + systemOfEquations.getResultForCoordinates(centerCell));
                if (cell.isBorderCell()){
                    this.space.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "result virtual cells: " + systemOfEquations.getResultForVirtualCell(logCoords));
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

        for (Coordinates eachCoord: systemOfEquations.area.getNearFieldCoordinatesForCell(centerCell)){
            //add temperatures and particle flow to cell
                FluidCell adjacentCell = this.space.allCells.getCell(eachCoord).getAsFluidCell();
                try {
                    double factor = systemOfEquations.area.getFactorFor(centerCell, eachCoord);
                    //double factor = 1/((double) systemOfEquations.area.getNearFieldCoordinatesForCell(centerCell).size()+1.0);
                    adjacentCell.addToNumberParticlesForTemperatureCalculationDuringNormalization(systemOfEquations.getResultForCoordinates(centerCell) *factor, cell.getLastValue());
                    cell.addToNumberParticlesForTemperatureCalculationDuringNormalization(-systemOfEquations.getResultForCoordinates(centerCell)*factor, cell.getLastValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //add flow from virtual cells
                if (cell.isBorderCell()) {
                    cell.addToNumberParticlesForTemperatureCalculationFromVirtualBorderCell(systemOfEquations.getResultForVirtualCell(centerCell));
                }


                if (centerCell.equals(logCoords)) {
                    try {
                        this.space.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "result " + eachCoord + ": " + systemOfEquations.getResultForCoordinates(eachCoord));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
    }

    protected void calulateInertiaParticleFlow() {
        for (Coordinates eachCell: this.fluidCells){
            space.calculateInertiaParticleFlowForCell(eachCell);

        }
        this.status = 4;
    }

    protected void applyInertiaParticleFlow() {
        for (Coordinates eachCell: this.fluidCells){
            space.applyInertiaParticleFlowForCell(eachCell);

        }
        this.status = 5;
    }
}
