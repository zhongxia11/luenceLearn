import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class TryLock {

    private static void tryLock() {
        try {
            //RandomAccessFile raf = new RandomAccessFile("D:\\lucene\\dataindex\\write.lock", "w");
            FileChannel cl = FileChannel.open(new File("D:\\lucene\\dataindex\\write.lock").toPath(), StandardOpenOption.WRITE);
            //FileChannel cl = raf.getChannel();
            FileLock fl = cl.tryLock(0,Integer.MAX_VALUE,false);
            if (fl != null) {
                System.out.println("获取锁...");
                Thread.sleep(1000 * 20);
            } else {
                System.out.println("锁被占用!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        tryLock();
    }


}
