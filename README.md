[README.md](https://github.com/user-attachments/files/35441360/README.md)
# Gerenciador de Tarefas — POO II

Sistema desktop em Java com interface gráfica em **JavaFX** desenvolvido para o Trabalho IV de **Programação Orientada a Objetos II** (Ciência da Computação – 4° Período).

**Integrantes:** Paulo Cesar, Henry e Israel  
**Professor:** Márcio Alves de Araújo

## Funcionalidades

- Cadastro, listagem em árvore, edição, remoção e busca de tarefas e grupos de tarefas.
- Organização hierárquica de subtarefas dentro de grupos, com cálculo automático de progresso.
- Classificação por categoria, prioridade (`Baixa`, `Média`, `Alta`), status (`Pendente`, `Em andamento`, `Concluída`) e prazo.
- Filtros combinados por texto, categoria, prioridade e status.
- Painel de resumo atualizado em tempo real.
- Persistência automática em arquivo CSV (`dados/tarefas.csv`).

## Padrões de Projeto Aplicados

- **Singleton (Criacional):** `TarefaRepository` garante uma única instância responsável pelo estado em memória e pelo acesso ao arquivo CSV.
- **Composite (Estrutural):** `ItemTarefa`, `TarefaSimples` e `GrupoTarefas` permitem tratar tarefas individuais e grupos de subtarefas de forma uniforme.
- **Observer (Comportamental):** `TarefaSubject` (`TarefaController`) notifica automaticamente os observadores (`TelaPrincipal` e `PainelResumo`) sempre que há alteração nos dados.

## Estrutura de Pacotes

```text
src/main/java/com/tarefas/
├── Launcher.java
├── MainApp.java
├── model/
│   ├── GrupoTarefas.java
│   ├── ItemTarefa.java
│   ├── Prioridade.java
│   ├── StatusTarefa.java
│   └── TarefaSimples.java
├── observer/
│   ├── TarefaObserver.java
│   └── TarefaSubject.java
├── repository/
│   └── TarefaRepository.java
├── controller/
│   └── TarefaController.java
└── view/
    ├── DialogoTarefa.java
    ├── PainelResumo.java
    └── TelaPrincipal.java
```

## Como Executar

### Pré-requisitos
- **JDK 17** ou superior instalado.
- **IntelliJ IDEA** (recomendado) ou **Apache Maven** 3.8+.

### Pelo IntelliJ IDEA
1. Extraia a pasta do projeto e abra-a no IntelliJ IDEA (`File > Open` selecionando a pasta `gerenciador-tarefas` ou o `pom.xml`).
2. Aguarde o Maven baixar a dependência do JavaFX automaticamente.
3. Confirme em `File > Project Structure` que o SDK está configurado para **JDK 17** (ou superior).
4. Abra `src/main/java/com/tarefas/Launcher.java` e clique no botão **Run** (ícone verde de Play).

### Pelo Terminal (Maven)
```bash
mvn clean javafx:run
```

## Onde os Dados são Gravados

Os dados são salvos automaticamente no arquivo `dados/tarefas.csv`, localizado na raiz do projeto. O repositório já acompanha dados de exemplo para demonstração e pode ser editado diretamente pelo sistema; todas as alterações permanecem gravadas ao fechar e abrir o programa novamente.
