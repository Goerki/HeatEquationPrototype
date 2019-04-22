

import Heatequation.Space;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CalculationProgressWindow extends JDialog {
    private JPanel contentPane;
    private JProgressBar progressBar;
    private JTextArea remainingTimeTextField;
    private JTextArea elapsedTimeTextField;
    private JTextPane calculatingTheResultsWillTextPane;
    private JPanel calculationPanel;
    Space space;
    long timeElapsed;
    long startingTime;



    public CalculationProgressWindow(Space space) {
        setContentPane(contentPane);
        setModal(true);
        timeElapsed = 0;

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        this.space = space;

    }

    private void calculateElapsedTime(){
        this.timeElapsed = (System.currentTimeMillis()-startingTime)/1000;
     }


    public boolean calculate() {
        //space.startCalculation();
        startingTime = System.currentTimeMillis();

         System.out.print("start calculation " + space.startCalculation());
        while (!space.calculationReady()) {
            this.calculateElapsedTime();
            this.progressBar.setValue((int)space.getPercentageOfStatus());
            this.showElapsedTime(this.getSecondsAsString((int) this.timeElapsed));
            this.showRemainingTime(this.calculateRemainingTime((int) space.getPercentageOfStatus()));

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private String calculateRemainingTime(int percentage){
        if (percentage == 0){
            return "calculating";
        }
        double time = (double) this.timeElapsed / (double) percentage*100.0 - (double) timeElapsed;
        return this.getSecondsAsString((int)time);
    }

    private String getSecondsAsString(int time){
        long minutes =TimeUnit.SECONDS.toMinutes(time);
        long hours = TimeUnit.SECONDS.toHours(time);
        long seconds = TimeUnit.SECONDS.toSeconds(time);

        String result ="";
        if (hours > 0){
            result += (int) hours + " hours, ";
            minutes -= hours*60;
            seconds -= hours*3600;
        }
        if (minutes >0){
            result += (int) minutes + " minutes, ";
            seconds -= minutes*60;
        }
        result += (int) seconds + " seconds";
        return  result;
    }



    private void showRemainingTime(String time) {
        this.remainingTimeTextField.setText(time);

    }


    private void showElapsedTime(String time) {
        this.elapsedTimeTextField.setText(time);

    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    private int startCalculation() {
        return space.startCalculation();


    }
}


