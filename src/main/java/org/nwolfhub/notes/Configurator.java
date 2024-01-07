package org.nwolfhub.notes;
import org.nwolfhub.notes.auth.v1.UserDetailsProvider;
import org.nwolfhub.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

@Configuration
@EnableWebSecurity
@EnableJpaRepositories
public class Configurator {
    @Autowired
    UserDetailsProvider provider;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(SessionManagementConfigurer::disable)
                .httpBasic(basic -> {})
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/v1/notes/*")
                        .authenticated())
                .logout(LogoutConfigurer::permitAll);
        return http.build();
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

    //|-----------------------------------------------------------------|
    //|CODE BELOW THIS LINE IS DEPRECATED AND WILL BE REMOVED IN A WHILE|
    //|-----------------------------------------------------------------|


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
}
