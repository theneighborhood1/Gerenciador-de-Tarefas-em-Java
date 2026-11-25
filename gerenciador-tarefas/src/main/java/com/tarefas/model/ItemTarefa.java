package com.tarefas.model;

import java.time.LocalDate;

public interface ItemTarefa {
    int getId();
    void setId(int id);

    Integer getGrupoId();
    void setGrupoId(Integer grupoId);

    String getTitulo();
    void setTitulo(String titulo);

    String getDescricao();
    void setDescricao(String descricao);

    String getCategoria();
    void setCategoria(String categoria);

    Prioridade getPrioridade();
    void setPrioridade(Prioridade prioridade);

    StatusTarefa getStatus();
    void setStatus(StatusTarefa status);

    LocalDate getPrazo();
    void setPrazo(LocalDate prazo);

    boolean isGrupo();
    int contarTarefas();
    int contarConcluidas();
    double calcularProgresso();
}
