package org.entando.kubernetes.config;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import static java.lang.String.format;

@Configuration
public class LiquibaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(LiquibaseConfig.class);
    @Value("${spring.liquibase.change-log}")
    private String changelog;

    @Value("${spring.liquibase.lock.fallback.minutes}")
    private Integer lockFallbackMinutes;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        // Added a hook to check for locks before LiquiBase initialises.
        removeDBLock(dataSource);
        //
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changelog);
        liquibase.setDataSource(dataSource);
        return liquibase;
    }



    private void removeDBLock(DataSource dataSource) {

        //Timestamp, currently set to configured mins or older.

        final Timestamp lastDBLockTime = new Timestamp(System.currentTimeMillis() - (lockFallbackMinutes * 60 * 1000));

        final String query = format("DELETE FROM DATABASECHANGELOGLOCK WHERE LOCKGRANTED < timestamp '%s'", lastDBLockTime);
        logger.info("Cleaning lock statemant is : {} .",query);

        try (Statement stmt = dataSource.getConnection().createStatement()) {

            int updateCount = stmt.executeUpdate(query);
            if(updateCount>0){
                logger.error("Locks Removed Count: {} .",updateCount);
            }
        } catch (SQLException e) {
            logger.error("Error! Remove Change Lock threw and Exception. ",e);
        }
    }
}
