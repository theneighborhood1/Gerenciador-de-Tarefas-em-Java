package com.tarefas.view;

import com.tarefas.controller.TarefaController;
import com.tarefas.model.GrupoTarefas;
import com.tarefas.model.ItemTarefa;
import com.tarefas.model.Prioridade;
import com.tarefas.model.StatusTarefa;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DialogoTarefa {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TarefaController controller;
    private final boolean modoGrupo;
    private final ItemTarefa itemEdicao;

    public DialogoTarefa(TarefaController controller, boolean modoGrupo, ItemTarefa itemEdicao) {
        this.controller = controller;
        this.modoGrupo = modoGrupo;
        this.itemEdicao = itemEdicao;
    }

    public void exibir(Window owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        boolean editando = itemEdicao != null;
        String tituloJanela;
        if (editando) {
            tituloJanela = modoGrupo ? "Editar Grupo de Tarefas" : "Editar Tarefa";
        } else {
            tituloJanela = modoGrupo ? "Novo Grupo de Tarefas" : "Nova Tarefa";
        }
        stage.setTitle(tituloJanela);

        Label lblCabecalho = new Label(tituloJanela);
        lblCabecalho.getStyleClass().add("dialogo-titulo");

        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText(modoGrupo ? "Ex: Trabalho de POO II" : "Ex: Implementar padrão Observer");

        ComboBox<String> cbCategoria = new ComboBox<>();
        cbCategoria.setEditable(true);
        cbCategoria.setMaxWidth(Double.MAX_VALUE);
        cbCategoria.getItems().addAll("Faculdade", "Trabalho", "Pessoal", "Estudos");
        for (String cat : controller.listarCategorias()) {
            if (!cbCategoria.getItems().contains(cat)) {
                cbCategoria.getItems().add(cat);
            }
        }
        cbCategoria.setPromptText("Selecione ou digite uma categoria");

        ComboBox<GrupoOpcao> cbGrupo = new ComboBox<>();
        cbGrupo.setMaxWidth(Double.MAX_VALUE);
        cbGrupo.getItems().add(new GrupoOpcao(null, "Nenhum (nível principal)"));
        List<GrupoTarefas> grupos = controller.listarGrupos();
        for (GrupoTarefas g : grupos) {
            if (editando && g.getId() == itemEdicao.getId()) {
                continue;
            }
            cbGrupo.getItems().add(new GrupoOpcao(g.getId(), g.getTitulo()));
        }
        cbGrupo.getSelectionModel().selectFirst();

        ComboBox<Prioridade> cbPrioridade = new ComboBox<>();
        cbPrioridade.setMaxWidth(Double.MAX_VALUE);
        cbPrioridade.getItems().addAll(Prioridade.values());
        cbPrioridade.setValue(Prioridade.MEDIA);

        ComboBox<StatusTarefa> cbStatus = new ComboBox<>();
        cbStatus.setMaxWidth(Double.MAX_VALUE);
        cbStatus.getItems().addAll(StatusTarefa.values());
        cbStatus.setValue(StatusTarefa.PENDENTE);

        DatePicker dpPrazo = new DatePicker();
        dpPrazo.setMaxWidth(Double.MAX_VALUE);
        dpPrazo.setPromptText("dd/mm/aaaa (opcional)");
        configurarFormatoData(dpPrazo);

        TextArea txtDescricao = new TextArea();
        txtDescricao.setPrefRowCount(3);
        txtDescricao.setWrapText(true);
        txtDescricao.setPromptText("Detalhes adicionais (opcional)");

        if (editando) {
            txtTitulo.setText(itemEdicao.getTitulo());
            cbCategoria.setValue(itemEdicao.getCategoria());
            cbPrioridade.setValue(itemEdicao.getPrioridade());
            cbStatus.setValue(itemEdicao.getStatus());
            dpPrazo.setValue(itemEdicao.getPrazo());
            txtDescricao.setText(itemEdicao.getDescricao());

            Integer grupoAtual = itemEdicao.getGrupoId();
            if (grupoAtual != null) {
                for (GrupoOpcao opcao : cbGrupo.getItems()) {
                    if (grupoAtual.equals(opcao.id)) {
                        cbGrupo.setValue(opcao);
                        break;
                    }
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        int linha = 0;
        grid.add(criarRotulo("Título *"), 0, linha);
        grid.add(txtTitulo, 1, linha++);

        grid.add(criarRotulo("Categoria *"), 0, linha);
        grid.add(cbCategoria, 1, linha++);

        grid.add(criarRotulo("Grupo pai"), 0, linha);
        grid.add(cbGrupo, 1, linha++);

        grid.add(criarRotulo("Prioridade"), 0, linha);
        grid.add(cbPrioridade, 1, linha++);

        if (!modoGrupo) {
            grid.add(criarRotulo("Status"), 0, linha);
            grid.add(cbStatus, 1, linha++);
        }

        grid.add(criarRotulo("Prazo"), 0, linha);
        grid.add(dpPrazo, 1, linha++);

        grid.add(criarRotulo("Descrição"), 0, linha);
        grid.add(txtDescricao, 1, linha);

        GridPane.setHgrow(txtTitulo, Priority.ALWAYS);
        GridPane.setHgrow(cbCategoria, Priority.ALWAYS);
        GridPane.setHgrow(cbGrupo, Priority.ALWAYS);
        GridPane.setHgrow(cbPrioridade, Priority.ALWAYS);
        GridPane.setHgrow(cbStatus, Priority.ALWAYS);
        GridPane.setHgrow(dpPrazo, Priority.ALWAYS);
        GridPane.setHgrow(txtDescricao, Priority.ALWAYS);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("botao-secundario");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnSalvar = new Button("Salvar");
        btnSalvar.getStyleClass().add("botao-primario");
        btnSalvar.setDefaultButton(true);
        btnSalvar.setOnAction(e -> {
            try {
                LocalDate prazo = lerDataSegura(dpPrazo);
                String categoria = cbCategoria.getEditor().getText();
                if (categoria == null || categoria.isBlank()) {
                    categoria = cbCategoria.getValue();
                }

                GrupoOpcao grupoSelecionado = cbGrupo.getValue();
                Integer grupoId = grupoSelecionado == null ? null : grupoSelecionado.id;

                if (editando) {
                    StatusTarefa statusFinal = modoGrupo ? null : cbStatus.getValue();
                    controller.editarItem(
                            itemEdicao.getId(),
                            grupoId,
                            txtTitulo.getText(),
                            txtDescricao.getText(),
                            categoria,
                            cbPrioridade.getValue(),
                            statusFinal,
                            prazo
                    );
                } else if (modoGrupo) {
                    controller.cadastrarGrupo(
                            grupoId,
                            txtTitulo.getText(),
                            txtDescricao.getText(),
                            categoria,
                            cbPrioridade.getValue(),
                            prazo
                    );
                } else {
                    controller.cadastrarTarefa(
                            grupoId,
                            txtTitulo.getText(),
                            txtDescricao.getText(),
                            categoria,
                            cbPrioridade.getValue(),
                            cbStatus.getValue(),
                            prazo
                    );
                }
                stage.close();
            } catch (IllegalArgumentException ex) {
                mostrarErro("Dados inválidos", ex.getMessage());
            } catch (IOException ex) {
                mostrarErro("Erro ao salvar", "Não foi possível gravar os dados no arquivo CSV: " + ex.getMessage());
            }
        });

        HBox botoes = new HBox(10, btnCancelar, btnSalvar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(10, 0, 0, 0));

        VBox root = new VBox(14, lblCabecalho, grid, botoes);
        root.getStyleClass().add("dialogo-root");
        root.setPadding(new Insets(20));
        root.setPrefWidth(460);

        Scene scene = new Scene(root);
        var cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
        stage.showAndWait();
    }

    private Label criarRotulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("campo-label");
        label.setMinWidth(90);
        return label;
    }

    private void configurarFormatoData(DatePicker picker) {
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : FORMATO_DATA.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(text.trim(), FORMATO_DATA);
            }
        });
    }

    private LocalDate lerDataSegura(DatePicker picker) {
        String texto = picker.getEditor().getText();
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato dd/mm/aaaa ou selecione no calendário.");
        }
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private static class GrupoOpcao {
        final Integer id;
        final String nome;

        GrupoOpcao(Integer id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }
}
