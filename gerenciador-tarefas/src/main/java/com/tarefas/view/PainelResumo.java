package com.tarefas.view;

import com.tarefas.controller.TarefaController;
import com.tarefas.observer.TarefaObserver;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PainelResumo extends HBox implements TarefaObserver {
    private final TarefaController controller;

    private final Label lblTotal = new Label("0");
    private final Label lblPendentes = new Label("0");
    private final Label lblConcluidas = new Label("0");
    private final Label lblAltaPrioridade = new Label("0");
    private final Label lblProgresso = new Label("0%");
    private final ProgressBar barraProgresso = new ProgressBar(0);

    public PainelResumo(TarefaController controller) {
        this.controller = controller;
        this.controller.adicionarObserver(this);

        setSpacing(12);
        setPadding(new Insets(12, 20, 8, 20));
        setAlignment(Pos.CENTER_LEFT);

        VBox cardTotal = criarCard("Total de Tarefas", lblTotal, "card-metrica");
        VBox cardPendentes = criarCard("Em Aberto", lblPendentes, "card-metrica");
        VBox cardConcluidas = criarCard("Concluídas", lblConcluidas, "card-metrica");
        VBox cardAlta = criarCard("Prioridade Alta", lblAltaPrioridade, "card-metrica-destaque");
        VBox cardProgresso = criarCardProgresso();

        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        HBox.setHgrow(cardPendentes, Priority.ALWAYS);
        HBox.setHgrow(cardConcluidas, Priority.ALWAYS);
        HBox.setHgrow(cardAlta, Priority.ALWAYS);
        HBox.setHgrow(cardProgresso, Priority.ALWAYS);

        getChildren().addAll(cardTotal, cardPendentes, cardConcluidas, cardAlta, cardProgresso);
        atualizar();
    }

    private VBox criarCard(String titulo, Label valor, String classeCss) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("card-titulo");

        valor.getStyleClass().add("card-valor");

        VBox box = new VBox(4, lblTitulo, valor);
        box.getStyleClass().add(classeCss);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private VBox criarCardProgresso() {
        Label lblTitulo = new Label("Progresso Geral");
        lblTitulo.getStyleClass().add("card-titulo");

        lblProgresso.getStyleClass().add("card-valor-menor");
        barraProgresso.setMaxWidth(Double.MAX_VALUE);
        barraProgresso.getStyleClass().add("barra-progresso");

        VBox box = new VBox(6, lblTitulo, lblProgresso, barraProgresso);
        box.getStyleClass().add("card-metrica");
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    @Override
    public void atualizar() {
        int total = controller.getTotalTarefas();
        int pendentes = controller.getTotalPendentes();
        int concluidas = controller.getTotalConcluidas();
        int alta = controller.getTotalPrioridadeAlta();
        double progresso = controller.getProgressoGeral();

        lblTotal.setText(String.valueOf(total));
        lblPendentes.setText(String.valueOf(pendentes));
        lblConcluidas.setText(String.valueOf(concluidas));
        lblAltaPrioridade.setText(String.valueOf(alta));
        lblProgresso.setText(Math.round(progresso) + "%");
        barraProgresso.setProgress(progresso / 100.0);
    }
}
