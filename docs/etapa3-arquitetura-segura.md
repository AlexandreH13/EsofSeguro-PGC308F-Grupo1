## Estratégias de Arquitetura para Mitigação dos Riscos

Neste documento será descrito as decisões arquiteturais que antecedem a implementação como forma de prevenção ou minimização de riscos associados ao uso da ferramenta. Abaixo estão três tabelas que apresentam informações sobre a arquitetura proposta. Na primeira tabela estão os requisitos de segurança para a arquitetura, isto é, ela responde o que determinado componente da arquitetura deve fazer para mitigar seu risco associado? Na segunda tabela são apresentadas as vulnerabilidades candidatas de cada requisito da tabela anterior. Já a terceira tabela trata das decisões arquiteturais.

### Requisitos de segurança para a arquiteutura proposta

| ID | Requisito Verificável | Origem |
| --- | --- | --- |
| RS01 | EXEMPLO: O que o servidor ou outro componente da arquitetura deve quando outro promotor tentar acessar a transcrição de outro promotor? (Aqui devemos responder essa pergunta, exemplo, servidor deve verificar a identidade)| AME1, RI01 (Aqui são os riscos e ameaças associadas) |

### Vulnerabilidades associadas

| ID | Vulnerabilidade associada | Requisito que trata | Ameaças |
| --- | --- | --- | --- |
| V01 | EXEMPLO: Falta de verificação da identidade do usuário promotor | RS01 | AME1 |

### Decisões arquiteturais

| ID | Decisão | Rastreabilidade | Critério de aceitação |
| --- | --- | --- | --- |
| DA01 | Qual a decisão arquitetural para cumprir o requisito de segurança associado? (Devemos responder essa pergunta aqui) | RS01, V01, AME1 | Qual teste deve ser feito |