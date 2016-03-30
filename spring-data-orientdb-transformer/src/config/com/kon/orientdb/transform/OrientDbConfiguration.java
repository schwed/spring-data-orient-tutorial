package com.kon.orientdb.transform;

import com.kon.orientdb.transform.dataaccess.data.Attribute;
import com.kon.orientdb.transform.dataaccess.data.Feeds;
import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.orient.commons.core.OrientTransactionManager;
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;
import org.springframework.data.orient.object.OrientObjectTemplate;
import org.springframework.data.orient.object.repository.support.OrientObjectRepositoryFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * Created by kshevchuk on 10/2/2015.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableTransactionManagement
@EnableOrientRepositories(basePackages = {"com.kpmg.dns.orientdb.transform.dataaccess.repository"}, repositoryFactoryBeanClass = OrientObjectRepositoryFactoryBean.class)
public class OrientDbConfiguration {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    Environment environment;

    @Autowired
    @Qualifier("applicationProperties")
    Properties properties;

    @Bean
    public OrientGraphFactory orientGraphFactory() {
        OrientGraphFactory orientGraphFactory = new OrientGraphFactory(getUrl(), getUsername(), getPassword());
        orientGraphFactory.setupPool(getMinPoolSize(), getMaxPoolSize());
        return orientGraphFactory;
    }

    /**
     * Creates a new Transactional Graph using an existent database instance.
     */


    // CAN NOT USE IT HERE BECAUSE CAST ERROR BETWEEN OBJECT DATABASE AND GRAPH DATABASE OBJECTS
    /*
    @Bean
    public OrientGraph  orientGraph() {
        return orientGraphFactory().getTx();
    }
    */
    @Bean
    public OrientObjectDatabaseFactory factory() {
        OrientObjectDatabaseFactory factory = new OrientObjectDatabaseFactory();
        factory.setUrl(getUrl());
        factory.setUsername(getUsername());
        factory.setPassword(getPassword());
        factory.setMinPoolSize(getMinPoolSize());
        factory.setMaxPoolSize(getMaxPoolSize());
        return factory;
    }

    @Bean
    public OrientTransactionManager transactionManager() {
        return new OrientTransactionManager(factory());
    }

    @Bean
    public OrientObjectTemplate objectTemplate() {
        return new OrientObjectTemplate(factory());
    }

    @PostConstruct
    public void registerEntities() {
        OObjectDatabaseTx db = factory().db();
        OEntityManager em = db.getEntityManager();
        em.registerEntityClass(Attribute.class);
        em.registerEntityClass(Feeds.class);

    }


    private String getUrl() {
        return properties.getProperty(Constants.ORIENT_DB_URL.toString());
    }

    private String getUsername() {
        return properties.getProperty(Constants.ORIENT_DB_USERNAME.toString());
    }

    private String getPassword() {
        return properties.getProperty(Constants.ORIENT_DB_PASSWORD.toString());
    }

    private int getMinPoolSize() {
        return Integer.parseInt(properties.getProperty(Constants.ORIENT_DB_MIN_POOL_SIZE.toString()));
    }

    private int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty(Constants.ORIENT_DB_MAX_POOL_SIZE.toString()));
    }

}
