package Heatequation;

import Heatequation.Cells.Coordinates;
import Heatequation.Cells.CustomLong;

import java.util.HashMap;

public class Test {

    public static void main(String[] args) {
        double x = 1000.0/3435;
        long test =      1000000000000000000L;
        double doubleTest = 1000.123456789123456;


        CustomLong testTemp = new CustomLong(0.0,12);

        CustomLong i1=  new CustomLong(1000,12);
        CustomLong i2=  new CustomLong(1E-12,12);
        CustomLong i3=  new CustomLong(6000000,12);
        CustomLong i4=  new CustomLong(0.01,12);
        CustomLong i5=  new CustomLong(0.0000001,12);
        CustomLong i6=  new CustomLong(1E35,12);
        i3.getAsDouble();

        i1.add(i2);

        i4.add(i5);


        CustomLong t1=  new CustomLong(4,8);
        i1=  new CustomLong(1,12);
        i1.add(t1);
        CustomLong t2=  new CustomLong(4,4);
        i1=  new CustomLong(1,12);
        i1.add(t2);
        CustomLong t3=  new CustomLong(4,15);
        i1=  new CustomLong(1,12);
        i1.add(t3);







        Coordinates eins = new Coordinates(1,1,1);
        Coordinates zwei = new Coordinates(1,1,2);
        HashMap<Coordinates, String> map = new HashMap<>();
        map.put(eins, "yay");
        map.put(zwei, "noe");


        if (!map.get(new Coordinates(1,1,1)).isEmpty()){
            System.out.print("das geht");
        }
        if (eins == zwei){
            System.out.print("geht doch!");

        }

    }




/*
    public static void test(){
        Space space = new Space(3,3,3,50,100,1);
        space.createSolidCube(1,0,0,2,3,2,10,"Iron");

        space.initialize(10);
        System.out.println("gubl \n");
        long beginTime = System.currentTimeMillis();
        System.out.println(space.toString());


        HeatChart heatTest = new HeatChart(space.getXLayer(2), 0, 50);
        heatTest.setHighValueColour(Color.RED);
        heatTest.setLowValueColour(Color.CYAN);
        heatTest.setTitle("Testplot");

        System.out.print("high: " + heatTest.getHighValue() + " low: " + heatTest.getLowValue());



        long endTime = System.currentTimeMillis();
        long duration = endTime - beginTime;

        System.out.print("duration: "+ duration);

        HeatChart heatTest2 = new HeatChart(space.getXLayer(2), 0, 50);
        try {


            new File("after.png").delete();
            heatTest2.setHighValueColour(Color.RED);
            heatTest2.setLowValueColour(Color.CYAN);
            heatTest2.setTitle("Testplot");
            System.out.print("high: " + heatTest2.getHighValue() + " low: " + heatTest2.getLowValue());
            heatTest2.saveToFile(new File("after.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("exception");
        }
        try {
            new File("start.png").delete();
            heatTest.saveToFile(new File("start.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("exception");
        }
    }
    */
}


