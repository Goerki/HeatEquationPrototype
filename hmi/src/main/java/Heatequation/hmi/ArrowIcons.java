package Heatequation.hmi;

import Heatequation.Cells.Cell;
import Heatequation.Cells.Coordinates;
import Heatequation.Cells.FluidCell;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ArrowIcons {
    int maxSize;
    int minSize;
    String axis;
    String iconPath;
    double minValue;
    double maxValue;

    public ArrowIcons(String iconPath, String axis, int minSize, int maxSize, double minValue, double maxValue){
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.axis = axis;
        this.iconPath = iconPath;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    public Image getImageForCell(FluidCell cell) throws Exception{
        Image arrow = null;
        try {
            arrow = ImageIO.read(new File(this.iconPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Double> cellResults =  this.getSizeAndDirectionForCell(cell.getAsFluidCell());
        if (cellResults.get("flow")==0.0){
            throw new Exception("no flow detected");
        }
        int size = this.getSizeForFlow(cellResults.get("flow"));
        double angle = this.getAngleForTangens(cellResults);

        return getScaledImage(arrow,size, angle);

    }

    private double getAngleForTangens(Map<String, Double> tangens) {

        double angle = Math.atan(tangens.get("tangens"));
        if(tangens.containsKey("signum") && tangens.get("signum") <=0){
            angle += Math.PI;
        }
        return angle;
    }

    private int getSizeForFlow(Double flow) {

        double scaleRange = this.maxSize - this.minSize;
        double valueRange = this.maxValue - this.minValue;
        double thisValue = flow - minValue;
        thisValue = thisValue / valueRange;
        thisValue*= scaleRange;
        return this.minSize + (int)thisValue;

    }

    private Map<String, Double> getSizeAndDirectionForCell(FluidCell cell) throws Exception{
        Map<String, Double> result = new HashMap<>();
        //Map<Coordinates.direction, Double> flow =this.invertParticleFow(cell.getParticleFLow());
        Map<Coordinates.direction, Double> flow =cell.getParticleFLow();
        Coordinates.direction xDirection = this.getXDirectionFromAxis();
        Coordinates.direction yDirection = this.getYDirectionFromAxis();

        Map<Coordinates.direction, Double> flowVector = new HashMap<>();

        double xFlow = (flow.get(Coordinates.direction.XPLUS1) - flow.get(Coordinates.direction.XMINUS1));
        double yFlow = (flow.get(Coordinates.direction.YPLUS1) - flow.get(Coordinates.direction.YMINUS1));
        double zFlow = (flow.get(Coordinates.direction.ZPLUS1) - flow.get(Coordinates.direction.ZMINUS1));
        double flowValue = Math.abs(xFlow) + Math.abs(yFlow) + Math.abs(zFlow);
        switch (axis.toLowerCase()){
            case "x": {
                flowValue -= Math.abs(xFlow);
                break;
            }
            case "y":{
                flowValue-= Math.abs(yFlow);
                break;
            }
            case "z":{
                flowValue-= Math.abs(zFlow);
                break;
            }
        }
        result.put("flow", flowValue);
        flowVector.put(Coordinates.direction.XPLUS1, xFlow);
        flowVector.put(Coordinates.direction.YPLUS1, yFlow);
        flowVector.put(Coordinates.direction.ZPLUS1, zFlow);
        flowVector.put(Coordinates.direction.XMINUS1, -xFlow);
        flowVector.put(Coordinates.direction.YMINUS1, -yFlow);
        flowVector.put(Coordinates.direction.ZMINUS1, -zFlow);

        if(flowVector.get(xDirection) != 0) {
            double tangens = flowVector.get(yDirection) / flowVector.get(xDirection);
            /*
            double angle = Math.atan(tangens);
            angle /= Math.PI;
            if(Math.signum(flowVector.get(xDirection))<0){
                angle += Math.PI;
            }
            */
            result.put("tangens", tangens);



        } else {
            result.put("tangens", 9E14);

        }
        result.put("signum", Math.signum(flowVector.get(xDirection)));
        return result;
    }

    private Map<Coordinates.direction, Double> invertParticleFow(Map<Coordinates.direction, Double> particleFLow) {
        Map<Coordinates.direction, Double> result = new HashMap<>();
        for (Coordinates.direction key: particleFLow.keySet()){
            result.put(key, -particleFLow.get(key));

        }
        return result;
    }


    private Coordinates.direction getXDirectionFromAxis() {
        switch(this.axis.toLowerCase()){
            case "x": return Coordinates.direction.ZPLUS1;
            case "y": return Coordinates.direction.XPLUS1;
            case "z": return Coordinates.direction.XPLUS1;
            default: return null;
        }
    }

    private Coordinates.direction getYDirectionFromAxis() {
        switch(this.axis.toLowerCase()){
            case "x": return Coordinates.direction.YMINUS1;
            case "y": return Coordinates.direction.ZPLUS1;
            case "z": return Coordinates.direction.YPLUS1;
            default: return null;
        }
    }


    private Image getScaledImage(Image srcImg, int d, double angle){
        BufferedImage resizedImg = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0,d, d, null);
        AffineTransform a = AffineTransform.getRotateInstance(angle, (double)d/2.0, (double)d/2);
        g2.dispose();
        double maximalDiagonalValue = d*1.5;
        int newSize = (int)maximalDiagonalValue+1;
        BufferedImage turnedImage = new BufferedImage(newSize,newSize, BufferedImage.TYPE_INT_ARGB);
        g2 = turnedImage.createGraphics();
        g2.setTransform(a);
        g2.drawImage(resizedImg, 0,0,null);
        g2.dispose();

        return turnedImage;
    }
}
