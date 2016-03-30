package com.kon.orientdb.transform;

import com.kon.orientdb.transform.exception.TransformationException;
import com.kon.orientdb.transform.service.DirectoryTraversalService;
import com.kon.orientdb.transform.service.TransformationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;
import org.springframework.data.orient.object.repository.support.OrientObjectRepositoryFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by kshevchuk on 10/1/2015.
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {Configs.class})
@EnableOrientRepositories(basePackages = {"com.kpmg.dns.orientdb.transform.dataaccess.repository"}, repositoryFactoryBeanClass = OrientObjectRepositoryFactoryBean.class)
public class MainTransformer implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    Environment environment;

    @Autowired
    @Qualifier("applicationProperties")
    Properties properties;

    // input arguments
    private static String path;
    private static Boolean testMode = Boolean.FALSE;

    @Bean(name = "applicationProperties")
    public Properties getProperties() throws IOException {
        Properties properties = new Properties();
        Path reader = Paths.get(path);
        properties.load(Files.newInputStream(reader));
        return properties;
    }

    @Autowired
    OrientObjectDatabaseFactory factory;

    @Autowired
    TransformationService transformationService;

    @Autowired
    DirectoryTraversalService directoryTraversalService;

    public static void main(final String... args) {

        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        builder.listeners(new ApplicationListener<ApplicationStartedEvent>() {
            @Override
            public void onApplicationEvent(ApplicationStartedEvent applicationEvent) {
                if (args.length == 0) {
                    logger.trace("TO RUN TOOL PLEASE PROVIDE PROPERTIES FILE LOCATION AND/OR TEST MODE FLAG");
                    System.exit(-1);
                }
                path = args[0];

            }
        });
        builder.initializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            @Override
            public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
                // pass this flag to db configuration, if true then its a test mode, default is false
                if (args.length == 0) {
                    logger.trace("TO RUN TOOL PLEASE PROVIDE PROPERTIES FILE LOCATION AND/OR TEST MODE FLAG");
                    System.exit(-1);
                }
                for (String argument : args) {
                    logger.trace("ARGUMENT IN ARRAY: " + argument);
                }
                if (args.length > 1) {
                    testMode = Boolean.parseBoolean(args[1]);
                    logger.trace("test mode: " + testMode);
                }
                MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
                Map<String, Object> map = new HashMap<>();
                map.put("testMode", testMode);
                propertySources.addFirst(new MapPropertySource("TEST-MODE", map));

            }
        });
        builder.web(Boolean.FALSE);
        builder.sources(MainTransformer.class);
        builder.run(args);
    }

    @Override
    public void run(String... strings) throws Exception {
        String mode = testMode == Boolean.TRUE ? "IN TEST MODE WITH ROLLBACKS..." : "IN PERSISTENT MODE WITH COMMITS...";
        logger.trace("WILL RUN TRANSFORMATION UTILITY " + mode);

        // validate command line parameter - properties file
        if (strings.length == 0) {
            logger.trace("TO RUN TOOL PLEASE PROVIDE PROPERTIES FILE LOCATION AND/OR TEST MODE FLAG");
            System.exit(-1);
        }

        if (Files.notExists(Paths.get(strings[0]), LinkOption.NOFOLLOW_LINKS)) {
            throw new TransformationException("INVALID PROPERTIES FILE LOCATION.");
        }

        Path filePath = Paths.get(strings[0]);
        if (Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS)
                && !Files.isReadable(filePath)) {
            throw new TransformationException("INPUT FILE DOES NOT HAVE READ PERMISSION.");
        }

        List<String> filesList = new ArrayList<>();
        Path inputPath = Paths.get(properties.getProperty(Constants.ORIENT_DB_DNS_TRANSFORMATION_INPUT.toString()));
        if (Files.exists(inputPath)
                && Files.isDirectory(inputPath, LinkOption.NOFOLLOW_LINKS)
                && Files.isReadable(inputPath)) {

            // fetch list of files, throws TransformationException
            filesList.addAll(directoryTraversalService.traverseDirectory(inputPath));
        }

        try {
            factory.db().begin();
            for (String file : filesList) {
                Map<String, List<String>> map = transformationService.parse(file);
                if (transformationService.performTransformation(map) == Boolean.TRUE) {
                    if (testMode) {
                        logger.trace("WILL ROLLBACK TRANSFORMATIONS FOR FILE: " + file);
                        factory.db().rollback();
                    } else {
                        logger.trace("WILL COMMIT TRANSFORMATIONS FOR FILE: " + file);
                        factory.db().commit();
                    }
                }
            }
            logger.trace("DONE RUNNING TRANSFORMATION UTILITY.");
        } catch (Exception e) {
            logger.trace("EXCEPTION: " + e.getMessage());
            logger.trace("FAILED TO RUN TRANSFORMATIONS.");
            factory.db().rollback();
        } finally {
            factory.db().close();
        }
    }

}
