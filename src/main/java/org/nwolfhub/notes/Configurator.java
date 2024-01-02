package org.nwolfhub.notes;

import org.nwolfhub.notes.api.legacy.NotesController;
import org.nwolfhub.notes.database.legacy.HibernateController;
import org.nwolfhub.notes.database.legacy.UserDao;
import org.nwolfhub.utils.Utils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "org.nwolfhub.utils.Configurator")
@EnableWebSecurity
@EnableJpaRepositories
public class Configurator {
    private static final org.nwolfhub.utils.Configurator configurator = new org.nwolfhub.utils.Configurator(new File("notes.cfg"), getDemoCfg());
    public static String getDemoCfg() {
        String text = """
                    cleanup_rate=12
                    db_url=127.0.0.1
                    db_port=5432
                    db_name=notes
                    db_username=user
                    db_password=password
                    use_redis=true
                    redis_db_id=1
                    redis_url=127.0.0.1
                    redis_port=6379
                    redis_user=default
                    redis_password=password
                    users_dir=users""";
        return text;
    }

    @Bean
    @Primary
    public static Properties getHibernateProps() {
        Properties prop = new Properties();
        prop.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        prop.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        prop.put("hibernate.connection.url", Utils.buildConnectionString(configurator.getValue("db_url"), Integer.valueOf(configurator.getValue("db_port")), configurator.getValue("db_name")));
        prop.put("hibernate.connection.username", configurator.getValue("db_username"));
        prop.put("hibernate.connection.password", configurator.getValue("db_password"));
        prop.put("hibernate.current_session_context_class", "thread");
        prop.put("hibernate.connection.CharSet", "utf8");
        prop.put("hibernate.hbm2ddl.auto", "update");
        prop.put("hibernate.connection.characterEncoding", "utf8");
        prop.put("hibernate.connection.useUnicode", true);
        prop.put("hibernate.connection.pool_size", 100);
        return prop;
    }

    @Bean(name = "hibernateController")
    @Primary
    public static HibernateController getHibernateController() {
        return new HibernateController(getHibernateProps());
    }

    @Bean(name = "userDao")
    @Primary
    public static UserDao getDao() {
        return new UserDao(getHibernateController());
    }

    public static String getEntry(String key) {
        return configurator.getValue(key);
    }

    @Bean(name = "privilegesConfigurator")
    @Primary
    public static org.nwolfhub.utils.Configurator privilegesConfigurator() {
        org.nwolfhub.utils.Configurator privilegeConfig = new org.nwolfhub.utils.Configurator(new File("privileges.cfg"), """
        active=false
        host=http://your-donation-server.com
        privilege_default=10
        privilege_premium=40
        """);
        NotesController.used=Boolean.parseBoolean(privilegeConfig.getValue("used"));
        return privilegeConfig;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("org.nwolfhub.notes.database");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }
    //todo: migrate configurator to spring application.properties
    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(Utils.buildConnectionString(configurator.getValue("db_url"), Integer.valueOf(configurator.getValue("db_port")), configurator.getValue("db_name")));
        dataSource.setUsername(configurator.getValue("db_username"));
        dataSource.setPassword(configurator.getValue("db_password"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        return properties;
    }
}
