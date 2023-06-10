package org.nwolfhub.notes.database;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.module.Configuration;
import java.util.Properties;

@Component
public class HibernateController {
    private SessionFactory sessionFactory;

    @Autowired
    private Properties properties;

    public HibernateController() {}
}