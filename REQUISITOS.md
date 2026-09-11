## Elicitação de Requisitos

Documento usado para listar os requisitos levantados.

**Requisitos Funcionais**

* RQF1 - Permitir um promotor do estado realizar acesso com suas credenciais

* RQF2 - Usuário pode pesquisar mídias disponíveis de um processo

* RQF3 - Usuário seleciona a mídia desejada para transcrição

* RQF4 - Usuário pode acompanhar o status da transcrição da mídia (assíncrono)

* RQF5 - Usuário pode visualizar as transcrições já realizadas agrupadas por processo

* RQF6 - Usuário pode acessar uma transcrição com status "finalizada"

* RQF7 - Usuário pode selecionar uma transcrição finalizada para usar como contexto na interação com a Inteligência Artificial disponível no sistema

* RQF8 - Usuário pode fazer o download da transcrição em formato .docx

* RQF9 - Usuário pode deletar uma transcrição da sua lista de transcrições

---

**Requisitos Não Funcionais**

* RQNF1 - Autenticação/Autorização: Controlar acesso ao programa
* RQNF2 - Privacidade: Proteger dados conforme lgpd
* RQNF3 - Desempenho:
    * RQNF3.1 - O sistema deve permitir o upload de mídias até 80MB;
    * RQNF3.2 - O tempo máximo da transcrição de uma mídia deve ser de 1 hora;
    * RQNF3.3 - O chat com a I.A não deve salvar o contexto da conversar quando o usuário encerrar a mesma;
    * RQNF3.4 - Ao iniciar um chat com a I.A, a transcrição da mídia deve ser automáticamente passada como contexto.
* RQNF4 - Escabilidade:
    * RQNF4.1 - O sistema deve permitir uploads simultâneos de diversos usuários;
    * RQNF4.2 - O sistema deve permitir um usuário enviar mais de uma mídia para transcrição;
    * RQNF4.3 - O sistema deve suportar uma média de 2.000 transcrições diárias;
    * RQNF4.4 - O sistema de filas deve possuir duas filas para transcrições, sendo uma fila para mídas grandes e outra fila para mídias menores e com transcrição rápida.
* RQNF5 - Confiabilidade: falhas na IA podem causar duplo sentido ou mudar o sentido das transcrições
* RQNF6 - Integridade: garantir que midias originais nao sofram alterações
* RQNF7 - Auditoria: manter logs das operações feitas
* RQNF8 - Usabilidade: interface intuitiva
* RQNF9 - Manutenibilidade: sistema deve ser fácil de se manter
