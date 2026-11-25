package com.tarefas.model;

public enum StatusTarefa {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída");

    private final String descricao;

    StatusTarefa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static StatusTarefa fromTexto(String texto) {
        if (texto == null) {
            return PENDENTE;
        }
        String limpo = texto.trim().toUpperCase();
        for (StatusTarefa s : values()) {
            if (s.name().equals(limpo) || s.descricao.toUpperCase().equals(limpo)) {
                return s;
            }
        }
        return PENDENTE;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
