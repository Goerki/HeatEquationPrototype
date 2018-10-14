import org.tc33.jheatchart.HeatChart;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) {



        Space space = new Space(3);
        System.out.println("gubl \n");
        long beginTime = System.currentTimeMillis();
        System.out.println(space.toString());


        HeatChart heatTest = new HeatChart(space.getXLayer(2), 0, 50);
        heatTest.setHighValueColour(Color.RED);
        heatTest.setLowValueColour(Color.CYAN);
        heatTest.setTitle("Testplot");

        System.out.print("high: " + heatTest.getHighValue() + " low: " + heatTest.getLowValue());




            space.calcSpace(10000, 1000);
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
}


