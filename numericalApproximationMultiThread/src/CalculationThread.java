
import Cells.Cell;
import Cells.CellArea;
import Cells.Coordinates;
import Cells.FluidCell;

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
            this.particleFlowCalculation();
        }else if (this.status == 3){
            this.fillEquations(equationsList, fluidAreaList);
        }else if(this.status==4){
            this.normalizeCells(equationsList, fluidAreaList);
        }else if (this.status==5) {
            this.overwriteOldFluidValues();
        }
            //TODO: Change type if fluid calculation is implemented

            stop();
        }




        protected void overwriteOldSolidValues(){
            for (Coordinates solidCell: this.solidCells){
                space.allCells.getCell(solidCell).setOldValue();
            }

            this.status=2;
        }

        protected void overwriteOldFluidValues(){

            for (Coordinates fluidCell: this.fluidCells){
                space.allCells.getCell(fluidCell).setOldValue();
            }
            this.status=6;
        }


    protected void solidCalculation(){
        space.calcSolidCells(solidCells);
        this.status=1;
    }

    protected void particleFlowCalculation(){
        space.calculateParticleFlowForCells(fluidCells);
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
        this.status = 4;
    }

    protected void normalizeCells(List<SystemOfEquations> equationList, List<CellArea> areaList){

        //set self and initialize calculaton
        for(Coordinates coords : this.fluidCells){
                this.space.allCells.getCell(coords).getAsFluidCell().addSelfToNumberParticlesForTemperatureCalculation();
        }

        //add each particle from neighbor cells
        for(Coordinates coords : this.fluidCells){
            try {
                normalizeCell(coords, equationList.get(this.getEquationIndexForCell(areaList,this.space.allCells.getCell(coords).getAsFluidCell())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //normailize all cells
        for(Coordinates coords : this.fluidCells){
            this.space.allCells.getCell(coords).getAsFluidCell().normalizeNumberParticlesAndTemperature();
        }
        this.status = 5;

    }

    protected void normalizeCell(Coordinates centerCell, SystemOfEquations systemOfEquations){
        double tempTemperature;
        FluidCell cell  = this.space.allCells.getCell(centerCell).getAsFluidCell();
        for(Coordinates adjacentCord : space.allCells.getAllAdjacentFluidCells(centerCell)){
            //add temperatures and particle flow to cell
            FluidCell adjacentCell = this.space.allCells.getCell(adjacentCord).getAsFluidCell();

            try {
                adjacentCell.addToNumberParticlesForTemperatureCalculation(systemOfEquations.getResultForCoordinates(adjacentCord)/space.allCells.getAllAdjacentFluidCells(adjacentCord).size(), adjacentCell.getLastValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
