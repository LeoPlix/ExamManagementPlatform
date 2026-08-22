# Exam Management System (EMS)

Sistema de gestão, segmentação, distribuição e avaliação de exames com correção anónima e gestão do ciclo de revisão de provas.

---

## Pré-requisitos

- **Java 21** (JDK 21) e **Maven**
- **Node.js 18+** e **npm**
- **Docker** e **Docker Compose**

---

## Como Executar o Projeto

Todos os comandos seguintes devem ser executados a partir do diretório `src/`.

### 1. Base de Dados (PostgreSQL)
Iniciar o contentor da base de dados:
```bash
docker compose up -d
```
A base de dados ficará disponível no porto `7654` (`postgres/postgres`, base de dados `emsdb`).

### 2. Backend (Spring Boot)
Num terminal, iniciar a aplicação:
```bash
cd backend
mvn spring-boot:run
```
A API REST ficará acessível em `http://localhost:8080`.

> **Nota:** No arranque da aplicação, a base de dados é automaticamente populada com dados de teste (escolas, disciplinas, utilizadores e uma prova de exemplo em estado inicial).

### 3. Frontend (Vue 3 + Vuetify + Vite)
Num novo terminal, instalar as dependências e iniciar o servidor de desenvolvimento:
```bash
cd frontend
npm install
npm run dev
```
Aceder à aplicação no navegador em `http://localhost:5173`.

---

## Utilizadores e Credenciais de Teste

| Perfil | Email | Palavra-passe | Descrição do Acesso |
|---|---|---|---|
| **Administrador** | `admin@dei.tecnico.ulisboa.pt` | `admin123` | Distribuição global de questões, dashboard de métricas e gestão do sistema |
| **Funcionário (Camões)** | `staff.camoes@dei.tecnico.ulisboa.pt` | `staff123` | Upload de exames, segmentação visual de itens, pauta de notas e publicação (Escola de Camões) |
| **Funcionário (Filipa)** | `staff.filipa@dei.tecnico.ulisboa.pt` | `staff123` | Upload de exames, segmentação visual de itens, pauta de notas e publicação (Escola D. Filipa) |
| **Professor (Matemática)** | `prof.mat@dei.tecnico.ulisboa.pt` | `teacher123` | Correção cega com anotações em canvas e reavaliação de revisões |
| **Professor (FQA)** | `prof.fqa@dei.tecnico.ulisboa.pt` | `teacher123` | Avaliação docente na área de Física e Química A |
| **Professor (Português)** | `prof.port@dei.tecnico.ulisboa.pt` | `teacher123` | Avaliação docente na área de Português |
| **Aluno** | `aluno1@dei.tecnico.ulisboa.pt` | `student123` | Consulta da prova (original/corrigida), notas e submissão de pedidos de revisão |
| **Alunos adicionais** | `aluno2@dei.tecnico.ulisboa.pt` a `aluno10@dei.tecnico.ulisboa.pt` | `student123` | Contas de alunos adicionais para testes de escala |

---

## Roteiro de Teste Manual (Fluxo Completo)

Para testar o ciclo de vida completo de uma prova no sistema, seguir a sequência abaixo:

### Passo 1: Segmentação de Itens da Prova (Staff)
1. Iniciar sessão com a conta de **Staff** (`staff.camoes@dei.tecnico.ulisboa.pt` / `staff123`).
2. Aceder ao menu **Gestão de Exames**.
3. Na prova de demonstração (Matemática A), clicar na ação de recorte/segmentação de perguntas:
   - Na página 1, selecionar a área retangular correspondente à primeira questão (ex.: cotação `10.0`) e clicar em **Adicionar Pergunta**.
   - Na página 2, selecionar a área correspondente à segunda questão (ex.: cotação `10.0`) e clicar em **Adicionar Pergunta**.
   - Garantir que a soma das cotações totaliza `20.0` valores e clicar em **Concluir Segmentação**.

### Passo 2: Distribuição Anónima de Questões (Administrador)
1. Terminar sessão e entrar como **Administrador** (`admin@dei.tecnico.ulisboa.pt` / `admin123`).
2. Aceder ao menu **Distribuição de Provas**.
3. Clicar em **Distribuir Todas as Provas** para distribuir os itens segmentados de forma anónima pelos docentes da respetiva disciplina.
4. Aceder ao **Dashboard Estatístico** para verificar as métricas agregadas.

### Passo 3: Correção Cega com Anotações em Canvas (Docente)
1. Terminar sessão e entrar como **Professor de Matemática** (`prof.mat@dei.tecnico.ulisboa.pt` / `teacher123`).
2. Aceder ao menu **Avaliação Docente**.
3. Clicar em **Avaliar Resposta** sobre um dos fragmentos atribuídos:
   - Utilizar as ferramentas de anotação no canvas (caneta, visto, cruz ou texto) e os controlos de zoom.
   - Inserir a nota atribuída e o comentário pedagógico.
   - Clicar em **Submeter Classificação**.
4. Repetir o processo para o segundo item para finalizar a avaliação da prova.

### Passo 4: Publicação de Notas e Autorização de Consulta (Staff)
1. Entrar novamente como **Staff** (`staff.camoes@dei.tecnico.ulisboa.pt` / `staff123`).
2. Aceder ao menu **Pauta de Classificações**:
   - Clicar em **Publicar Notas** para disponibilizar as classificações numéricas e iniciar o período de revisão de 48 horas.
   - Clicar em **Publicar Provas Corrigidas** (ou autorizar individualmente pedidos de visualização) para disponibilizar o exame com anotações.

### Passo 5: Consulta da Prova e Pedido de Revisão (Aluno)
1. Iniciar sessão como **Aluno** (`aluno1@dei.tecnico.ulisboa.pt` / `student123`).
2. Aceder ao menu **As Minhas Provas**.
3. Clicar na prova para abrir o visualizador:
   - Alternar entre a resolução original e a versão com as anotações do docente.
   - Clicar em **Pedir Revisão** num dos itens, preencher a fundamentação e submeter.

### Passo 6: Reavaliação e Homologação de Notas (Docente e Staff)
1. Entrar com o docente responsável pela revisão para analisar a fundamentação, reavaliar o item e emitir parecer.
2. Entrar como **Staff**, aceder à **Pauta de Classificações** e clicar em **Lançar Notas de Revisão** para homologar as notas finais revistas.

---

## Testes Automatizados
 
### Backend (Testes Unitários e de Integração)
```bash
cd backend
mvn test
```

### Frontend (Verificação de Tipos e Linting)
```bash
cd frontend
npm run type-check
npm run lint
```
