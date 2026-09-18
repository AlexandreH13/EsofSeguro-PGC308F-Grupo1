### A fazer

#### Modelagem de Ameaças - Entrega 16/09 (quarta-feira)

* Carlos: 
    - Stride: Modelar o "S" e o "T". 
    - Expandir requisitos não funcionais: Autenticação/Autorização e Privacidade

* Rocha:
    - Stride: Modelar o "R"
    - Expandir requisitos não funcionais: Auditoria, Usabilidade e Manutenibilidade

* Alexandre:
    - Stride: Modelar o "I" e o "D"
    - Expandir requisitos não funcionais: Desempenho, Escalabilidade (Feito)

* Bruno:
    - Stride: Modelar o "E"
    - Expandir requisitos não funcionais: Confiabilidade e Integridade


### Ameaças
# TODO — Análise de Ameaças

## 1. Identificar ameaças

- [ ] Levantar ameaças relacionadas à autenticação.
- [ ] Levantar ameaças relacionadas à autorização.
- [ ] Levantar ameaças relacionadas à privacidade.
- [ ] Levantar ameaças relacionadas ao armazenamento de dados.
- [ ] Levantar ameaças relacionadas à comunicação entre sistemas.
- [ ] Levantar ameaças relacionadas às mídias/processos.
- [ ] Levantar ameaças relacionadas ao banco de dados.
- [ ] Levantar ameaças relacionadas à sessão do usuário.
- [ ] Levantar ameaças relacionadas à infraestrutura.

## 2. Classificar probabilidade

Para cada ameaça, classificar:

- [ ] Alta probabilidade — exploração relativamente fácil e com poucos requisitos.
- [ ] Média probabilidade — exploração possível, mas exige algumas condições.
- [ ] Baixa probabilidade — exploração difícil ou depende de condições específicas.
- [ ] Muito baixa/improvável — exploração exige recursos ou tecnologias muito avançadas.

## 3. Classificar impacto

Para cada ameaça, verificar:

- [ ] Crítico — pode comprometer completamente o sistema ou expor dados altamente sensíveis.
- [ ] Alto — pode causar grande impacto, mas com escopo limitado.
- [ ] Médio — causa prejuízo relevante, mas não compromete completamente o sistema.
- [ ] Baixo — impacto limitado e facilmente contornável.

## 4. Avaliar criticidade

- [ ] Alta probabilidade + alto impacto → prioridade máxima.
- [ ] Alta probabilidade + baixo impacto → ameaça frequente, mas menos crítica.
- [ ] Baixa probabilidade + alto impacto → ameaça rara, porém potencialmente grave.
- [ ] Baixa probabilidade + baixo impacto → menor prioridade.

## 5. Documentar cada ameaça

Para cada ameaça, registrar:

- [ ] Identificação da ameaça.
- [ ] Ativo afetado.
- [ ] Probabilidade.
- [ ] Impacto.
- [ ] Criticidade.
- [ ] Pré-requisitos para exploração.
- [ ] Consequência.
- [ ] Medida de mitigação.
- [ ] Risco residual após a mitigação.

## 6. Exemplos para análise

### Ameaça fácil/provável

- [ ] Verificar tentativa de acesso utilizando credenciais comprometidas.
- [ ] Verificar ausência de controle de tentativas de login.
- [ ] Verificar possibilidade de acesso a funcionalidades sem autorização.
- [ ] Verificar exposição de informações em mensagens de erro.

### Ameaça provável, mas pouco crítica

- [ ] Identificar ataques que possam causar indisponibilidade temporária.
- [ ] Identificar exposição de informações não sensíveis.
- [ ] Identificar alterações que possam ser detectadas e revertidas facilmente.

### Ameaça pouco provável, mas muito crítica

- [ ] Avaliar comprometimento da chave criptográfica.
- [ ] Avaliar comprometimento do servidor/banco de dados.
- [ ] Avaliar acesso privilegiado obtido por exploração de vulnerabilidade grave.
- [ ] Avaliar quebra de mecanismos criptográficos por recursos computacionais extremamente avançados.

### Ameaça extremamente difícil

- [ ] Avaliar cenários que dependam de recursos computacionais atualmente impraticáveis.
- [ ] Avaliar ataques que exigiriam comprometimento simultâneo de múltiplas camadas de segurança.
- [ ] Avaliar quebra de criptografia considerada segura com a tecnologia atualmente disponível.
- [ ] Documentar que determinados cenários possuem baixa probabilidade, mas podem apresentar impacto crítico.
