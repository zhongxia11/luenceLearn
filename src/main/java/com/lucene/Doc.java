package com.lucene;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Doc {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String url;
    private String docno;
    private String contenttitle;
    private String content;
    private Short inlucene;

    public Doc(){

    }

    public Doc(Integer id,String url, String docno, String contenttitle, String content, Short inlucene) {
        this.id = id;
        this.url = url;
        this.docno = docno;
        this.contenttitle = contenttitle;
        this.content = content;
        this.inlucene = inlucene;
    }

    public Doc(String url, String docno, String contenttitle, String content) {
        this.url = url;
        this.docno = docno;
        this.contenttitle = contenttitle;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDocno() {
        return docno;
    }

    public void setDocno(String docno) {
        this.docno = docno;
    }

    public String getContenttitle() {
        return contenttitle;
    }

    public void setContenttitle(String contenttitle) {
        this.contenttitle = contenttitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Short getInlucene() {
        return inlucene;
    }

    public void setInlucene(Short inlucene) {
        this.inlucene = inlucene;
    }
}
