package com.flock.atgb.dto;

public class MongoDBConfig {

    // mongo db properties
    private String mongodbHost;
    private int mongodbPort;
    private String mongodbHostSecondary;
    private int mongodbPortSecondary;
    private String mongodbHostArbiter;
    private int mongodbPortArbiter;
    private String mongoDBName;
    private String mongoDBPrefix;
    private Boolean mongodbLoggingEnabled;
    private boolean readPrimary;

    private int mongodbThreadsAllowedToBlock;
    private int mongodbConnectionsPerHost;

    public boolean isReadPrimary() {
        return readPrimary;
    }

    public void setReadPrimary(boolean readPrimary) {
        this.readPrimary = readPrimary;
    }

    public String getMongodbHostSecondary() {
        return mongodbHostSecondary;
    }

    public void setMongodbHostSecondary(String mongodbHostSecondary) {
        this.mongodbHostSecondary = mongodbHostSecondary;
    }

    public int getMongodbPortSecondary() {
        return mongodbPortSecondary;
    }

    public void setMongodbPortSecondary(int mongodbPortSecondary) {
        this.mongodbPortSecondary = mongodbPortSecondary;
    }

    public String getMongodbHostArbiter() {
        return mongodbHostArbiter;
    }

    public void setMongodbHostArbiter(String mongodbHostArbiter) {
        this.mongodbHostArbiter = mongodbHostArbiter;
    }

    public int getMongodbPortArbiter() {
        return mongodbPortArbiter;
    }

    public void setMongodbPortArbiter(int mongodbPortArbiter) {
        this.mongodbPortArbiter = mongodbPortArbiter;
    }

    public String getMongodbHost() {
        return mongodbHost;
    }

    public void setMongodbHost(String mongodbHost) {
        this.mongodbHost = mongodbHost;
    }

    public int getMongodbPort() {
        return mongodbPort;
    }

    public void setMongodbPort(int mongodbPort) {
        this.mongodbPort = mongodbPort;
    }

    public String getMongoDBPrefix() {
        return mongoDBPrefix;
    }

    public void setMongoDBPrefix(String mongoDBPrefix) {
        this.mongoDBPrefix = mongoDBPrefix;
    }

    public String getMongoDBName() {
        return mongoDBName;
    }

    public void setMongoDBName(String mongoDBName) {
        this.mongoDBName = mongoDBName;
    }

    public Boolean getMongodbLoggingEnabled() {
        return mongodbLoggingEnabled;
    }

    public void setMongodbLoggingEnabled(Boolean mongodbLoggingEnabled) {
        this.mongodbLoggingEnabled = mongodbLoggingEnabled;
    }

    public int getMongodbThreadsAllowedToBlock() {
        return mongodbThreadsAllowedToBlock;
    }

    public void setMongodbThreadsAllowedToBlock(int mongodbThreadsAllowedToBlock) {
        this.mongodbThreadsAllowedToBlock = mongodbThreadsAllowedToBlock;
    }

    public int getMongodbConnectionsPerHost() {
        return mongodbConnectionsPerHost;
    }

    public void setMongodbConnectionsPerHost(int mongodbConnectionsPerHost) {
        this.mongodbConnectionsPerHost = mongodbConnectionsPerHost;
    }
}
