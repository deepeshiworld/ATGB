package com.flock.atgb.handler;

import com.flock.atgb.db.MongoDBManager;
import com.flock.atgb.dto.MongoDBConfig;
import com.flock.atgb.util.FlockConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by B0095829 on 4/1/17.
 */
@Configuration
public class FlockConfig {


    @Bean(name = "flockDBManager")
    public MongoDBManager mongoPhotoStatsDBManager() {
        return new MongoDBManager(getMongoDBConfig(FlockConstants.FLOCK_DB));
    }

    @Bean
    public MongoDBConfig getMongoDBConfig(String dbName) {
        MongoDBConfig config = new MongoDBConfig();
        config.setMongoDBName(dbName);
        config.setMongodbHost("127.0.0.1");
        config.setMongodbPort(27017);
        config.setMongodbThreadsAllowedToBlock(50);
        config.setMongodbConnectionsPerHost(500);
        return config;
    }

}
