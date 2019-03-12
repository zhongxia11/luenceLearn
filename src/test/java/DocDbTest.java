import com.google.gson.Gson;
import com.lucene.Doc;
import com.lucene.DocRepo;
import com.lucene.DocService;
import nearRealTime.Searcher;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.assertj.core.util.Lists;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import starter.Application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DocDbTest {

    @Autowired
    private DocService docService;

    @Autowired
    private DocRepo docRepo;

    @Test
    @Ignore
    public void addToDb() throws Exception {
        String dataDir = "d:\\news_tensite_xml3.dat";
        String start_s = "<doc>";
        String end_s = "</doc>";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataDir), "utf-8"));
        String s;
        StringBuilder buffer = new StringBuilder();
        int listBufferSize = 1000;
        List<Doc> lists = Lists.newArrayList();
        int count = 0;
        while ((s = reader.readLine()) != null) {
            if (s.equals(start_s)) {
                buffer = new StringBuilder();
            }
            buffer.append(s);
            if (s.equals(end_s)) {
                Document document = DocumentHelper.parseText(buffer.toString());
                Element root = document.getRootElement();
                Doc doc = new Doc(root.element("url").getStringValue(), root.element("docno").getStringValue(), root.element("contenttitle").getStringValue(), root.element("content").getStringValue());
                count++;
                lists.add(doc);
                if (count == listBufferSize) {
                    docService.saveAll(lists);
                    lists.clear();
                    count = 0;
                }
            }
        }
        if (lists.size() != 0) {
            docService.saveAll(lists);
        }
    }


    @Test
    @Ignore
    public void addToLucene() {

        //Optional<Doc> optional = docRepo.findById(80596);

        List<Doc> list = docRepo.findAllById(Lists.newArrayList(52641, 53506, 53520, 57329, 57224));
        for (Doc doc : list) {
            nearRealTime.Searcher.addIndex(doc);
        }
        /*if(optional.isPresent()){
            Doc doc = optional.get();
            nearRealTime.Searcher.addIndex(doc);
        }*/
        try {
            Searcher.search("杨幂");
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    @Test
    public void addByHttp() {
        List<Doc> list = docRepo.findAllById(Lists.newArrayList(52641, 53506, 53520, 57329, 57224));
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://localhost:9999/lucene/add");
        try {
            Gson gson = new Gson();
            for (Doc doc : list) {
                String json = gson.toJson(doc);
                System.out.println("json:"+json);
                post.setEntity(new StringEntity(json,"utf-8"));
                post.setHeader("Content-type","application/json");
                HttpResponse response = client.execute(post);
                HttpEntity resEntity = response.getEntity();
                System.out.println(EntityUtils.toString(resEntity, "utf-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
