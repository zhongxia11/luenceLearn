package nearRealTime;

import com.google.common.collect.Maps;
import com.lucene.Doc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 *近实时
 * */
public class Searcher {

    static IndexSearcher is = null;
    final static String INDEX_DIR = "D:\\lucene\\dataindex";
    static Directory ramDirectoryA;
    static IndexReader fsReader = null;
    static MultiReader multiReader = null;

    static int addCount = 0;
    static IndexReader ramReaderB;

    static Lock merge_ramIndexB_lock = new ReentrantLock();
    static Directory fsDirectory;

    static {

        try {
            // 得到读取索引文件的路径
            fsDirectory = FSDirectory.open(Paths.get(INDEX_DIR));
            // 通过dir得到的路径下的所有的文件
            Long old = System.currentTimeMillis();
            try{
                fsReader = DirectoryReader.open(fsDirectory);
            }catch(IndexNotFoundException ex){
                System.err.println("初始fsReader不存在!!");

            }
            ramDirectoryA = new RAMDirectory();

            if(fsReader!=null){
                System.out.println("耗时1:"+(System.currentTimeMillis()-old));
                // 通过dir得到的路径下的所有的文件
                //IndexReader ramReader = DirectoryReader.open(ramDirectory);
                multiReader = new MultiReader(fsReader);
                // 建立索引查询器
                Long old2 = System.currentTimeMillis();
                is = new IndexSearcher(multiReader);
                System.out.println("耗时2:"+(System.currentTimeMillis()-old2));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Map<String,String> search(String q) throws Exception {

        Map<String,String> result = Maps.newHashMap();
        if(is==null){
            return result;
        }
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
        TopDocs hits = is.search(query, 10);
        // 计算索引结束时间
        long end = System.currentTimeMillis();
        //System.out.println(hits.totalHits);
        System.out.println("匹配 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");
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
            result.put(doc.get("docno"),doc.get("contenttitle"));
        }
        // 关闭reader
        //reader.close();
        return result;
    }

    /**
     * 获取文档，文档里再设置每个字段
     * @param xml
     * @return document
     */
    public static  Document getDocument(String xml) throws Exception {
        long old = System.currentTimeMillis();
        //xml = xml.replaceAll("&","&amp;");
        Document doc = new Document();
        try{
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            // 通过document对象获取根节点bookstore
            Element root = document.getRootElement();
            //把设置好的索引加到Document里，以便在确定被索引文档
            doc.add(new StringField("docno",root.element("docno").getStringValue(), Field.Store.YES));
            doc.add(new TextField("content", root.element("content").getStringValue(),Field.Store.NO));
            //Field.Store.YES：把文件名存索引文件里，为NO就说明不需要加到索引文件里去
            doc.add(new TextField("contenttitle",root.element("contenttitle").getStringValue(), Field.Store.YES));
            //把完整路径存在索引文件里
            doc.add(new TextField("url", root.element("url").getStringValue(), Field.Store.YES));
            //System.out.println("耗时:"+(System.currentTimeMillis()-old));
        }catch (Exception e){
            e.printStackTrace();
            throw  e;
        }
        return doc;
    }


    public static  Document getDocument(Doc doc2) throws Exception {
        long old = System.currentTimeMillis();
        //xml = xml.replaceAll("&","&amp;");
        Document doc = new Document();
        try{
            doc.add(new StringField("docno",doc2.getDocno(), Field.Store.YES));
            doc.add(new TextField("content", doc2.getContent(),Field.Store.NO));
            //Field.Store.YES：把文件名存索引文件里，为NO就说明不需要加到索引文件里去
            doc.add(new TextField("contenttitle",doc2.getContenttitle(), Field.Store.YES));
            //把完整路径存在索引文件里
            doc.add(new TextField("url",doc2.getUrl(), Field.Store.YES));
            //System.out.println("耗时:"+(System.currentTimeMillis()-old));
        }catch (Exception e){
            e.printStackTrace();
            throw  e;
        }
        return doc;
    }


    public static void add(String dataDir) throws Exception {

            String start_s = "<doc>";
            String end_s = "</doc>";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataDir),"gbk"));
            String s;
            StringBuilder buffer = new StringBuilder();
            while((s=reader.readLine())!=null){
                if(s.equals(start_s)){
                    buffer = new StringBuilder();
                }
                buffer.append(s);
                if(s.equals(end_s)){
                    Document document = getDocument(buffer.toString());
                    addDocument(document);
                }
            }
            System.out.println("add complete!!");
            addCount++;
    }


    public static boolean addIndex(Doc doc){
        try {
            Document document = getDocument(doc);
            addDocument(document);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static void addDocument(Document document) throws Exception {
        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(ramDirectoryA,iwConfig);
        writer.addDocument(document);
        writer.commit();
        writer.flush();
        writer.close();

        if(addCount>3){
            Directory ramDirectoryB = ramDirectoryA;
            ramDirectoryA = new RAMDirectory();
            ramReaderB = DirectoryReader.open(ramDirectoryB);
            if(fsReader!=null){
                multiReader = new MultiReader(fsReader,ramReaderB);
            }else{
                multiReader = new MultiReader(ramReaderB);
            }
            is = new IndexSearcher(multiReader);
            addCount = 0;
            Thread merge_thread = new Thread(() -> {
                try {
                    Thread.sleep(200);
                    Analyzer analyzer2 = new IKAnalyzer();
                    IndexWriterConfig iwConfig2 = new IndexWriterConfig(analyzer2);
                    IndexWriter writer2 = new IndexWriter(fsDirectory,iwConfig2);
                    writer2.addIndexes(ramDirectoryB);
                    writer2.commit();
                    writer2.close();
                    fsReader = DirectoryReader.open(fsDirectory);
                    merge_ramIndexB_lock.lock();
                    if(ramDirectoryA.listAll().length!=0){
                        IndexReader ramReaderA = DirectoryReader.open(ramDirectoryA);
                        multiReader = new MultiReader(fsReader,ramReaderA);
                    }else{
                        multiReader = new MultiReader(fsReader);
                    }
                    is = new IndexSearcher(multiReader);
                    ramReaderB.close();
                    ramReaderB = null;
                    merge_ramIndexB_lock.unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            merge_thread.start();
        }else{
            IndexReader ramReader = DirectoryReader.open(ramDirectoryA);
            merge_ramIndexB_lock.lock();
            if(ramReaderB!=null){
                if(fsReader!=null){
                    multiReader = new MultiReader(fsReader,ramReader,ramReaderB);
                }else{
                    multiReader = new MultiReader(ramReader,ramReaderB);
                }
            }else{
                if(fsReader!=null){
                    multiReader = new MultiReader(fsReader,ramReader);
                }else{
                    multiReader = new MultiReader(ramReader);
                }
            }
            merge_ramIndexB_lock.unlock();
            is = new IndexSearcher(multiReader);
            addCount++;
        }
    }


    public static void main(String[] args) {

        int count = 1;
        Thread[] threads = new Thread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        //我们要搜索的内容
                        String q = "刘德华";
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

        Thread writeThread = new Thread(() -> {
            String dataDir = "d:\\lucene_add.txt";
            try {
                while(true){
                    Thread.sleep(100);
                    System.out.println("开始写入!!");
                    add(dataDir);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //writeThread.start();

    }


}
