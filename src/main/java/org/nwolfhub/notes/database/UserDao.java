package org.nwolfhub.notes.database;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.nwolfhub.notes.model.User;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class UserDao {
    private HibernateController controller;

    public UserDao(HibernateController controller) {
        this.controller = controller;
    }
    public User getUser(Integer id) {
        Session session = controller.getSessionFactory().openSession();
        User user = session.get(User.class, id);
        session.close();
        return user;
    }
    public void setObject(Object o) {
        Session session = controller.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(o);
        transaction.commit();
        session.close();
    }

    public List<User> getUsers() {
        Session session = controller.getSessionFactory().openSession();
        List<User> toReturn = session.createQuery("from User").stream().toList();
        session.close();
        return toReturn;
    }

    public UserDao(AnnotationConfigApplicationContext context) {
        controller = context.getBean(HibernateController.class);
    }
}
