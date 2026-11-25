package com.tarefas.view;

import com.tarefas.controller.TarefaController;
import com.tarefas.model.GrupoTarefas;
import com.tarefas.model.ItemTarefa;
import com.tarefas.model.Prioridade;
import com.tarefas.model.StatusTarefa;
import com.tarefas.observer.TarefaObserver;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TelaPrincipal extends BorderPane implements TarefaObserver {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TarefaController controller;
    private final TreeTableView<ItemTarefa> tabela = new TreeTableView<>();

    private final TextField txtBusca = new TextField();
    private final ComboBox<String> cbCategoria = new ComboBox<>();
    private final ComboBox<String> cbPrioridade = new ComboBox<>();
    private final ComboBox<String> cbStatus = new ComboBox<>();
    private final Label lblDetalheTitulo = new Label("Selecione uma tarefa ou grupo para ver os detalhes.");
    private final Label lblDetalheDescricao = new Label("");

    public TelaPrincipal(TarefaController controller) {
        this.controller = controller;
        this.controller.adicionarObserver(this);

        getStyleClass().add("root-pane");

        VBox topo = new VBox();
        topo.getChildren().addAll(criarCabecalho(), new PainelResumo(controller));
        setTop(topo);

        VBox centro = new VBox(10);
        centro.setPadding(new Insets(8, 20, 12, 20));
        centro.getChildren().addAll(criarBarraFiltros(), criarBarraAcoes(), tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        setCenter(centro);

        setBottom(criarRodapeDetalhes());

        configurarTabela();
        atualizar();
    }

    private HBox criarCabecalho() {
        Label lblTitulo = new Label("Gerenciador de Tarefas");
        lblTitulo.getStyleClass().add("header-titulo");

        Label lblSubtitulo = new Label("Organize tarefas, subtarefas, categorias e prioridades");
        lblSubtitulo.getStyleClass().add("header-subtitulo");

        VBox textos = new VBox(2, lblTitulo, lblSubtitulo);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Label lblBadge = new Label("POO II • JavaFX");
        lblBadge.getStyleClass().add("header-badge");

        HBox header = new HBox(12, textos, espaco, lblBadge);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 18, 22));
        header.getStyleClass().add("header-bar");
        return header;
    }

    private HBox criarBarraFiltros() {
        txtBusca.setPromptText("Buscar por título ou descrição...");
        txtBusca.setPrefWidth(260);
        txtBusca.textProperty().addListener((obs, ant, novo) -> carregarTabela());

        cbCategoria.setPrefWidth(150);
        cbCategoria.setOnAction(e -> carregarTabela());

        cbPrioridade.getItems().addAll("Todas prioridades", "Alta", "Média", "Baixa");
        cbPrioridade.getSelectionModel().selectFirst();
        cbPrioridade.setPrefWidth(155);
        cbPrioridade.setOnAction(e -> carregarTabela());

        cbStatus.getItems().addAll("Todos status", "Pendente", "Em andamento", "Concluída");
        cbStatus.getSelectionModel().selectFirst();
        cbStatus.setPrefWidth(150);
        cbStatus.setOnAction(e -> carregarTabela());

        Button btnLimpar = new Button("Limpar filtros");
        btnLimpar.getStyleClass().add("botao-secundario");
        btnLimpar.setOnAction(e -> {
            txtBusca.clear();
            cbCategoria.getSelectionModel().selectFirst();
            cbPrioridade.getSelectionModel().selectFirst();
            cbStatus.getSelectionModel().selectFirst();
            carregarTabela();
        });

        HBox barra = new HBox(10, txtBusca, cbCategoria, cbPrioridade, cbStatus, btnLimpar);
        barra.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtBusca, Priority.ALWAYS);
        return barra;
    }

    private HBox criarBarraAcoes() {
        Button btnNovaTarefa = new Button("+ Nova Tarefa");
        btnNovaTarefa.getStyleClass().add("botao-primario");
        btnNovaTarefa.setOnAction(e -> abrirDialogo(false, null));

        Button btnNovoGrupo = new Button("+ Novo Grupo");
        btnNovoGrupo.getStyleClass().add("botao-laranja-contorno");
        btnNovoGrupo.setOnAction(e -> abrirDialogo(true, null));

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("botao-secundario");
        btnEditar.setOnAction(e -> editarSelecionado());

        Button btnConcluir = new Button("Concluir / Reabrir");
        btnConcluir.getStyleClass().add("botao-secundario");
        btnConcluir.setOnAction(e -> alternarStatusSelecionado());

        Button btnRemover = new Button("Remover");
        btnRemover.getStyleClass().add("botao-perigo");
        btnRemover.setOnAction(e -> removerSelecionado());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox acoes = new HBox(10, btnNovaTarefa, btnNovoGrupo, espaco, btnEditar, btnConcluir, btnRemover);
        acoes.setAlignment(Pos.CENTER_LEFT);
        return acoes;
    }

    private VBox criarRodapeDetalhes() {
        lblDetalheTitulo.getStyleClass().add("rodape-titulo");
        lblDetalheDescricao.getStyleClass().add("rodape-descricao");
        lblDetalheDescricao.setWrapText(true);

        VBox rodape = new VBox(3, lblDetalheTitulo, lblDetalheDescricao);
        rodape.getStyleClass().add("rodape-box");
        rodape.setPadding(new Insets(10, 20, 12, 20));
        return rodape;
    }

    private void configurarTabela() {
        tabela.setShowRoot(false);
        tabela.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(new Label("Nenhuma tarefa encontrada."));

        TreeTableColumn<ItemTarefa, String> colTitulo = new TreeTableColumn<>("Tarefa / Grupo");
        colTitulo.setPrefWidth(280);
        colTitulo.setCellValueFactory(param -> {
            ItemTarefa item = param.getValue().getValue();
            String texto = item.isGrupo() ? "[Grupo] " + item.getTitulo() : item.getTitulo();
            return new ReadOnlyStringWrapper(texto);
        });

        TreeTableColumn<ItemTarefa, String> colCategoria = new TreeTableColumn<>("Categoria");
        colCategoria.setPrefWidth(130);
        colCategoria.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getCategoria()));

        TreeTableColumn<ItemTarefa, String> colPrioridade = new TreeTableColumn<>("Prioridade");
        colPrioridade.setPrefWidth(110);
        colPrioridade.setCellValueFactory(param ->
                new ReadOnlyStringWrapper(param.getValue().getValue().getPrioridade().getDescricao()));
        colPrioridade.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("prio-alta", "prio-media", "prio-baixa");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("Alta".equalsIgnoreCase(item)) {
                        getStyleClass().add("prio-alta");
                    } else if ("Média".equalsIgnoreCase(item)) {
                        getStyleClass().add("prio-media");
                    } else {
                        getStyleClass().add("prio-baixa");
                    }
                }
            }
        });

        TreeTableColumn<ItemTarefa, String> colStatus = new TreeTableColumn<>("Status");
        colStatus.setPrefWidth(125);
        colStatus.setCellValueFactory(param ->
                new ReadOnlyStringWrapper(param.getValue().getValue().getStatus().getDescricao()));

        TreeTableColumn<ItemTarefa, String> colPrazo = new TreeTableColumn<>("Prazo");
        colPrazo.setPrefWidth(105);
        colPrazo.setCellValueFactory(param -> {
            var prazo = param.getValue().getValue().getPrazo();
            return new ReadOnlyStringWrapper(prazo == null ? "-" : FORMATO_DATA.format(prazo));
        });

        TreeTableColumn<ItemTarefa, String> colProgresso = new TreeTableColumn<>("Progresso");
        colProgresso.setPrefWidth(125);
        colProgresso.setCellValueFactory(param -> {
            ItemTarefa item = param.getValue().getValue();
            long pct = Math.round(item.calcularProgresso());
            String texto = item.isGrupo()
                    ? item.contarConcluidas() + "/" + item.contarTarefas() + " (" + pct + "%)"
                    : pct + "%";
            return new ReadOnlyStringWrapper(texto);
        });

        tabela.getColumns().addAll(colTitulo, colCategoria, colPrioridade, colStatus, colPrazo, colProgresso);

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, ant, novo) -> {
            if (novo == null || novo.getValue() == null) {
                lblDetalheTitulo.setText("Selecione uma tarefa ou grupo para ver os detalhes.");
                lblDetalheDescricao.setText("");
            } else {
                ItemTarefa item = novo.getValue();
                String tipo = item.isGrupo() ? "Grupo" : "Tarefa";
                lblDetalheTitulo.setText(tipo + " #" + item.getId() + " — " + item.getTitulo()
                        + " (" + item.getCategoria() + " • " + item.getStatus().getDescricao() + ")");
                String desc = item.getDescricao();
                lblDetalheDescricao.setText(desc == null || desc.isBlank() ? "Sem descrição informada." : desc);
            }
        });

        tabela.setRowFactory(tv -> {
            TreeTableRow<ItemTarefa> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editarSelecionado();
                }
            });
            return row;
        });
    }

    @Override
    public void atualizar() {
        atualizarComboCategorias();
        carregarTabela();
    }

    private void atualizarComboCategorias() {
        String selecionada = cbCategoria.getValue();
        cbCategoria.getItems().clear();
        cbCategoria.getItems().add("Todas categorias");
        cbCategoria.getItems().addAll(controller.listarCategorias());

        if (selecionada != null && cbCategoria.getItems().contains(selecionada)) {
            cbCategoria.setValue(selecionada);
        } else {
            cbCategoria.getSelectionModel().selectFirst();
        }
    }

    private void carregarTabela() {
        String cat = cbCategoria.getValue();
        if (cat == null || cat.equals("Todas categorias")) {
            cat = null;
        }

        Prioridade prio = null;
        String prioTexto = cbPrioridade.getValue();
        if (prioTexto != null && !prioTexto.equals("Todas prioridades")) {
            prio = Prioridade.fromTexto(prioTexto);
        }

        StatusTarefa status = null;
        String statusTexto = cbStatus.getValue();
        if (statusTexto != null && !statusTexto.equals("Todos status")) {
            status = StatusTarefa.fromTexto(statusTexto);
        }

        List<ItemTarefa> itens = controller.filtrar(txtBusca.getText(), cat, prio, status);
        TreeItem<ItemTarefa> raiz = new TreeItem<>(null);
        for (ItemTarefa item : itens) {
            raiz.getChildren().add(montarNo(item));
        }
        tabela.setRoot(raiz);
    }

    private TreeItem<ItemTarefa> montarNo(ItemTarefa item) {
        TreeItem<ItemTarefa> no = new TreeItem<>(item);
        no.setExpanded(true);
        if (item instanceof GrupoTarefas grupo) {
            for (ItemTarefa filho : grupo.getItens()) {
                no.getChildren().add(montarNo(filho));
            }
        }
        return no;
    }

    private ItemTarefa getItemSelecionadoReal() {
        TreeItem<ItemTarefa> selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null || selecionado.getValue() == null) {
            return null;
        }
        return controller.buscarPorId(selecionado.getValue().getId());
    }

    private void abrirDialogo(boolean modoGrupo, ItemTarefa item) {
        DialogoTarefa dialogo = new DialogoTarefa(controller, modoGrupo, item);
        dialogo.exibir(getScene().getWindow());
    }

    private void editarSelecionado() {
        ItemTarefa item = getItemSelecionadoReal();
        if (item == null) {
            mostrarAviso("Nenhum item selecionado", "Selecione uma tarefa ou grupo na lista para editar.");
            return;
        }
        abrirDialogo(item.isGrupo(), item);
    }

    private void alternarStatusSelecionado() {
        ItemTarefa item = getItemSelecionadoReal();
        if (item == null) {
            mostrarAviso("Nenhum item selecionado", "Selecione uma tarefa ou grupo para alterar o status.");
            return;
        }
        try {
            controller.alternarConcluida(item.getId());
        } catch (IllegalArgumentException | IOException ex) {
            mostrarAviso("Erro", ex.getMessage());
        }
    }

    private void removerSelecionado() {
        ItemTarefa item = getItemSelecionadoReal();
        if (item == null) {
            mostrarAviso("Nenhum item selecionado", "Selecione uma tarefa ou grupo para remover.");
            return;
        }

        String complemento = item.isGrupo() ? "\nTodas as subtarefas dentro deste grupo também serão removidas." : "";
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar remoção");
        confirmacao.setHeaderText("Remover \"" + item.getTitulo() + "\"?");
        confirmacao.setContentText("Esta ação não pode ser desfeita." + complemento);

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                controller.removerItem(item.getId());
            } catch (IllegalArgumentException | IOException ex) {
                mostrarAviso("Erro ao remover", ex.getMessage());
            }
        }
    }

    private void mostrarAviso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
