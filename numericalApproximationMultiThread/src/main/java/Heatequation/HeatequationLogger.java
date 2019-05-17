package Heatequation;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.System.Logger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HeatequationLogger {
    FileWriter fileWriter;
    BufferedWriter writer;
    public enum LogLevel{
        DEBUG, ERROR, INFO, SYTEMOFEQUATIONS
    }
    List<LogLevel> loglevel;

    public HeatequationLogger(String logFile) {
        try {
            this.fileWriter = new FileWriter(new File(logFile));
            this.writer = new BufferedWriter(this.fileWriter);
            this.loglevel = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLoglevel(List<LogLevel> loglevel) {
        this.loglevel = loglevel;
    }

    public void addToLoglevel(LogLevel level){
        this.loglevel.add(level);
    }

    public void removeLoglevel(LogLevel level){
        this.loglevel.remove(level);
    }

    private String getTimestamp(){
            long now = System.currentTimeMillis();
            Timestamp ts = new Timestamp(now);
            return ts.toString();
        }


        public void logMessage(LogLevel level, String message) {
            if (this.loglevel.contains(level)) {
                StringBuilder builder = new StringBuilder("\n" + this.getTimestamp());
                builder.append(" " + level + ": " + message);
                try {
                    this.writer.write(builder.toString());
                    this.writer.flush();
                    System.out.print(builder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }

        public boolean logLevelEnabled(LogLevel level){
            return this.loglevel.contains(level);
        }




}
