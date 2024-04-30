module com.example.sam_game {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.sam_game_pac to javafx.fxml;
    exports com.sam_game_pac;
}