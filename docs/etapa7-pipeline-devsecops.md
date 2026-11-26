## Pipeline DevSecOps

### Descrição

Este documento planeja um pipeline DevSecOps para o serviço de transcrição. O pipeline não é implantado: o objetivo é mostrar em quais momentos do ciclo de desenvolvimento entram as atividades de segurança realizadas nas etapas 1 a 6, quais evidências elas produzem e quais verificações impedem o avanço para a etapa seguinte. Os oito momentos seguem o enunciado. Sete deles reaproveitam entregas anteriores; a análise de código e de dependências, ainda não realizada pelo projeto, é apresentada como proposta nas seções seguintes.

---

### 1. Visão geral

O diagrama abaixo relaciona os oito momentos do ciclo e os principais pontos de decisão do pipeline. Cada losango representa uma condição de parada; a saída de correção retorna à implementação segura, enquanto o avanço só ocorre depois que a falha é corrigida ou, em casos excepcionalmente autorizados, que uma decisão de risco fica registrada.

```mermaid
flowchart TB
    PL["1. Planejamento<br/>STRIDE, casos de abuso, riscos"] --> RA["2. Requisitos e arquitetura<br/>RS01 a RS03, DA01 a DA03"]
    RA --> IM["3. Implementação segura<br/>pull request revisado"]
    IM --> TE["4. Testes automatizados<br/>TS01 a TS06 no build"]
    TE --> G1{"P1: algum teste<br/>reprovado?"}
    G1 -->|sim| CO["Corrigir falha"]
    G1 -->|não| AN["5. Análise de código e dependências<br/>gitleaks, SpotBugs, Dependency-Check"]
    AN --> G2{"P4 ou P5: segredo ou<br/>dependência vulnerável?"}
    G2 -->|sim| DE{"Há correção disponível?"}
    DE -->|não| AR["Registrar aceitação formal<br/>de risco com prazo"]
    DE -->|sim| CO
    AR --> DI
    G2 -->|não| DI["6. Teste dinâmico<br/>sessão do ZAP"]
    DI --> G3{"P2 ou P3: chamada divergente<br/>ou alerta sem análise?"}
    G3 -->|sim| CO
    G3 -->|não| IP["7. Implantação<br/>imagem, Trivy, segredos"]
    IP --> G4{"P5: imagem com<br/>vulnerabilidade alta ou crítica?"}
    G4 -->|sim| DE2{"Há correção disponível?"}
    DE2 -->|não| AR
    DE2 -->|sim| CO
    G4 -->|não| MO["8. Monitoramento e resposta<br/>eventos E1 a E11, regras"]
    CO --> IM
    MO -->|"incidente ou ameaça nova"| PL
```

Os momentos 1 a 4 e 6 já possuem alguma evidência no projeto. Os momentos 5 e 7 são propostas desta etapa, porque ainda não há análise automática de código, dependências ou imagem no pipeline atual. O momento 8 está parcialmente implementado no protótipo: hoje, o token recusado (E2) e o acesso negado por propriedade (E5) são registrados.

Como melhoria sugerida ao fluxo, segredos devem ser procurados antes da compilação e no histórico do repositório. As imagens deverão ser promovidas pelo digest, não por uma tag mutável, e uma execução aprovada deverá conservar os relatórios dos scanners, o commit analisado e a decisão registrada para cada gate.

---

### 2. Momentos do pipeline

A tabela segue o modelo solicitado no enunciado. A última coluna estabelece o que precisa ser confirmado antes que o artefato possa avançar ao momento seguinte. A ocorrência de uma condição da seção 4 interrompe o avanço e encaminha o trabalho de volta à implementação, à análise da decisão ou à correção do controle.

| Momento | Atividade de segurança | Evidência produzida | Condição para continuar |
| --- | --- | --- | --- |
| 1. Planejamento | STRIDE, casos de abuso e análise de risco por probabilidade e impacto ([Etapa 1](etapa1-modelagem-ameacas.md), [Etapa 2](etapa2-analise-de-risco.md)) | Tabelas AME1 a AME16, casos CAE1 a CAE7 e registros RI01 a RI16 | Riscos MUITO ALTO e ALTO com tratamento decidido no [plano NIST](etapa2-tratamento-de-risco-nist.md), por redução ou aceitação formal |
| 2. Requisitos e arquitetura | Requisitos de segurança, vulnerabilidades catalogadas e decisões de arquitetura ([Etapa 3](etapa3-arquitetura-segura.md)) | RS01 a RS03, V01 a V03 com CWE, DA01 a DA03 e diagrama da arquitetura segura | Cada risco priorizado ligado a um requisito com critério de aceitação verificável |
| 3. Implementação segura | Sessão somente com segundo fator e autorização por propriedade ([Etapa 4](etapa4-codigo-seguro.md)) | Código do [protótipo](../prototipo) e pull request revisado | Pull request aprovado por integrante diferente daquele que realizou a alteração |
| 4. Testes automatizados | TS01 a TS06 executados no build da imagem e no serviço `testes` do compose | Saída reproduzível do `mvn test` e dos testes do protótipo | Todos os testes passam (P1); o `Dockerfile` impede a geração da imagem quando há falha |
| 5. Análise de código e dependências | Busca de segredos, análise estática e verificação de dependências (seção 3) | Relatórios do gitleaks, do SpotBugs com FindSecBugs e do OWASP Dependency-Check | Nenhum segredo exposto (P4) e nenhuma dependência vulnerável alta ou crítica sem decisão registrada (P5) |
| 6. Teste dinâmico | Sessão do ZAP com roteiro de uso legítimo e abusos ([Etapa 5](etapa5-verificacao-vulnerabilidades.md)) | `zap-relatorio.html`, `alertas.md` e `trafego.log` em [evidencias/etapa-5](../evidencias/etapa-5) | Nenhuma chamada divergente no roteiro (P2), nenhum alerta alto e os médios com análise registrada (P3) |
| 7. Implantação | Varredura da imagem com Trivy, segredos fora do código e da imagem e TLS mútuo no endpoint de status definido por RS03 | Imagem versionada, digest associado ao commit, SBOM e relatório do Trivy | Nenhuma vulnerabilidade alta ou crítica sem decisão registrada (P5), sem execução como usuário root |
| 8. Monitoramento e resposta | Eventos E1 a E11 e regras de detecção ([Etapa 6](etapa6-deteccao-de-intrusoes.md)) | Logs de auditoria, alertas, registro de incidente e histórico de revisão das regras | Alertas tratados conforme a resposta definida na Etapa 6 e regras revisadas após falsos positivos |

A sequência termina apenas em uma imagem aprovada e em monitoramento contínuo. Um incidente observado em operação ou uma ameaça nova reinicia o fluxo no momento 1, atualizando a modelagem de ameaças antes da próxima implementação.

Como melhorias sugeridas ao projeto, os artefatos devem ser associados ao SHA do commit e à versão dos scanners. O momento 7 pode promover uma imagem pelo digest aprovado em vez de uma tag mutável, mantendo o retorno automático para uma versão anterior conhecida como segura.

---

### 3. Análise de código, dependências e imagem

Pendente.

---

### 4. Condições que param o pipeline

Pendente.

---

### 5. Referências

Pendente.
