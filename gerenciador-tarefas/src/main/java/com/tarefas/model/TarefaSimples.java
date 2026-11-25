package com.tarefas.model;

import java.time.LocalDate;

public class TarefaSimples implements ItemTarefa {
    private int id;
    private Integer grupoId;
    private String titulo;
    private String descricao;
    private String categoria;
    private Prioridade prioridade;
    private StatusTarefa status;
    private LocalDate prazo;

    public TarefaSimples(int id, Integer grupoId, String titulo, String descricao,
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
        return status;
    }

    @Override
    public void setStatus(StatusTarefa status) {
        this.status = status;
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
        return false;
    }

    @Override
    public int contarTarefas() {
        return 1;
    }

    @Override
    public int contarConcluidas() {
        return status == StatusTarefa.CONCLUIDA ? 1 : 0;
    }

    @Override
    public double calcularProgresso() {
        if (status == StatusTarefa.CONCLUIDA) {
            return 100.0;
        }
        if (status == StatusTarefa.EM_ANDAMENTO) {
            return 50.0;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
