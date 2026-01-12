module com.smecs {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;

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
}

