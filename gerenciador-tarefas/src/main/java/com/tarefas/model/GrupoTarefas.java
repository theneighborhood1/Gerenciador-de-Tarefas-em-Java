package com.tarefas.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrupoTarefas implements ItemTarefa {
    private int id;
    private Integer grupoId;
    private String titulo;
    private String descricao;
    private String categoria;
    private Prioridade prioridade;
    private StatusTarefa status;
    private LocalDate prazo;
    private final List<ItemTarefa> itens = new ArrayList<>();

    public GrupoTarefas(int id, Integer grupoId, String titulo, String descricao,
                        String categoria, Prioridade prioridade, StatusTarefa status, LocalDate prazo) {
        this.id = id;
        this.grupoId = grupoId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.prioridade = prioridade;
        this.status = status;
        this.prazo = prazo;
    }

    public void adicionar(ItemTarefa item) {
        item.setGrupoId(this.id);
        itens.add(item);
    }

    public void remover(ItemTarefa item) {
        itens.remove(item);
    }

    public List<ItemTarefa> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void limparItens() {
        itens.clear();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Integer getGrupoId() {
        return grupoId;
    }

    @Override
    public void setGrupoId(Integer grupoId) {
        this.grupoId = grupoId;
    }

    @Override
    public String getTitulo() {
        return titulo;
    }

    @Override
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    @Override
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String getCategoria() {
        return categoria;
    }

    @Override
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public Prioridade getPrioridade() {
        return prioridade;
    }

    @Override
    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    @Override
    public StatusTarefa getStatus() {
        if (itens.isEmpty()) {
            return status;
        }
        int total = contarTarefas();
        int concluidas = contarConcluidas();
        if (total > 0 && concluidas == total) {
            return StatusTarefa.CONCLUIDA;
        }
        boolean algumaIniciada = false;
        for (ItemTarefa item : itens) {
            if (item.getStatus() != StatusTarefa.PENDENTE) {
                algumaIniciada = true;
                break;
            }
        }
        return algumaIniciada ? StatusTarefa.EM_ANDAMENTO : StatusTarefa.PENDENTE;
    }

    @Override
    public void setStatus(StatusTarefa status) {
        this.status = status;
        if (status == StatusTarefa.CONCLUIDA) {
            for (ItemTarefa item : itens) {
                item.setStatus(StatusTarefa.CONCLUIDA);
            }
        }
    }

    @Override
    public LocalDate getPrazo() {
        return prazo;
    }

    @Override
    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    @Override
    public boolean isGrupo() {
        return true;
    }

    @Override
    public int contarTarefas() {
        int total = 0;
        for (ItemTarefa item : itens) {
            total += item.contarTarefas();
        }
        return total;
    }

    @Override
    public int contarConcluidas() {
        int total = 0;
        for (ItemTarefa item : itens) {
            total += item.contarConcluidas();
        }
        return total;
    }

    @Override
    public double calcularProgresso() {
        int total = contarTarefas();
        if (total == 0) {
            return status == StatusTarefa.CONCLUIDA ? 100.0 : 0.0;
        }
        return (contarConcluidas() * 100.0) / total;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
