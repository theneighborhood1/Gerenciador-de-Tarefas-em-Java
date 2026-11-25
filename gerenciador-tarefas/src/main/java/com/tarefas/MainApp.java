package com.tarefas;

import com.tarefas.controller.TarefaController;
import com.tarefas.view.TelaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TarefaController controller = new TarefaController();
        TelaPrincipal tela = new TelaPrincipal(controller);

        Scene scene = new Scene(tela, 1020, 660);
        var cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Gerenciador de Tarefas");
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(560);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
