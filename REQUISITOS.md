## Elicitação de Requisitos

Documento usado para listar os requisitos levantados. Cada requisito funcional e não funcional está identificado por um código único.

---

**Requisitos Funcionais**

| ID | Descrição |
| --- | --- |
| RQF1 | Permitir um promotor do estado realizar acesso com suas credenciais |
| RQF2 | Usuário pode pesquisar mídias disponíveis de um processo |
| RQF3 | Usuário seleciona a mídia desejada para transcrição |
| RQF4 | Usuário pode acompanhar o status da transcrição da mídia (assíncrono) |
| RQF5 | Usuário pode visualizar as transcrições já realizadas agrupadas por processo |
| RQF6 | Usuário pode acessar uma transcrição com status "finalizada" |
| RQF7 | Usuário pode selecionar uma transcrição finalizada para usar como contexto na interação com a Inteligência Artificial disponível no sistema |
| RQF8 | Usuário pode fazer o download da transcrição em formato .docx |
| RQF9 | Usuário pode deletar uma transcrição da sua lista de transcrições |
| RQF10 | Usuário pode enviar perguntas à Inteligência Artificial e receber respostas baseadas na transcrição selecionada como contexto |
| RQF11 | Usuário pode encerrar sua sessão no sistema |

---

**Requisitos Não Funcionais**

| ID | Nome | Descrição |
| --- | --- | --- |
| **RQNF1** | **Autenticação/Autorização** | Permitir um promotor do estado realizar acesso com suas credenciais |
| **RQNF2** | **Privacidade** | Usuário pode pesquisar mídias disponíveis de um processo |
| **RQNF3** | **Desempenho** | Usuário seleciona a mídia desejada para transcrição |
| RQNF3.1 | Desempenho | O sistema deve permitir o upload de mídias até 80MB |
| RQNF3.2 | Desempenho | O tempo máximo da transcrição de uma mídia deve ser de 1 hora |
| RQNF3.3 | Desempenho | O chat com a I.A não deve salvar o contexto da conversar quando o usuário encerrar a mesma |
| RQNF3.4 | Desempenho | Ao iniciar um chat com a I.A, a transcrição da mídia deve ser automáticamente passada como contexto |
| **RQNF4** | **Escabilidade** | Usuário pode acompanhar o status da transcrição da mídia (assíncrono) |
| RQNF4.1 | Escabilidade | O sistema deve permitir uploads simultâneos de diversos usuários |
| RQNF4.2 | Escabilidade | O sistema deve permitir um usuário enviar mais de uma mídia para transcrição |
| RQNF4.3 | Escabilidade | O sistema deve suportar uma média de 2.000 transcrições diárias |
| RQNF4.4 | Escabilidade | O sistema de filas deve possuir duas filas para transcrições, sendo uma fila para mídas grandes e outra fila para mídias menores e com transcrição rápida |
| **RQNF5** | **Confiabilidade** | Usuário pode visualizar as transcrições já realizadas agrupadas por processo |
| **RQNF6** | **Integridade** | Usuário pode acessar uma transcrição com status "finalizada" |
| **RQNF7** | **Auditoria** | Usuário pode selecionar uma transcrição finalizada para usar como contexto na interação com a Inteligência Artificial disponível no sistema |
| **RQNF8** | **Usabilidade** | Usuário pode fazer o download da transcrição em formato .docx |
| **RQNF9** | **Manutenibilidade** | Usuário pode deletar uma transcrição da sua lista de transcrições |
