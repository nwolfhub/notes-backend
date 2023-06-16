package org.nwolfhub.notes;

import org.nwolfhub.notes.database.HibernateController;
import org.nwolfhub.utils.Utils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
public class Configurator {
    private static final org.nwolfhub.utils.Configurator configurator = new org.nwolfhub.utils.Configurator(false, new File("projects.cfg"));;
    public static File createDemoConfig() throws IOException {
        File configFile = new File("notes.cfg");
        if (!configFile.exists()) {
            configFile.createNewFile();
            FileOutputStream out = new FileOutputStream(configFile);
            String text = """
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
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush(); out.close();
        }
        return configFile;
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

    public static String getEntry(String key) {
        return configurator.getValue(key);
    }
}
