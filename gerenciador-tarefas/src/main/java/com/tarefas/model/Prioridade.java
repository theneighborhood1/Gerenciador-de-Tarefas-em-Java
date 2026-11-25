package com.tarefas.model;

public enum Prioridade {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta");

    private final String descricao;

    Prioridade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Prioridade fromTexto(String texto) {
        if (texto == null) {
            return MEDIA;
        }
        String limpo = texto.trim().toUpperCase();
        for (Prioridade p : values()) {
            if (p.name().equals(limpo) || p.descricao.toUpperCase().equals(limpo)) {
                return p;
            }
        }
        return MEDIA;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
