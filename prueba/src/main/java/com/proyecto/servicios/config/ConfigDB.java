package com.proyecto.servicios.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.proyecto.servicios.repositorys.sf",
                "com.proyecto.servicios.repositorys.gestopago"
        },
        transactionManagerRef = "sfTransactionManager",
        entityManagerFactoryRef = "sfEntityManagerFactory"
)
public class ConfigDB {
    @Autowired
    private Environment env;

    @Bean(name="sfDatasource")
    public DataSource sfDatasource(){
        HikariConfig config = new HikariConfig();
        String jdbcUrl = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        String driverClass = env.getProperty("spring.datasource.driver-class-name");

        // Soporte para DATABASE_URL o postgres:// en Render / plataformas en la nube
        String rawUrl = (jdbcUrl != null && !jdbcUrl.isBlank()) ? jdbcUrl : env.getProperty("DATABASE_URL");
        if (rawUrl != null && !rawUrl.isBlank()) {
            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                try {
                    java.net.URI uri = new java.net.URI(rawUrl.replace("postgres://", "postgresql://"));
                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath();
                    jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                    if (uri.getUserInfo() != null) {
                        String[] userParts = uri.getUserInfo().split(":");
                        if (userParts.length > 0 && (username == null || username.isBlank() || "postgres".equals(username))) {
                            username = userParts[0];
                        }
                        if (userParts.length > 1 && (password == null || password.isBlank() || "1234".equals(password))) {
                            password = userParts[1];
                        }
                    }
                } catch (Exception ex) {
                    log.warn("No se pudo parsear URI de base de datos ({}), usando fallback: {}", rawUrl, ex.getMessage());
                    jdbcUrl = rawUrl.startsWith("jdbc:") ? rawUrl : "jdbc:" + rawUrl;
                }
            } else if (rawUrl.startsWith("jdbc:")) {
                jdbcUrl = rawUrl;
            }
        }

        try {
            config.setJdbcUrl(jdbcUrl);
            config.setPassword(password);
            config.setUsername(username);
            if (driverClass != null && !driverClass.isBlank()) {
                config.setDriverClassName(driverClass);
            }
            config.setMaximumPoolSize(10);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(3000);
            config.setValidationTimeout(3000);
            config.setMinimumIdle(2);
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("sfDatasource");

            return new HikariDataSource(config);
        } catch (Exception e) {
            log.warn("No fue posible conectar con PostgreSQL primario ({}), iniciando fallback en memoria PostgreSQL: {}", jdbcUrl, e.getMessage());
            HikariConfig fallbackConfig = new HikariConfig();
            fallbackConfig.setJdbcUrl("jdbc:h2:mem:gestopago_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
            fallbackConfig.setUsername("sa");
            fallbackConfig.setPassword("");
            fallbackConfig.setDriverClassName("org.h2.Driver");
            fallbackConfig.setPoolName("sfDatasourceFallback");
            return new HikariDataSource(fallbackConfig);
        }
    }

    @Bean(name="sfEntityManagerFactory")
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean sfEntityManagerFactory(){
        LocalContainerEntityManagerFactoryBean em= new LocalContainerEntityManagerFactoryBean();
        try{
          em.setDataSource(sfDatasource());
          em.setPackagesToScan(
                  "com.proyecto.servicios.entity.sf",
                  "com.proyecto.servicios.entity.gestopago"
          );
          em.setPersistenceUnitName("sfDatasource");
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
          Map<String, Object> properties=new HashMap<>();
          properties.put("hibernate.hbm2ddl.auto", "none");
          properties.put("hibernate.show-sql", false);
          String dialect = env.getProperty("spring.jpa.properties.hibernate.dialect");
          if (dialect != null && !dialect.isBlank()) {
              properties.put("hibernate.dialect", dialect);
          }
          properties.put("jakarta.persistence.query.timeout", 600000);


        } catch (Exception e) {
            log.error("Ha ocurrido un error en la conexion a base de datos, a causa de:",e);
            return null;

        }
        return em;
    }
 @Bean(name="sfTransactionManager")
 public PlatformTransactionManager sfTransactionManager(@Qualifier("sfEntityManagerFactory") EntityManagerFactory sfEntityManagerFactory){
        return new JpaTransactionManager(sfEntityManagerFactory);

 }

}
