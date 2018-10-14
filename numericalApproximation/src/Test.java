public class Test {

    public static void main(String[] args) {


        Space space = new Space(50);
        System.out.println("gubl \n");
        long beginTime = System.currentTimeMillis();
        System.out.println(space.toString());


       for (int i =0; i < 100000; i++){
//            System.out.println(i+" \n");
            space.calcSpace();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - beginTime;

        System.out.print("duration: "+ duration);
    }
}


