## Descrição do Serviço Usado como Case

### Serviço de transcrição de mídias para um Ministério Público do Estado

---

**O que é**

Um serviço que recebe mídias relacionadas a processos e realiza a transcrição das mesmas. Essas mídias podem ser gravações de depoimentos, oitivas e inquéritos policiais.

---

**Como faz**

O usuário consulta um determinado processo dentro de uma plataforma e procura pelas mídias disponíveis. Seleciona a mídia que deseja transcrever. Um upload da mídia é feito para outro serviço de storage do cliente, e uma mensagem é enviada para uma fila para iniciar a transcrição. A transcrição é feita por uma I.A e disponibilizada para o cliente dentro desse mesmo serviço.

---

**Para que serve**

O serviço é usado para obter uma versão transcrita (textual) de mídias de longas duração, e permitir a interação dessa versão transcrita com I.As.

---

**Para quem**

Usado por toda promotoria do estado. Todos interessados que possuem acesso a mídias (áudios e vídeos) relacionados a depoimentos e inquéritos.

---

**High-level design da aplicação**

A tabela abaixo descreve cada componente ilustrado no digrama high-level da aplicação.

| Componente | Descrição |
| --- | --- |
| Backend | Serviço web que recebe requisições do usuário. Interage com API que busca informações dos processos e envia para a fila a solicitação de transcrição |
| Integração | API que realiza integração com serviço terceiro que possui os dados dos processos e as mídias armazenadas |
| Sistema Processos | Sistema do Estado com as informações dos processos e mídias |
| Storage | Serviço de armazenamento na nuvem que vai persistir as mídias e suas transcrições |
| Enfileiramento | Sistema de filas para permitir o processamento assíncrono |
| Módulo I.A | Serviço python que "escuta" a fila e gerencia as requisições de transcrição. Envia a mídia para a I.A degravar e formata a saída para persistir a transcrição no formato ideal.
| I.A | Serviço de I.A utilizado via API |

![hld](resources/hldesign.png)
