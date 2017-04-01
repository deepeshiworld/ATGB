package com.flock.atgb.db;

import com.flock.atgb.dto.MongoDBConfig;
import com.mongodb.*;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: bhuvangupta Date: 20/09/12 Time: 10:49 PM To change this
 * template use File | Settings | File Templates.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MongoDBManager {

    public static final String MONGO_ID = "_id";
    protected static final Logger logger = LoggerFactory.getLogger(MongoDBManager.class);
    private static final int DEFAULT_ALLOWED_THREADS_TO_BLOCK = 50;
    private static final int DEFAULT_CONNECTIONS_PER_HOST = 100;
    private Mongo mongo;
    private DB metadataDB;
    private Boolean isLoggingEnabled;

    private MongoDBConfig config;

    public MongoDBManager(MongoDBConfig config) {
        init(config);
    }

    public static ObjectId getObjectId(String oid) {
        byte[] byts = new BigInteger(oid, 16).toByteArray();
        ObjectId mongoId = new ObjectId(byts);
        return mongoId;
    }

    public static String getIdFromMongoOId(Object jsonObj) {
        if (jsonObj instanceof JSONObject) {
            JSONObject obj = (JSONObject) jsonObj;
            return (String) obj.get("$oid");
        }
        return jsonObj.toString();
    }

    public static void main(String[] args) {
        DBObject one = new BasicDBObject("categoryId", "1");
        DBObject two = getExpiryQuery("expiryTime", System.currentTimeMillis());
        // DBObject two = new BasicDBObject("expiryTime", new BasicDBObject("$not", new
        // BasicDBObject("$lt", 0)));

        DBObject f = new BasicDBObject();
        f.putAll(one);
        f.putAll(two);

        // BasicDBObject query = new BasicDBObject();
        // query.put("id", "1");
        //
        // BasicDBObject clause1 = new BasicDBObject("post_title", "1");
        // BasicDBObject clause2 = new BasicDBObject("post_description", "2");
        // BasicDBList clauses = new BasicDBList();
        // clauses.add(clause1);
        // clauses.add(clause2);
        //
        // query.put("$or", clauses);
        //
        // Map m = query.toMap();
        // if(m instanceof LinkedHashMap) {
        // System.out.println("LinkedHashMap");
        // }
        // else if(m instanceof HashMap) {
        // System.out.println("HashMap");
        // }
        // System.out.println(m);
        //
        // for(Object obj : m.values()) {
        // System.out.println(obj.getClass().getName());
        // }
        // // {$or:[{expiry:{$exists:false}},{expiry:{$exists:true, $gte:40}}]}
    }

    private static DBObject getExpiryQuery(String dateField, long fromTime) {
        DBObject finalQuery = new BasicDBObject();

        BasicDBList orQueries = new BasicDBList();
        orQueries.add(new BasicDBObject(dateField, new BasicDBObject("$not", new BasicDBObject("$lt", fromTime))));
        orQueries.add(new BasicDBObject(dateField, 0));

        finalQuery.put("$or", orQueries);

        return finalQuery;
    }

    public static boolean isValidObjectId(String s) {
        if (s == null) {
            return false;
        }
        int len = s.length();
        if (len != 24) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if ((c >= '0') && (c <= '9')) {
                continue;
            }
            if ((c >= 'a') && (c <= 'f')) {
                continue;
            }
            if ((c < 'A') || (c > 'F')) {
                return false;
            }
        }
        return true;
    }

    private void init(MongoDBConfig config) {

        this.config = config;
        if (config == null) {
            logger.error("not creating mongodb manager");
            return;
        }
        if (StringUtils.isBlank(config.getMongodbHost())) {
            logger.error("not creating mongodb manager for [{}]", config.getMongoDBName());
            return;
        }
        List<ServerAddress> seeds = new ArrayList<>();

        if (StringUtils.isBlank(config.getMongodbHost())) {
            logger.error("not creating mongodb manager for " + config.getMongoDBName());
            return;
        }

        ServerAddress primary = new ServerAddress(config.getMongodbHost(), config.getMongodbPort());
        seeds.add(primary);

        // Adding Comma separated Secondary Members
        if (StringUtils.isNotBlank(config.getMongodbHostSecondary())) {

            String[] secHostArr = config.getMongodbHostSecondary().split(",");
            for (String secHost : secHostArr) {
                ServerAddress slave = new ServerAddress(secHost, config.getMongodbPortSecondary());
                seeds.add(slave);
            }

        }

        // Adding Comma separated Arbiter Members
        if (StringUtils.isNotBlank(config.getMongodbHostArbiter())) {
            String[] arbHostArr = config.getMongodbHostArbiter().split(",");
            for (String arbHost : arbHostArr) {
                ServerAddress arbiter = new ServerAddress(arbHost, config.getMongodbPortArbiter());
                seeds.add(arbiter);
            }

        }

        Builder builder = MongoClientOptions.builder();

        int mongodbThreadsAllowedToBlock = config.getMongodbThreadsAllowedToBlock();
        if (mongodbThreadsAllowedToBlock <= 0) {
            mongodbThreadsAllowedToBlock = DEFAULT_ALLOWED_THREADS_TO_BLOCK;
        }
        int mongodbConnectionsPerHost = config.getMongodbConnectionsPerHost();
        if (mongodbConnectionsPerHost <= 0) {
            mongodbConnectionsPerHost = DEFAULT_CONNECTIONS_PER_HOST;
        }
        builder.threadsAllowedToBlockForConnectionMultiplier(mongodbThreadsAllowedToBlock).connectionsPerHost(mongodbConnectionsPerHost);

        if (config.getMongodbHostSecondary() != null && !config.isReadPrimary()) { // secondary reads is turned off
            builder.readPreference(ReadPreference.secondaryPreferred());
        }
        builder.description(config.getMongoDBName());
        builder.alwaysUseMBeans(true);
        MongoClientOptions options = builder.build();

        mongo = new MongoClient(seeds, options);

        if (StringUtils.isNotBlank(config.getMongodbHostSecondary())) {
            mongo.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        }

        metadataDB = mongo.getDB(config.getMongoDBName());
        isLoggingEnabled = false;
        if (config.getMongodbLoggingEnabled() != null && config.getMongodbLoggingEnabled())
            isLoggingEnabled = true;
    }

    DB getDB() {
        return metadataDB;
    }

    public DBCollection createCollection(String collectionName) {
        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            collection = getDB().createCollection(collectionName, null);
        }
        return collection;
    }

    /**
     * This method uses 'insert()' and not 'save()' so jsonData must have '_id' field
     *
     * @param collectionName
     * @return
     */
    // TODO: check the write concern for if there is any object which couldn't get inserted  successfully
    public void addObjects(String collectionName, List<DBObject> dbObjects) {
        DBCollection collection = getDB().getCollection(collectionName);
        if (dbObjects != null && dbObjects.size() > 0) {
            WriteConcern writeConcern = new WriteConcern();
            collection.insert(dbObjects, writeConcern);
        }
    }

    public void addObject(String collectionName, String jsonData) {
        DBCollection collection = getDB().getCollection(collectionName);
        collection.insert((DBObject) JSON.parse(jsonData));
    }

    public void addObject(String collectionName, JSONObject jsonData) {
        DBCollection collection = getDB().getCollection(collectionName);
        collection.insert(new BasicDBObject(jsonData));
    }

    public DBObject getObject(DBCollection collection, String objectId) {
        return getObject(collection, objectId, true);
    }

    public DBObject getObject(DBCollection collection, String idStr, boolean useObjectIdAsId) {
        if (StringUtils.isBlank(idStr))
            return null;
        long startTime = System.currentTimeMillis();
        BasicDBObject query = new BasicDBObject();
        Object id = useObjectIdAsId ? getObjectId(idStr) : idStr;
        query.put(MONGO_ID, id);
        DBObject result = collection.findOne(query);
        return result;
    }

    public DBObject getObject(String collectionName, String objectId) {
        return getObject(true, collectionName, objectId);
    }

    public DBObject getObject(boolean useObjectIdAsId, String collectionName, String idStr) {
        DBCollection collection = getDB().getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        Object id = useObjectIdAsId ? getObjectId(idStr) : idStr;
        query.put(MONGO_ID, id);
        DBObject result = collection.findOne(query);
        return result;
    }

    public boolean deleteObject(String collectionName, DBObject object) {
        DBCollection collection = getDB().getCollection(collectionName);
        if (object != null) {
            collection.remove(object);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteObject(String collectionName, String objectId) {
        DBCollection collection = getDB().getCollection(collectionName);
        DBObject result = getObject(collection, objectId);
        if (result != null) {
            collection.remove(result);
            return true;
        } else {
            return false;
        }
    }

    public void deleteObjects(String collectionName, Map queryParams) {
        DBCollection collection = getDB().getCollection(collectionName);
        DBObject query = new BasicDBObject(queryParams);
        collection.remove(query);
    }

    public DBObject getObject(String collectionName, Map queryParams) {
        long startTime = System.currentTimeMillis();
        DBCollection collection = getDB().getCollection(collectionName);

        BasicDBObject query = new BasicDBObject(queryParams);

        DBObject result = collection.findOne(query);
        return result;
    }

    public void updateObject(String collectionName, Map queryParams, DBObject newObj) {
        DBCollection collection = getDB().getCollection(collectionName);
        BasicDBObject query = new BasicDBObject(queryParams);
        // Will create new if no matching record is found
        collection.update(query, newObj, true, false);
    }

    public void updateObject(String collectionName, Map queryParams, String jsonData) {
        DBObject dbObject = (DBObject) JSON.parse(jsonData);
        updateObject(collectionName, queryParams, dbObject);
    }

    public WriteResult updateObject(String collectionName, Map queryParams, Map newObj, boolean upsert, boolean multi) {
        DBCollection collection = getDB().getCollection(collectionName);
        BasicDBObject query = new BasicDBObject(queryParams);
        BasicDBObject newObject = new BasicDBObject(newObj);
        return collection.update(query, newObject, upsert, multi);
    }

    public List<DBObject> getObjects(String collectionName, Map queryParams) {
        long startTime = System.currentTimeMillis();
        DBCollection collection = getDB().getCollection(collectionName);

        BasicDBObject query = new BasicDBObject(queryParams);
        DBCursor results = collection.find(query);
        List<DBObject> resultsList = new ArrayList<DBObject>();
        while (results.hasNext()) {
            DBObject next = results.next();
            resultsList.add(next);
        }
        return resultsList;
    }

    /**
     * For case insensitive search on the values of queryParams in mongo
     *
     * @param collectionName
     * @param queryParams
     * @param keyToBeEscapedFromCaseInsensitiveSearch
     * @return
     */
    public List<DBObject> getObjectsWithCaseInsensitiveSearch(String collectionName, Map queryParams, String keyToBeEscapedFromCaseInsensitiveSearch) {
        long startTime = System.currentTimeMillis();
        DBCollection collection = getDB().getCollection(collectionName);
        boolean isEscapedKeyPresent = !(StringUtils.isBlank(keyToBeEscapedFromCaseInsensitiveSearch));

        BasicDBObjectBuilder patternedQuery = BasicDBObjectBuilder.start();

        //for case insensitive search
        for (Iterator<String> itr = queryParams.keySet().iterator(); itr.hasNext(); ) {
            String key = itr.next();
            if (isEscapedKeyPresent && keyToBeEscapedFromCaseInsensitiveSearch.equalsIgnoreCase(key)) {
                continue;
            }
            Pattern pattern = Pattern.compile(queryParams.get(key).toString(), Pattern.CASE_INSENSITIVE);
            patternedQuery.add(key, pattern);
        }
        if (isEscapedKeyPresent && queryParams.containsKey(keyToBeEscapedFromCaseInsensitiveSearch)) {
            patternedQuery.add(keyToBeEscapedFromCaseInsensitiveSearch, queryParams.get(keyToBeEscapedFromCaseInsensitiveSearch));
        }

        BasicDBObject query = (BasicDBObject) patternedQuery.get();
        DBCursor results = collection.find(query);

        List<DBObject> resultsList = new ArrayList<DBObject>();
        while (results.hasNext()) {
            DBObject next = results.next();
            resultsList.add(next);
        }

        return resultsList;
    }

    public List<DBObject> getAllObjects(String collectionName) {
        DBCollection collection = getDB().getCollection(collectionName);
        DBCursor cursor = collection.find();
        List<DBObject> resultsList = new ArrayList<DBObject>();
        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            resultsList.add(next);
        }
        return resultsList;
    }

    public List<DBObject> getAllObjectsInOrder(String collectionName) {
        DBCollection collection = getDB().getCollection(collectionName);
        DBCursor cursor = collection.find();
        List<DBObject> resultsList = new LinkedList<>();
        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            resultsList.add(next);
        }
        return resultsList;
    }

    public DBObject getObject(String collectionName, Map queryParams, Map keys) {
        long startTime = System.currentTimeMillis();
        DBCollection collection = getDB().getCollection(collectionName);
        BasicDBObject query = new BasicDBObject(queryParams);
        BasicDBObject dbkeys = new BasicDBObject(keys);
        DBObject result = collection.findOne(query, dbkeys);
        return result;
    }

    public List<DBObject> getObjects(String collectionName, Map queryParams, Map keys) {
        DBCollection collection = getDB().getCollection(collectionName);

        BasicDBObject query = new BasicDBObject(queryParams);
        BasicDBObject dbkeys = new BasicDBObject(keys);

        DBCursor results = collection.find(query, dbkeys);

        List<DBObject> resultsList = new ArrayList<DBObject>();
        while (results.hasNext()) {
            DBObject next = results.next();
            resultsList.add(next);
        }
        return resultsList;
    }

    // give negative value of n to get all results
    public List<DBObject> getObjects(String collectionName, int startPos, int n, Map queryParams, Map sortingKey) {
        List<DBObject> objList = new ArrayList<DBObject>();
        if (startPos < 0) {
            return objList;
        }
        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            return objList;
        }
        BasicDBObject sortBy = new BasicDBObject();
        if (!CollectionUtils.isEmpty(sortingKey)) {
            sortBy.putAll(sortingKey);
        }
        BasicDBObject query = new BasicDBObject(queryParams);
        DBCursor dbCursor = collection.find(query);
        DBCursor sort = dbCursor.sort(sortBy);
        DBCursor skip = sort.skip(startPos);
        DBCursor limit = skip;
        if (n >= 0) {
            limit = skip.limit(n);
        }
        while (limit.hasNext()) {
            DBObject dbObject = limit.next();
            objList.add(dbObject);
        }
        return objList;
    }

    public List<DBObject> getObjects(String collectionName, int startPos, int n, Map queryParams) {
//        return getObjects(collectionName, startPos, n, queryParams, new HashMap<String, Object>());
        return getObjects(collectionName, startPos, n, queryParams, null);
    }

    public long getCount(String collectionName, Map queryParams) {
        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            return 0;
        }
        DBObject query = new BasicDBObject(queryParams);
        long count = collection.getCount(query);
        return count;
    }

    public long getTotalCount(String collectionName, Map queryParams, String dateField, long fromTime) {
        return getTotalCount(collectionName, queryParams, null, dateField, fromTime);
    }

    public long getTotalCount(String collectionName, Map queryParams, Map<String, List<Object>> keyRangeValueMap, String dateField, long fromTime) {
        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            return 0;
        }

        DBObject query = mergeAllANDQueries(new BasicDBObject(queryParams), getExpiryQuery(dateField, fromTime));

        if (keyRangeValueMap != null) {
            for (Entry<String, List<Object>> en : keyRangeValueMap.entrySet()) {
                String fieldName = en.getKey();
                BasicDBList rangeList = new BasicDBList();
                rangeList.addAll(en.getValue());
                DBObject rangeObj = new BasicDBObject("$in", rangeList);
                query.put(fieldName, rangeObj);
            }
        }

        long count = collection.getCount(query);

        return count;
    }

    public List<DBObject> getObjectsInDateRange(String collectionName, Map queryParams, String dateField, long fromTime, long toTime, Map sortKey) {
        List<DBObject> objList = new ArrayList<DBObject>();

        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            return objList;
        }
        BasicDBObject query = new BasicDBObject(queryParams);
        query.put(dateField, BasicDBObjectBuilder.start("$gte", fromTime).add("$lte", toTime).get());
        DBCursor dbCursor = collection.find(query);
        if (dbCursor != null) {
            if (sortKey != null && sortKey.size() > 0) {
                DBCursor sort = dbCursor.sort(new BasicDBObject(sortKey));
                while (sort.hasNext()) {
                    DBObject dbObject = sort.next();
                    objList.add(dbObject);
                }
            } else {
                while (dbCursor.hasNext()) {
                    DBObject dbObject = dbCursor.next();
                    objList.add(dbObject);
                }
            }
        }
        return objList;
    }

    public List<DBObject> getObjectsInDateRangeWithOffsetAndCount(String collectionName, Map queryParams, String dateField, long fromTime, long toTime, Map sortKey, int n, int startPos) {
        List<DBObject> objList = new ArrayList<DBObject>();

        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            return objList;
        }
        BasicDBObject query = new BasicDBObject(queryParams);
        query.put(dateField, BasicDBObjectBuilder.start("$gte", fromTime).add("$lte", toTime).get());
        DBCursor dbCursor = collection.find(query);
        if (dbCursor != null) {
            if (sortKey != null && sortKey.size() > 0) {
                DBCursor sort = dbCursor.sort(new BasicDBObject(sortKey));
                DBCursor skip = sort.skip(startPos);
                DBCursor limit = skip;
                if (n >= 0) {
                    limit = skip.limit(n);
                }
                while (limit.hasNext()) {
                    DBObject dbObject = limit.next();
                    objList.add(dbObject);
                }
            } else {
                DBCursor skip = dbCursor.skip(startPos);
                DBCursor limit = skip;
                if (n >= 0) {
                    limit = skip.limit(n);
                }
                while (limit.hasNext()) {
                    DBObject dbObject = limit.next();
                    objList.add(dbObject);
                }
            }
        }
        return objList;
    }

    // will return only fields given in field list
    public List<DBObject> getPartialObjectsInDateRange(String collectionName, Map queryParams, List<String> fieldList, String dateField, long fromTime, long toTime, Map sortKey) {
        List<DBObject> objList = new ArrayList<DBObject>();

        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            return objList;
        }
        BasicDBObject query = new BasicDBObject(queryParams);
        query.put(dateField, BasicDBObjectBuilder.start("$gte", fromTime).add("$lte", toTime).get());
        Map fieldMap = new HashMap();
        for (String field : fieldList) {
            fieldMap.put(field, 1);
        }
        DBCursor dbCursor = collection.find(query, new BasicDBObject(fieldMap));
        if (dbCursor != null) {
            if (sortKey != null && sortKey.size() > 0) {
                DBCursor sort = dbCursor.sort(new BasicDBObject(sortKey));
                while (sort.hasNext()) {
                    DBObject dbObject = sort.next();
                    objList.add(dbObject);
                }
            } else {
                while (dbCursor.hasNext()) {
                    DBObject dbObject = dbCursor.next();
                    objList.add(dbObject);
                }
            }
        }
        return objList;
    }

    // will return only fields given in field list
    public List<DBObject> getPartialObjects(String collectionName, Map<String, Object> queryParams, List<String> fieldList, Map<String, Object> sortKey) {
        Map fieldMap = new HashMap();
        for (String field : fieldList) {
            fieldMap.put(field, 1);
        }
        return getPartialObjects(collectionName, queryParams, fieldMap, sortKey);
    }

    public List<DBObject> getPartialObjects(String collectionName, Map<String, Object> queryParams, Map fieldParams, Map sortKey) {
        return getPartialObjects(collectionName, 0, -1, queryParams, fieldParams, sortKey);
    }

    // give negative value of n to get all results
    public List<DBObject> getPartialObjects(String collectionName, int startPos, int n, Map queryParams, Map fieldParams, Map sortingKey) {
        List<DBObject> objList = new ArrayList<DBObject>();
        if (startPos < 0) {
            return objList;
        }
        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null) {
            return objList;
        }
        BasicDBObject sortBy = null;
        if (sortingKey != null) {
            sortBy = new BasicDBObject(sortingKey);
        }
        BasicDBObject query = new BasicDBObject(queryParams);
        DBCursor dbCursor = collection.find(query, new BasicDBObject(fieldParams));
        DBCursor skip = dbCursor.skip(startPos);
        DBCursor limit = skip;
        if (n >= 0) {
            limit = skip.limit(n);
        }
        DBCursor sort = limit;
        if (sortBy != null) {
            sort = limit.sort(sortBy);
        }
        while (sort.hasNext()) {
            DBObject dbObject = sort.next();
            objList.add(dbObject);
        }
        return objList;
    }

    public List<DBObject> getObjects(String collectionName, int startPos, int numResults, Map<String, Object> queryParamsMap, Map<String, List<Object>> keyRangeValueMap, Map<String, Object> sortingMap) {
        DBObject query = new BasicDBObject(queryParamsMap);
        if (keyRangeValueMap != null) {
            for (Entry<String, List<Object>> en : keyRangeValueMap.entrySet()) {
                String fieldName = en.getKey();
                BasicDBList rangeList = new BasicDBList();
                rangeList.addAll(en.getValue());
                DBObject rangeObj = new BasicDBObject("$in", rangeList);
                query.put(fieldName, rangeObj);
            }
        }

        return getData(collectionName, query, null, sortingMap, startPos, numResults);
    }

    public List<DBObject> getObjectsInDateRangeIfExist(String collectionName, Map<String, Object> queryParams, Map<String, Object> fieldsParam, Map<String, Object> sortingMap, String dateField,
                                                       long fromTime, long toTime, int numResults, int startPos) {

        return getData(collectionName, mergeAllANDQueries(new BasicDBObject(queryParams), getExpiryQuery(dateField, fromTime)), fieldsParam, sortingMap, startPos, numResults);
    }

    public DBObject mergeAllANDQueries(DBObject... andQueries) {
        if (andQueries == null || andQueries.length == 0) {
            return null;
        }
        DBObject finalQuery = new BasicDBObject();
        for (DBObject andQuery : andQueries) {
            finalQuery.putAll(andQuery);
        }
        return finalQuery;
    }

    /**
     * @param collectionName
     * @param query
     * @param sortingMap
     * @param fieldsParam    Map containing fields to be returned. Null to return all the fields
     * @param startPos
     * @param numResults
     * @return
     */
    private List<DBObject> getData(String collectionName, DBObject query, Map<String, Object> fieldsParam, Map<String, Object> sortingMap, int startPos, int numResults) {
        DBCollection dbCollection = getDB().getCollection(collectionName);
        if (sortingMap != null) {
            BasicDBObject sortBy = new BasicDBObject(sortingMap);
        }
        List<DBObject> resultList = new ArrayList<DBObject>();
        DBCursor cursor = null;
        if (fieldsParam == null)
            cursor = dbCollection.find(query);
        else
            cursor = dbCollection.find(query, new BasicDBObject(fieldsParam));
        if (cursor == null)
            return resultList; // empty list
        if (startPos >= 0)
            cursor = cursor.skip(startPos);
        if (numResults >= 0)
            cursor = cursor.limit(numResults);
        if (sortingMap != null && sortingMap.size() > 0)
            cursor = cursor.sort(new BasicDBObject(sortingMap));
        while (cursor.hasNext())
            resultList.add(cursor.next());
        return resultList;
    }

    public long getTotalCount(String collectionName, Map queryParams) {
        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null)
            return 0;
        DBObject query = new BasicDBObject(queryParams);
        long count = collection.getCount(query);
        return count;
    }

    public long getTotalCount(String collectionName, Map queryParams, Map<String, List<Object>> keyRangeValueMap) {
        DBCollection collection = getDB().getCollection(collectionName);
        if (collection == null)
            return 0;
        DBObject query = new BasicDBObject(queryParams);
        if (keyRangeValueMap != null) {
            for (Entry<String, List<Object>> en : keyRangeValueMap.entrySet()) {
                String fieldName = en.getKey();
                BasicDBList rangeList = new BasicDBList();
                rangeList.addAll(en.getValue());
                DBObject rangeObj = new BasicDBObject("$in", rangeList);
                query.put(fieldName, rangeObj);
            }
        }
        long count = collection.getCount(query);
        return count;
    }

    public List<DBObject> getObjectsWithANDQueryInDateRangeIfExist(String collectionName,
                                                                   Map<Object, List<Object>> queryParams, String dateField, long fromTime, long toTime, Map sortKey, int n,
                                                                   int startPos) {

        DBCollection collection = getDB().getCollection(collectionName);
        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> dbObjectList = new ArrayList<BasicDBObject>();
        for (Entry<Object, List<Object>> entry : queryParams.entrySet()) {
            for (Object paramvalue : entry.getValue()) {
                dbObjectList.add(new BasicDBObject((String) entry.getKey(), paramvalue));
            }

        }
        andQuery.put("$and", dbObjectList);
        if (StringUtils.isNotBlank(dateField) && fromTime < toTime)
            andQuery.put(dateField, BasicDBObjectBuilder.start("$gte", fromTime).add("$lte", toTime).get());
        DBCursor dbCursor = collection.find(andQuery);

        List<DBObject> resultsList = new ArrayList<DBObject>();
        if (dbCursor != null) {
            if (sortKey != null && sortKey.size() > 0) {
                DBCursor sort = dbCursor.sort(new BasicDBObject(sortKey));
                DBCursor skip = sort.skip(startPos);
                DBCursor limit = skip;
                if (n >= 0) {
                    limit = skip.limit(n);
                }
                while (limit.hasNext()) {
                    DBObject dbObject = limit.next();
                    resultsList.add(dbObject);
                }
            } else {
                while (dbCursor.hasNext()) {
                    DBObject dbObject = dbCursor.next();
                    resultsList.add(dbObject);
                }
            }
        }
        return resultsList;

    }

    public DBObject findAndModify(String collectionName, Map queryParams, Map updateParams) {
        long startTime = System.currentTimeMillis();
        DBCollection collection = getDB().getCollection(collectionName);

        BasicDBObject query = new BasicDBObject(queryParams);
        BasicDBObject update = new BasicDBObject(updateParams);
        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", update);

        DBObject result = collection.findAndModify(query, updateObject);

        return result;
    }
}
