package com.tarefas.repository;

import com.tarefas.model.GrupoTarefas;
import com.tarefas.model.ItemTarefa;
import com.tarefas.model.Prioridade;
import com.tarefas.model.StatusTarefa;
import com.tarefas.model.TarefaSimples;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TarefaRepository {
    private static final String CABECALHO = "tipo;id;grupoId;titulo;descricao;categoria;prioridade;status;prazo";
    private static TarefaRepository instance;

    private final Path caminhoArquivo;
    private final Map<Integer, ItemTarefa> itensPorId = new LinkedHashMap<>();
    private final List<ItemTarefa> itensRaiz = new ArrayList<>();
    private int proximoId = 1;

    private TarefaRepository() {
        this.caminhoArquivo = Paths.get("dados", "tarefas.csv");
        carregar();
    }

    public static synchronized TarefaRepository getInstance() {
        if (instance == null) {
            instance = new TarefaRepository();
        }
        return instance;
    }

    public List<ItemTarefa> listarRaiz() {
        return new ArrayList<>(itensRaiz);
    }

    public List<ItemTarefa> listarTodos() {
        return new ArrayList<>(itensPorId.values());
    }

    public List<GrupoTarefas> listarGrupos() {
        List<GrupoTarefas> grupos = new ArrayList<>();
        for (ItemTarefa item : itensPorId.values()) {
            if (item instanceof GrupoTarefas grupo) {
                grupos.add(grupo);
            }
        }
        return grupos;
    }

    public ItemTarefa buscarPorId(int id) {
        return itensPorId.get(id);
    }

    public void adicionar(ItemTarefa item) throws IOException {
        item.setId(proximoId++);
        itensPorId.put(item.getId(), item);
        vincularNaArvore(item);
        salvar();
    }

    public void atualizar(ItemTarefa item, Integer grupoAnteriorId) throws IOException {
        Integer novoGrupoId = item.getGrupoId();
        boolean mudouDePai = (grupoAnteriorId == null && novoGrupoId != null)
                || (grupoAnteriorId != null && !grupoAnteriorId.equals(novoGrupoId));

        if (mudouDePai) {
            desvincularDaArvore(item, grupoAnteriorId);
            vincularNaArvore(item);
        }
        salvar();
    }

    public void remover(int id) throws IOException {
        ItemTarefa item = itensPorId.get(id);
        if (item == null) {
            return;
        }
        desvincularDaArvore(item, item.getGrupoId());
        removerRecursivo(item);
        salvar();
    }

    public void salvar() throws IOException {
        Path pasta = caminhoArquivo.getParent();
        if (pasta != null && !Files.exists(pasta)) {
            Files.createDirectories(pasta);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(caminhoArquivo, StandardCharsets.UTF_8)) {
            writer.write(CABECALHO);
            writer.newLine();
            for (ItemTarefa item : itensRaiz) {
                escreverRecursivo(writer, item);
            }
        }
    }

    private void escreverRecursivo(BufferedWriter writer, ItemTarefa item) throws IOException {
        String tipo = item.isGrupo() ? "GRUPO" : "TAREFA";
        String grupoId = item.getGrupoId() == null ? "" : String.valueOf(item.getGrupoId());
        String prazo = item.getPrazo() == null ? "" : item.getPrazo().toString();

        String linha = String.join(";",
                tipo,
                String.valueOf(item.getId()),
                grupoId,
                limparTextoCsv(item.getTitulo()),
                limparTextoCsv(item.getDescricao()),
                limparTextoCsv(item.getCategoria()),
                item.getPrioridade().name(),
                item.getStatus().name(),
                prazo
        );

        writer.write(linha);
        writer.newLine();

        if (item instanceof GrupoTarefas grupo) {
            for (ItemTarefa filho : grupo.getItens()) {
                escreverRecursivo(writer, filho);
            }
        }
    }

    private void carregar() {
        itensPorId.clear();
        itensRaiz.clear();
        proximoId = 1;

        if (!Files.exists(caminhoArquivo)) {
            return;
        }

        try {
            List<String> linhas = Files.readAllLines(caminhoArquivo, StandardCharsets.UTF_8);
            List<ItemTarefa> carregados = new ArrayList<>();

            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i).trim();
                if (linha.isEmpty() || (i == 0 && linha.startsWith("tipo;"))) {
                    continue;
                }

                String[] partes = linha.split(";", -1);
                if (partes.length < 9) {
                    continue;
                }

                ItemTarefa item = montarItem(partes);
                if (item != null) {
                    carregados.add(item);
                    itensPorId.put(item.getId(), item);
                    if (item.getId() >= proximoId) {
                        proximoId = item.getId() + 1;
                    }
                }
            }

            for (ItemTarefa item : carregados) {
                vincularNaArvore(item);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar arquivo CSV: " + e.getMessage());
        }
    }

    private ItemTarefa montarItem(String[] partes) {
        try {
            String tipo = partes[0].trim().toUpperCase();
            int id = Integer.parseInt(partes[1].trim());
            Integer grupoId = partes[2].trim().isEmpty() ? null : Integer.parseInt(partes[2].trim());
            String titulo = partes[3].trim();
            String descricao = partes[4].trim();
            String categoria = partes[5].trim();
            Prioridade prioridade = Prioridade.fromTexto(partes[6]);
            StatusTarefa status = StatusTarefa.fromTexto(partes[7]);
            LocalDate prazo = parseData(partes[8]);

            if ("GRUPO".equals(tipo)) {
                return new GrupoTarefas(id, grupoId, titulo, descricao, categoria, prioridade, status, prazo);
            }
            return new TarefaSimples(id, grupoId, titulo, descricao, categoria, prioridade, status, prazo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseData(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void vincularNaArvore(ItemTarefa item) {
        Integer grupoId = item.getGrupoId();
        if (grupoId != null) {
            ItemTarefa pai = itensPorId.get(grupoId);
            if (pai instanceof GrupoTarefas grupoPai && grupoPai.getId() != item.getId()) {
                grupoPai.adicionar(item);
                return;
            }
            item.setGrupoId(null);
        }
        itensRaiz.add(item);
    }

    private void desvincularDaArvore(ItemTarefa item, Integer grupoId) {
        if (grupoId != null) {
            ItemTarefa pai = itensPorId.get(grupoId);
            if (pai instanceof GrupoTarefas grupoPai) {
                grupoPai.remover(item);
                return;
            }
        }
        itensRaiz.remove(item);
    }

    private void removerRecursivo(ItemTarefa item) {
        if (item instanceof GrupoTarefas grupo) {
            for (ItemTarefa filho : new ArrayList<>(grupo.getItens())) {
                removerRecursivo(filho);
            }
            grupo.limparItens();
        }
        itensPorId.remove(item.getId());
    }

    private String limparTextoCsv(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(";", ",").replace("\r", " ").replace("\n", " ").trim();
    }
}
