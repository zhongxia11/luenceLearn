import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Test {

    public static void main(String[] args) throws InterruptedException {

        List<String> list = new CopyOnWriteArrayList<String>();

        list.add("a");
        list.add("b");
        list.add("c");


        Thread thread = new Thread(() -> {
            try {
                int count = list.size();
                System.out.println("count:"+count);
                Thread.sleep(2000);
                System.out.println(list.get(count-1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
        Thread.sleep(1000);
        list.remove(0);
    }




}
