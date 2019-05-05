package Heatequation.Cells;

import com.google.gson.JsonObject;

import java.awt.*;
import java.io.Serializable;

public class Material implements Serializable {
    public String name;
    public Color color;
    private double heatCapacity;
    private double heatConductivity;
    private double alpha;
    private double viskosity;
    private double nusselt;
    public String type;

    public Material(String name, String type, String color, double heatCapacity, double heatConvectivity, double viskosity, double nusselt){
        init(name, type, color, heatCapacity, heatConvectivity, viskosity, nusselt);
    }

    public boolean isFluid(){
        return this.type.toLowerCase().contains("fluid");
    }

    public boolean isSolid(){
        return this.type.toLowerCase().contains("solid");
    }

    public Material(JsonObject json){
        if (json.has("viskosity")) {
            init(json.get("name").getAsString(),
                    "fluid",
                    json.get("color").getAsString(),
                    json.get("heatCapacity").getAsDouble(),
                    json.get("heatConductivity").getAsDouble(),
                    json.get("viskosity").getAsDouble(),
                    json.get("nusselt").getAsDouble());
        }else {
            init(json.get("name").getAsString(),
                    "solid",
                    json.get("color").getAsString(),
                    json.get("heatCapacity").getAsDouble(),
                    json.get("heatConductivity").getAsDouble(),
                    0,0);
        }
    }

    public double getHeatCapacity(){
        return this.heatCapacity;
    }

    private void init(String name, String type, String color, double heatCapacity, double heatConductivity, double viskosity, double nusselt){
        this.name=name;
        this.type = type;
        if (this.type.toLowerCase().contains("fluid")){
            this.viskosity=viskosity;
            this.heatCapacity=heatCapacity;
            this.heatConductivity=heatConductivity;
            this.nusselt = nusselt;
            this.alpha = heatConductivity/heatCapacity;
        }
        if (this.type.toLowerCase().contains("solid")){
            this.viskosity=0;
            this.heatCapacity=heatCapacity;
            this.heatConductivity=heatConductivity;
            this.nusselt = 0;
            this.alpha = heatConductivity/heatCapacity;
        }
        try {
            setColorFromHexString(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setColorFromHexString(String hexCode) throws Exception{
        if (hexCode.startsWith("#")){
            hexCode = hexCode.substring(1, 6);
        }
        if (hexCode.length() !=6){
            throw new Exception("Could not select Color from hex string " + hexCode);
        }
        this.color =  new Color(
                Integer.valueOf(hexCode.substring(0, 2), 16),
                Integer.valueOf(hexCode.substring(2, 4), 16),
                Integer.valueOf(hexCode.substring(4, 6), 16));
    }

    public double getheatConductivity() {
        return heatConductivity;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getViskosity() {
        return viskosity;
    }

    public double getNusselt() {
        return nusselt;
    }
}
