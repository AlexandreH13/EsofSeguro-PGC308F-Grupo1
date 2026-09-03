## Descrição do Serviço Usado como Case

### Serviço de transcrição de mídias para um Ministério Público do Estado

**O que é**

Um serviço que recebe mídias relacionadas a processos e realiza a transcrição das mesmas. Essas mídias podem ser gravações de depoimentos, oitivas e inquéritos policiais.

**Como faz**

O usuário consulta um determinado processo dentro de uma plataforma e procura pelas mídias disponíveis. Seleciona a mídia que deseja transcrever. Um upload da mídia é feito para outro serviço de storage do cliente, e uma mensagem é enviada para uma fila para iniciar a transcrição. A transcrição é feita por uma I.A e disponibilizada para o cliente dentro desse mesmo serviço.

**Para que serve**

O serviço é usado para obter uma versão transcrita (textual) de mídias de longas duração, e permitir a interação dessa versão transcrita com I.As.

**Para quem**

Usado por promotores de justiça do estado.
