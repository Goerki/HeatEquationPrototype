package Cells;

import com.google.gson.JsonObject;

import java.awt.*;

public class Material {
    public String name;
    public Color color;
    public double alpha;
    public double viskosity;
    public String type;

    public Material(String name, String type, String color, double value){
        init(name, type, color, value);
    }

    public boolean isFluid(){
        return this.type.toLowerCase().contains("fluid");
    }

    public boolean isSolid(){
        return this.type.toLowerCase().contains("solid");
    }

    public Material(JsonObject json){
        if (json.has("alpha")) {
            init(json.get("name").getAsString(),
                    "solid",
                    json.get("color").getAsString(),
                    json.get("alpha").getAsDouble());
        }else {
            init(json.get("name").getAsString(),
                    "solid",
                    json.get("color").getAsString(),
                    json.get("viskosity").getAsDouble());
        }
    }

    private void init(String name, String type, String color, double value){
        this.name=name;
        this.type = type;
        if (this.type.toLowerCase().contains("fluid")){
            this.viskosity=value;
            this.alpha = 0;
        }
        if (this.type.toLowerCase().contains("solid")){
            this.alpha=value;
            this.viskosity = 0;
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

}
