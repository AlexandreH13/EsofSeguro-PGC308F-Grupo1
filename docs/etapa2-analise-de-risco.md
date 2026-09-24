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
| RI01 | Acesso indevido a transcrições | Um promotor pode acessar, baixar, excluir ou utilizar no chat transcrições de outros promotores, comprometendo a confidencialidade e a integridade dos dados. | Ausência de validação de propriedade da transcrição e de autorização por objeto no Backend. | AME1 | 4 | 4 | MUITO ALTO |
| RI02 | Backend como procurador confuso | Um promotor pode consultar e copiar mídias de processos aos quais não possui acesso, utilizando indevidamente a autoridade da credencial do Backend. | Falha na validação da identidade do promotor na comunicação com o Sistema Processos, permitindo que a identidade seja manipulada ou omitida. | AME2 | 3 | 4 | MUITO ALTO |
| RI03 | Acesso indevido por usuário não promotor | Um servidor ou estagiário do MP pode obter acesso às funcionalidades e aos dados restritos a promotores utilizando um token válido emitido para outro sistema. | Ausência de validação da audiência (audience) e do papel do usuário no token de autenticação. | AME3 | 3 | 4 | MUITO ALTO |
| RI04 | Atualização de status sem autenticação de serviço | Um host não autorizado pode alterar o status e o caminho da transcrição, comprometendo a integridade dos registros e permitindo a disponibilização de conteúdo indevido. | Endpoint de atualização de status sem autenticação de serviço e sem validação do caminho do arquivo informado. | AME4 | 3 | 4 | MUITO ALTO |
