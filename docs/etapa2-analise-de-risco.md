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
| RI05 | Vazamento de dados sigilosos por download não controldo | O promotor baixa o arquivo .docx e usa em serviço externo, como uma I.A ou outra aplicação, retirando o documento do ambiente controlado, expondo dados de processos e pessoas. | Ausência de controle pós-download, como por exemplo marca d'água, e de trilha de auditoria sobre reuso externo. | AME7 | 3 | 4 | ALTO |
| RI06 | Captura de dados em massa por usuário mal-intencionado | Um promotor com acesso legítimo baixa um grande volume de transcrições, criando cópias paralelas não auditadas de dados sigilosos. | Ausência de limites de download por usuário | AME7 | 2 | 4 | MÉDIO |
| RI07 | Indisponibilidade do processamento por "flood" de solicitações de transcrição. | O promotor, ou uma sessão comprometida,  dispara centenas de solicitações, saturando a fila e atrasando transcrições legítimas de outros usuários. | Ausência de rate limiting por usuário ou sessão no RQF3 | AME9 | 3 | 2 | MÉDIO |
| RI08 | Alteração indevida do conteúdo de uma transcrição finalizada. | Atacante com acesso ao storage altera uma transcrição já finalizda. O promotor baixa e usa juntamente com o chat o conteúdo adulterado. | Verificação de hash/conteúdo (RQNF6.2/6.3) depende de execução correta em toda leitura. Uma falha nessa checagem deixa a alteração passar. | AME14 | 1 | 4 | BAIXO |
| RI09 | Leitura/escrita arbitrária no storage via mensagem forjada na fila. | Um atacante que publica mensagem forjada na filla faz o módulo de I.A baixar uma mídia arbitrária do storage e enviá-la à I.A. Isso sobrescreve a última transcrição. | Fila aceita a publicação sem autenticar a origem. O hash da mensagem é calculado pelo próprio atacante e não protege contra o caminho arbitrário. | AME5 | 2 | 4 | MÉDIO |