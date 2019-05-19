package Heatequation;

import Heatequation.Cells.Coordinates;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
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
        space.createSolidCube(1,0,0,2,2,2,10,"Iron");

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


