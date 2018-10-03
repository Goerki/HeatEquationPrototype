
public class Cell {
    private long value;
    private long oldValue;

    Cell(long value){
        this.value = value;
        this.oldValue = value;
    }

    public long getValue(){
        return this.value;
    }

    public long getLastValue(){
        return this.oldValue;
    }

    public void setValue(long newValue){
        this.oldValue = this.value;
        this.value = newValue;
    }
}
