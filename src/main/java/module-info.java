module com.smecs {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;

    // Epic 4: MongoDB dependencies
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires org.json;

    opens com.smecs to javafx.fxml;
    opens com.smecs.controller to javafx.fxml;
    opens com.smecs.model to javafx.base;

    exports com.smecs;
    exports com.smecs.controller;
    exports com.smecs.model;
    exports com.smecs.service;
    exports com.smecs.dao;
    exports com.smecs.util;
    exports com.smecs.cache;
    exports com.smecs.nosql;  // Epic 4
    exports com.smecs.test;   // Epic 4
}

