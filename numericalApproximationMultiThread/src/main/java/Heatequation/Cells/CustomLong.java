package Heatequation.Cells;

public class CustomLong {
    protected long value;
    //protected int exponent;
    protected int resolution;



    public CustomLong(double value, int resolution){
        this.resolution = resolution;
        int exponent = this.getCommaPositionFromDouble(value);
        double longValue;
        if (exponent < 1){
            longValue = value*this.getExponentForComma(-exponent);
        } else {

            longValue = value/this.getExponentForComma(exponent);

        }

        longValue *=getExponentForComma(exponent + resolution);

        this.value = (long) longValue;
        System.out.println("double: " + value + " longValue: " + longValue + " long: " + this.value + " with comma position " + exponent);
    }

    public void add(CustomLong secondValue){
        if (secondValue.resolution == this.resolution){
            this.value += secondValue.value;
            return;
        } else {
            long adaptedValueOfSecondLong = secondValue.value *= this.getExponentForComma(this.resolution - secondValue.resolution);
            this.value += adaptedValueOfSecondLong;
            return;
        }
    }

    private static double getExponentForComma(int commaPosition) {
        if (commaPosition == 0){
            return 1.0;
        } else if (commaPosition == 1){
            return  10;
        } else if (commaPosition > 1){
            double result = 1;
            for (int i=0; i<commaPosition; i++) {
                result *= 10;
            }
            return result;
        } else {
            double result = 1;
            for (int i=0; i>commaPosition; i--) {
                result /= 10;
            }
            return result;
        }
    }

    private int getCommaPositionFromDouble(double value) {
        if (value == 0.0){
            return 0;
        }
        if (value >= 1) {
            int factor = 0;
            double limit = 10;
            while (value > limit) {
                limit*= 10;
                factor++;
            }
            return factor;
        } else {
            int factor = -1;
            double limit = 0.1;
            while (value < limit) {
                factor--;
                limit/= 10;
            }
            return factor;
        }
    }

    private int getExponentOfLong(long value) {
        if (value == 0.0){
            return 0;
        }
        if (value >= 1) {
            int factor = 0;
            double limit = 10;
            while (value > limit) {
                limit*= 10;
                factor++;
            }
            return factor;
        } else {
            int factor = -1;
            double limit = 0.1;
            while (value < limit) {
                factor--;
                limit/= 10;
            }
            return factor;
        }
    }


    @Override
    public String toString() {
        return String.valueOf(this.getAsDouble());
    }

    public double getAsDouble() {
        int size = this.resolution -this.getExponentOfLong(this.value);
        double factor = this.getExponentForComma(resolution);
        if (factor < 1){
            factor = 1/factor;
            System.out.println("kleiner 1 "+ factor);
            return (double) this.value *factor;
        } else {
            System.out.println("größer 1 " + factor);
            return (double) this.value /factor;
        }
    }

    static public CustomLong multiply(CustomLong long1, CustomLong long2, int resolutionOfResult){
        CustomLong smallNumber = new CustomLong(0,0);
        CustomLong bigNumber = new CustomLong(0,0);

        if (long1.resolution == long2.resolution){
            return CustomLong.multiplyWithSameResolution(long1, long2);
        } else if (long1.resolution < long2.resolution){
            smallNumber = long1;
            bigNumber = long2;
        } else{
            smallNumber = long2;
            bigNumber = long1;
        }
        int diff = bigNumber.resolution - smallNumber.resolution;
        smallNumber.value *= CustomLong.getExponentForComma(diff);
        smallNumber.resolution += diff;
        return CustomLong.multiplyWithSameResolution(smallNumber, bigNumber);


    }

    protected static CustomLong multiplyWithSameResolution(CustomLong long1, CustomLong long2) {
        return new CustomLong(long1.value*long2.value, long1.resolution);

    }
}
