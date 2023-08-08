package org.nwolfhub.notes;

import org.nwolfhub.notes.api.NotesController;
import org.nwolfhub.notes.database.HibernateController;
import org.nwolfhub.notes.database.UserDao;
import org.nwolfhub.utils.Utils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "org.nwolfhub.utils.Configurator")
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
        prop.put("hibernate.hbm2ddl.auto", "validate");
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
}
