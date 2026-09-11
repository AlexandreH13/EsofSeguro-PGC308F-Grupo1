## Elicitação de Requisitos

Documento usado para listar os requisitos levantados.

**Requisitos Funcionais**

* RQF1 - Permitir um promotor do estado realizar acesso com suas credenciais (Dúvida)

* RQF2 - Usuário pode pesquisar mídias disponíveis de um processo

* RQF3 - Usuário seleciona a mídia desejada para transcrição

* RQF4 - Usuário pode acompanhar o status da transcrição da mídia (assíncrono)

* RQF5 - Usuário pode visualizar as transcrições já realizadas agrupadas por processo

* RQF6 - Usuário pode acessar uma transcrição com status "finalizada"

* RQF7 - Usuário pode selecionar uma transcrição finalizada para usar como contexto na interação com a Inteligência Artificial disponível no sistema

* RQF8 - Usuário pode fazer o download da transcrição em formato .docx

**Requisitos Não Funcionais**

* Autenticação/Autorização: Controlar acesso ao programa
* Privacidade: Proteger dados conforme lgpd
* Desempenho: Consultas e navegação devem ter baixa latência
* Escabilidade: suportar uploads simultâneos de vários promotores
* Confiabilidade: falhas na IA podem causar duplo sentido ou mudar o sentido das transcrições
* Integridade: garantir que midias originais nao sofram alterações
* Auditoria: manter logs das operações feitas
* Usabilidade: interface intuitiva
* Manutenibilidade: sistema deve ser fácil de se manter
