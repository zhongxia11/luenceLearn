package com.lucene;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class DocService {

    @Autowired
    private DocRepo docRepo;

    @PersistenceContext
    private EntityManager entityManager;
    //@PersistenceContext
    //private JpaTransactionManager jpaTransactionManager;
    //@Autowired
    //private SessionFactory sessionFactory;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    //@Transactional
    public void saveAll(List<Doc> lists){
        //entityManager.getTransaction().begin();
        //jpaTransactionManager.get
        //HibernateEntityManager hEntityManager = (HibernateEntityManager)entityManager;
        Session session = entityManagerFactory.unwrap(SessionFactory.class).openSession();
        session.getTransaction().begin();

        /*Session session = sessionFactory.openSession();
        session.beginTransaction();
        */
        for(Doc doc:lists){
            session.save(doc);
        }
        session.flush();
        session.getTransaction().commit();
        session.close();
    }







}
