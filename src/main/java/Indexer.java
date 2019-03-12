import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Indexer {

    static String indexDir = "D:\\lucene\\dataindex";
    static Queue<Document> docuQueue = new ConcurrentLinkedQueue<>();
    static Queue<String> xmlQeue = new ConcurrentLinkedQueue<>();
        // 写索引实例
        private IndexWriter writer;

        /**
         * 构造方法 实例化IndexWriter
         *
         * @param indexDir
         * @throws IOException
         */
        public Indexer(String indexDir) throws IOException {
            //得到索引所在目录的路径
            Directory directory = FSDirectory.open(Paths.get(indexDir));
            // 标准分词器
            Analyzer analyzer = new IKAnalyzer();
            //保存用于创建IndexWriter的所有配置。
            IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
            //实例化IndexWriter
            writer = new IndexWriter(directory, iwConfig);
        }

        /**
         * 关闭写索引
         *
         * @throws Exception
         * @return 索引了多少个文件
         */
        public void close() throws IOException {
            writer.close();
        }

        public int init() throws InterruptedException, IOException {

            long deleteCount = writer.deleteAll();
            System.out.println("删除数量:"+deleteCount);
            int thread_count = Runtime.getRuntime().availableProcessors()*2;
            CountDownLatch latch = new CountDownLatch(thread_count);
            Thread[] thread = new Thread[thread_count];
            for(int i =0;i<thread_count;i++){
                thread[i] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String xml = null;
                        while((xml=xmlQeue.poll())!=null){
                            try {
                                Document doc = getDocument(xml);
                                docuQueue.offer(doc);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        latch.countDown();
                    }
                });
            }
            for(int i =0;i<thread_count;i++){
                thread[i].start();
            }
            latch.await();
            System.out.println("开始写索引!!");
            for(Object doc:docuQueue.toArray()){
                writer.addDocument((Document)doc);
            }

            writer.commit();
            writer.flush();
            int total = writer.numDocs();
            writer.close();
            return total;
        }


        public void add(String dataDir) throws Exception {

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
                    writer.addDocument(document);
                    writer.commit();
                    writer.flush();
                    writer.close();
                }
            }
            System.out.println("add complete!!");
        }


        public void read(String dataDir) throws Exception {

            String start_s = "<doc>";
            String end_s = "</doc>";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataDir),"utf-8"));
            String s;
            StringBuilder buffer = new StringBuilder();
            while((s=reader.readLine())!=null){
                if(s.equals(start_s)){
                    buffer = new StringBuilder();
                }
                buffer.append(s);
                if(s.equals(end_s)){
                    xmlQeue.offer(buffer.toString());
                }
            }
            System.out.println("文件读取完毕!!");
        }

        public static void convert(String dataDir) throws Exception {
            BufferedWriter writer = new BufferedWriter(new FileWriter("d:\\news_tensite_xml3.dat"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataDir),"utf-8"));
            String s;
            while((s=reader.readLine())!=null){
                writer.write(s.replaceAll("&","&amp;")+"\r\n");
            }
            writer.flush();
            writer.close();
            reader.close();
        }


        /**
         * 获取文档，文档里再设置每个字段
         *
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
                doc.add(new StringField("docno",root.element("docno").getStringValue(),Field.Store.YES));
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

        public static void delete() throws IOException {
            //得到索引所在目录的路径
            Directory directory = FSDirectory.open(Paths.get(indexDir));
            // 标准分词器
            Analyzer analyzer = new IKAnalyzer();
            //保存用于创建IndexWriter的所有配置。
            IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
            //实例化IndexWriter
            IndexWriter writer = new IndexWriter(directory, iwConfig);
            long result_c = writer.deleteDocuments(new Term("docno","5659c4279b7081ab-dd437189a1acd501"));
            System.out.println("result_c:"+result_c);
            writer.commit();
            writer.flush();
            writer.close();
        }

        public static void main(String[] args) throws Exception {

            /*while(true){
                String indexDir = "D:\\lucene\\dataindex";
                String dataDir = "d:\\lucene_add.txt";
                Indexer indexer = new Indexer(indexDir);
                indexer.add(dataDir);
                Thread.sleep(3000);
            }*/

            /*String indexDir = "D:\\lucene\\dataindex";
            String dataDir = "d:\\lucene_add.txt";
            Indexer indexer = new Indexer(indexDir);*/
            //indexer.add(dataDir);
            //delete();

            //索引指定的文档路径
           // String indexDir = "D:\\lucene\\dataindex";
            //String dataDir = "D:\\news_tensite_xml.dat";
            //convert(dataDir);
            ////被索引数据的路径


            /*String dataDir = "D:\\news_tensite_xml3.dat";

            Indexer indexer = null;
            int numIndexed = 0;
            //索引开始时间
            long start = System.currentTimeMillis();
            try {
                indexer = new Indexer(indexDir);
                indexer.read(dataDir);
                numIndexed = indexer.init();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    indexer.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            //索引结束时间
            long end = System.currentTimeMillis();
            System.out.println("索引：" + numIndexed + " 个文件 花费了" + (end - start) + " 毫秒");*/
        }




    }
