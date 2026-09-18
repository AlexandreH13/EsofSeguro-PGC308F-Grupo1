## Análise de Riscos

**Descrição**

Neste arquivo será detalhado cada risco identificado na aplicação. Esses riscos foram levantados assumindo possíveis ações que causam prejuízo, caso uma [ameaça](etapa1-modelagem-ameacas.md) seja explorada. Aqui, para cada risco queremos responder as seguintes três perguntas: 
1) O que pode acontecer?
2) Qual a probabilidade de acontecer? 
3) Qual o prejuízo se acontecer?

Além disso, também quantificamos cada risco em relação a sua probabilidade e impacto, onde impacto é o tamanho do prejuízo causado. Portanto, as variáveis "Probabilidade" e "Impacto" serão quantificadas atribuindo notas de 1 a 4. Já a variável "Risco" será computada usando a multiplicação das variáveis "Probabilidade" e "Impacto".

$r=p.i$\
onde\
$r$ = Risco\
$p$ = Probabilidade\
$i$ = Impacto

Portanto, a variável "Risco" irá conter valores no intervalo {1, ..., 16}. A classificação do risco será feito através de um mapeamento dos valores para uma classe, conforme a tabela abaixo.

#### Tabela de referência {#tabela-referencia}

| Nota | Classificação |
| --- | --- |
| 1 a 4 | BAIXO |
| 5 a 8 | MÉDIO |
| 9 a 12 | ALTO |
| 13 a 16 | MUITO ALTO |

---

#### Tabela de registro dos riscos

| Código | Nome | Descrição | Vulnerabilidade | Ameaça | Probabilidade | Impacto | Classificação |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RI01 | Risco 01 | Descrição do risco | Qual vulnerabilidade associada (falta de qual mecânismo) | Qual a ameaça (da tabela das ameaças) está associado. Ex: AME1 | 4 | 4 | MUITO ALTO |