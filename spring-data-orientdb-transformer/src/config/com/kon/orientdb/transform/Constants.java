package com.kon.orientdb.transform;

/**
 * Created by kshevchuk on 10/1/2015.
 */
public enum Constants {

    ORIENT_DB_URL("orient.db.url"),
    ORIENT_DB_USERNAME("orient.db.username"),
    ORIENT_DB_PASSWORD("orient.db.password"),
    ORIENT_DB_MIN_POOL_SIZE("orient.db.min.pool.size"),
    ORIENT_DB_MAX_POOL_SIZE("orient.db.max.pool.size"),
    ORIENT_DB_DNS_TRANSFORMATION_INPUT("orient.db.dns.transformation.input");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }



}
