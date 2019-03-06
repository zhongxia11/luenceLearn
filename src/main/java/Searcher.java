import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;
import java.io.IOException;
import java.nio.file.Paths;


/**
 *依靠task更新
 * */
public class Searcher {

    static IndexSearcher is = null;
    final static String INDEX_DIR = "D:\\lucene\\dataindex";

    static {

        try {
            // 得到读取索引文件的路径
            Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
            // 通过dir得到的路径下的所有的文件
            IndexReader reader = null;
            reader = DirectoryReader.open(dir);
            // 建立索引查询器
            is = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void search(String q) throws Exception {

        // 计算索引开始时间
        long start = System.currentTimeMillis();

        // 实例化分析器
        Analyzer analyzer = new IKAnalyzer(true);

        QueryParser parser = new QueryParser("content", analyzer);
        // 根据传进来的p查找
        Query query = parser.parse(q);

        // 开始查询
        /**
         * 第一个参数是通过传过来的参数来查找得到的query； 第二个参数是要出查询的行数
         */
        TopDocs hits = is.search(query, 1);
        // 计算索引结束时间
        long end = System.currentTimeMillis();
        System.out.println(hits.totalHits);
        //System.out.println("匹配 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");
        // 遍历hits.scoreDocs，得到scoreDoc
        /**
         * ScoreDoc:得分文档,即得到文档 scoreDocs:代表的是topDocs这个文档数组
         *
         * @throws Exception
         */
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            //System.out.println(scoreDoc.doc);
            Document doc = is.doc(scoreDoc.doc);
            //System.out.println(doc.get("contenttitle") + doc.get("url"));

        }
        // 关闭reader
        //reader.close();
    }

    public static void main(String[] args) {

        int count = 1;
        Thread[] threads = new Thread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        //String indexDir = "D:\\lucene\\dataindex";
                        //我们要搜索的内容
                        String q = "王静仪";
                        try {
                            search(q);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        for (int i = 0; i < count; i++) {
            threads[i].start();
        }

        Thread update_thread = new Thread(() -> {
            try {
                while(true){
                    Thread.sleep(1000*2);
                    Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
                    // 通过dir得到的路径下的所有的文件
                    IndexReader reader = null;
                    reader = DirectoryReader.open(dir);
                    // 建立索引查询器
                    IndexSearcher isNew = new IndexSearcher(reader);
                    IndexSearcher isOld = is;
                    is = isNew;
                    isOld.getIndexReader().close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        update_thread.start();

    }


}
