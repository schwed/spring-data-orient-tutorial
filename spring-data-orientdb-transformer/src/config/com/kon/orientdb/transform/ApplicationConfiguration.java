package com.kon.orientdb.transform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by kshevchuk on 10/1/2015.
 */

@Configuration
@ComponentScan(basePackageClasses = {Configs.class}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationConfiguration.class)})
@PropertySources(value = {@PropertySource("classpath:application.properties")})
public class ApplicationConfiguration {

    private static final Logger logger = LogManager.getLogger();

    @Inject
    private Environment environment;

    @PostConstruct
    public void init() {
        logger.trace("-----------------------------------------------------------------------");
        logger.trace("application initialized.");

    }


}
