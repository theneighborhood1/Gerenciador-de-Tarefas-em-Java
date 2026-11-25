package com.tarefas.controller;

import com.tarefas.model.GrupoTarefas;
import com.tarefas.model.ItemTarefa;
import com.tarefas.model.Prioridade;
import com.tarefas.model.StatusTarefa;
import com.tarefas.model.TarefaSimples;
import com.tarefas.observer.TarefaObserver;
import com.tarefas.observer.TarefaSubject;
import com.tarefas.repository.TarefaRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TarefaController implements TarefaSubject {
    private final TarefaRepository repository;
    private final List<TarefaObserver> observers = new ArrayList<>();

    public TarefaController() {
        this.repository = TarefaRepository.getInstance();
    }

    @Override
    public void adicionarObserver(TarefaObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removerObserver(TarefaObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notificarObservers() {
        for (TarefaObserver observer : observers) {
            observer.atualizar();
        }
    }

    public void cadastrarTarefa(Integer grupoId, String titulo, String descricao, String categoria,
                                Prioridade prioridade, StatusTarefa status, LocalDate prazo) throws IOException {
        validarObrigatorios(titulo, categoria);
        validarGrupoDestino(null, grupoId);

        TarefaSimples tarefa = new TarefaSimples(
                0,
                grupoId,
                titulo.trim(),
                descricao == null ? "" : descricao.trim(),
                categoria.trim(),
                prioridade == null ? Prioridade.MEDIA : prioridade,
                status == null ? StatusTarefa.PENDENTE : status,
                prazo
        );

        repository.adicionar(tarefa);
        notificarObservers();
    }

    public void cadastrarGrupo(Integer grupoPaiId, String titulo, String descricao, String categoria,
                               Prioridade prioridade, LocalDate prazo) throws IOException {
        validarObrigatorios(titulo, categoria);
        validarGrupoDestino(null, grupoPaiId);

        GrupoTarefas grupo = new GrupoTarefas(
                0,
                grupoPaiId,
                titulo.trim(),
                descricao == null ? "" : descricao.trim(),
                categoria.trim(),
                prioridade == null ? Prioridade.MEDIA : prioridade,
                StatusTarefa.PENDENTE,
                prazo
        );

        repository.adicionar(grupo);
        notificarObservers();
    }

    public void editarItem(int id, Integer novoGrupoId, String titulo, String descricao, String categoria,
                           Prioridade prioridade, StatusTarefa status, LocalDate prazo) throws IOException {
        validarObrigatorios(titulo, categoria);
        ItemTarefa item = repository.buscarPorId(id);
        if (item == null) {
            throw new IllegalArgumentException("Item não encontrado para edição.");
        }
        validarGrupoDestino(item, novoGrupoId);

        Integer grupoAnteriorId = item.getGrupoId();
        item.setGrupoId(novoGrupoId);
        item.setTitulo(titulo.trim());
        item.setDescricao(descricao == null ? "" : descricao.trim());
        item.setCategoria(categoria.trim());
        item.setPrioridade(prioridade == null ? Prioridade.MEDIA : prioridade);
        if (status != null) {
            item.setStatus(status);
        }
        item.setPrazo(prazo);

        repository.atualizar(item, grupoAnteriorId);
        notificarObservers();
    }

    public void alternarConcluida(int id) throws IOException {
        ItemTarefa item = repository.buscarPorId(id);
        if (item == null) {
            throw new IllegalArgumentException("Item não encontrado.");
        }

        if (item.getStatus() == StatusTarefa.CONCLUIDA) {
            item.setStatus(StatusTarefa.PENDENTE);
        } else {
            item.setStatus(StatusTarefa.CONCLUIDA);
        }

        repository.salvar();
        notificarObservers();
    }

    public void removerItem(int id) throws IOException {
        if (repository.buscarPorId(id) == null) {
            throw new IllegalArgumentException("Item não encontrado para remoção.");
        }
        repository.remover(id);
        notificarObservers();
    }

    public List<ItemTarefa> listarRaiz() {
        return repository.listarRaiz();
    }

    public List<GrupoTarefas> listarGrupos() {
        return repository.listarGrupos();
    }

    public ItemTarefa buscarPorId(int id) {
        return repository.buscarPorId(id);
    }

    public List<String> listarCategorias() {
        Set<String> categorias = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ItemTarefa item : repository.listarTodos()) {
            if (item.getCategoria() != null && !item.getCategoria().isBlank()) {
                categorias.add(item.getCategoria());
            }
        }
        return new ArrayList<>(categorias);
    }

    public List<ItemTarefa> filtrar(String termo, String categoria, Prioridade prioridade, StatusTarefa status) {
        String busca = termo == null ? "" : termo.trim().toLowerCase();
        List<ItemTarefa> resultado = new ArrayList<>();

        for (ItemTarefa raiz : repository.listarRaiz()) {
            ItemTarefa filtrado = filtrarNo(raiz, busca, categoria, prioridade, status);
            if (filtrado != null) {
                resultado.add(filtrado);
            }
        }
        return resultado;
    }

    private ItemTarefa filtrarNo(ItemTarefa item, String busca, String categoria,
                                 Prioridade prioridade, StatusTarefa status) {
        boolean corresponde = atendeFiltro(item, busca, categoria, prioridade, status);

        if (item instanceof GrupoTarefas grupo) {
            List<ItemTarefa> filhosFiltrados = new ArrayList<>();
            for (ItemTarefa filho : grupo.getItens()) {
                ItemTarefa f = filtrarNo(filho, busca, categoria, prioridade, status);
                if (f != null) {
                    filhosFiltrados.add(f);
                }
            }

            if (corresponde || !filhosFiltrados.isEmpty()) {
                GrupoTarefas copia = new GrupoTarefas(
                        grupo.getId(),
                        grupo.getGrupoId(),
                        grupo.getTitulo(),
                        grupo.getDescricao(),
                        grupo.getCategoria(),
                        grupo.getPrioridade(),
                        grupo.getStatus(),
                        grupo.getPrazo()
                );
                List<ItemTarefa> filhosParaExibir = filhosFiltrados.isEmpty() && corresponde
                        ? grupo.getItens()
                        : filhosFiltrados;
                for (ItemTarefa filho : filhosParaExibir) {
                    copia.adicionar(filho);
                }
                return copia;
            }
            return null;
        }

        return corresponde ? item : null;
    }

    private boolean atendeFiltro(ItemTarefa item, String busca, String categoria,
                                 Prioridade prioridade, StatusTarefa status) {
        if (!busca.isEmpty()) {
            String titulo = item.getTitulo() == null ? "" : item.getTitulo().toLowerCase();
            String descricao = item.getDescricao() == null ? "" : item.getDescricao().toLowerCase();
            if (!titulo.contains(busca) && !descricao.contains(busca)) {
                return false;
            }
        }

        if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("Todas")) {
            if (!categoria.equalsIgnoreCase(item.getCategoria())) {
                return false;
            }
        }

        if (prioridade != null && item.getPrioridade() != prioridade) {
            return false;
        }

        if (status != null && item.getStatus() != status) {
            return false;
        }

        return true;
    }

    public int getTotalTarefas() {
        int total = 0;
        for (ItemTarefa item : repository.listarRaiz()) {
            total += item.contarTarefas();
        }
        return total;
    }

    public int getTotalConcluidas() {
        int total = 0;
        for (ItemTarefa item : repository.listarRaiz()) {
            total += item.contarConcluidas();
        }
        return total;
    }

    public int getTotalPendentes() {
        return getTotalTarefas() - getTotalConcluidas();
    }

    public int getTotalPrioridadeAlta() {
        int total = 0;
        for (ItemTarefa item : repository.listarTodos()) {
            if (!item.isGrupo()
                    && item.getPrioridade() == Prioridade.ALTA
                    && item.getStatus() != StatusTarefa.CONCLUIDA) {
                total++;
            }
        }
        return total;
    }

    public double getProgressoGeral() {
        int total = getTotalTarefas();
        if (total == 0) {
            return 0.0;
        }
        return (getTotalConcluidas() * 100.0) / total;
    }

    private void validarObrigatorios(String titulo, String categoria) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("O título é obrigatório.");
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }
    }

    private void validarGrupoDestino(ItemTarefa itemAtual, Integer grupoDestinoId) {
        if (grupoDestinoId == null) {
            return;
        }
        ItemTarefa destino = repository.buscarPorId(grupoDestinoId);
        if (!(destino instanceof GrupoTarefas)) {
            throw new IllegalArgumentException("O grupo selecionado não existe.");
        }
        if (itemAtual != null && itemAtual.getId() == grupoDestinoId) {
            throw new IllegalArgumentException("Um grupo não pode ser movido para dentro de si mesmo.");
        }
        if (itemAtual instanceof GrupoTarefas grupoAtual && ehDescendente(grupoAtual, grupoDestinoId)) {
            throw new IllegalArgumentException("Não é possível mover um grupo para dentro de uma subtarefa dele.");
        }
    }

    private boolean ehDescendente(GrupoTarefas grupo, int idProcurado) {
        for (ItemTarefa filho : grupo.getItens()) {
            if (filho.getId() == idProcurado) {
                return true;
            }
            if (filho instanceof GrupoTarefas subgrupo && ehDescendente(subgrupo, idProcurado)) {
                return true;
            }
        }
        return false;
    }
}
