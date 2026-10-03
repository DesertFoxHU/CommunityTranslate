module me.bence.ctranslate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens me.bence.ctranslate to javafx.fxml;
    exports me.bence.ctranslate;
}