package com.tarefas.observer;

public interface TarefaSubject {
    void adicionarObserver(TarefaObserver observer);
    void removerObserver(TarefaObserver observer);
    void notificarObservers();
}
