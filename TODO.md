# TODO

## Estado em 27/09

- Etapa 1 (modelagem de ameaças) entregue: 16 ameaças STRIDE e 7 casos de abuso em [docs/etapa1-modelagem-ameacas.md](docs/etapa1-modelagem-ameacas.md).
- Etapa 2, análise de risco: registro reformulado com mapeamento 1:1 ameaça-risco (RI01 a RI16), coluna de nota (probabilidade × impacto), justificativas por risco e priorização em [docs/etapa2-analise-de-risco.md](docs/etapa2-analise-de-risco.md).
- Etapa 2, tratamento: plano em [docs/etapa2-tratamento-de-risco-nist.md](docs/etapa2-tratamento-de-risco-nist.md) cobre os 16 riscos.

Referências de formato: [exemplo do professor](https://github.com/sequincozes/exemplo-atividade-es-seguro) (Passos 4 a 6) e [VitaLink](https://github.com/camillabdt/VitaLink) (registro consolidado, justificativas e matriz NIST).

## Pendências da Etapa 2

### Gabriel

- Tabela de estratégia escolhida por risco, com nível inicial e justificativa (seção 9.2 do exemplo do professor), no doc de tratamento.
- Matriz risco × funções NIST CSF 2.0 (seção 9.3 do exemplo), no doc de tratamento.
- Ordem inicial de implementação no doc de tratamento (seção 9.5 do exemplo), agrupando controles e indicando dependências, refletindo as linhas em Aceitar (RI07, RI10, RI14) e os residuais MÉDIO (RI06, RI08, RI13).
- Conclusão da análise no doc de análise (seção 8.7 do exemplo) e conclusão do tratamento no doc NIST (seção 9.7).
- Revisão geral das ameaças (Etapa 1) e do doc de tratamento, commitando as correções encontradas.

### Carlos e Alexandre

- Revisar o PR da reformulação da análise de riscos, validando as notas de probabilidade e impacto dos riscos RI05 a RI16, as notas alteradas na revisão (RI03, RI06, RI07, RI12) e os residuais do plano (RI06, RI07, RI08, RI13).
- Validar no plano de tratamento os limites numéricos iniciais (sessão, solicitações, chat, retenção, alerta de Storage), as premissas externas declaradas (provedor de identidade, cofre de chaves, decodificação no Módulo I.A.) e a decisão de aceitar o RI07 em MUITO ALTO.
