module com.macewan.infoedmonton {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.junit.jupiter.api;
    requires javafx.web;
    requires java.scripting;

    opens com.macewan.infoedmonton to javafx.fxml;
    exports com.macewan.infoedmonton;
}