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

| Nota    | Classificação |
|---------|---------------|
| 1 a 4   | BAIXO         |
| 5 a 8   | MÉDIO         |
| 9 a 12  | ALTO          |
| 13 a 16 | MUITO ALTO    |

---

#### Tabela de registro dos riscos

Cada risco corresponde à ameaça de mesmo número na [modelagem de ameaças](etapa1-modelagem-ameacas.md). As condições que tornam cada risco possível e as razões das notas estão na seção de justificativas abaixo. A tabela está ordenada da maior nota para a menor.

| Código | Ameaça | Evento de risco | Probabilidade | Impacto | Nota | Classificação |
|--------|--------|-----------------|---------------|---------|------|---------------|
| RI01 | AME1 | Promotor troca o id e lê ou apaga transcrição de colega | 4 | 4 | 16 | MUITO ALTO |
| RI07 | AME7 | Promotor leva transcrição baixada a serviço externo e expõe dados sigilosos | 4 | 4 | 16 | MUITO ALTO |
| RI02 | AME2 | Promotor obtém do Sistema Processos mídias de processos em que não atua | 3 | 4 | 12 | ALTO |
| RI04 | AME4 | Chamada forjada ao endpoint de status entrega ao promotor transcrição errada | 3 | 4 | 12 | ALTO |
| RI13 | AME13 | Atacante com senha roubada age como promotor em todos os processos dele | 3 | 4 | 12 | ALTO |
| RI03 | AME3 | Estagiário ou servidor do MP entra no sistema como promotor | 3 | 3 | 9 | ALTO |
| RI08 | AME8 | Terceiro usa a sessão aberta do promotor em computador fora do MP | 3 | 3 | 9 | ALTO |
| RI05 | AME5 | Mensagem forjada na fila desvia transcrições e sobrescreve as de outros promotores | 2 | 4 | 8 | MÉDIO |
| RI15 | AME15 | Atacante troca a mídia no Storage e a transcrição sai falsa | 2 | 4 | 8 | MÉDIO |
| RI16 | AME16 | Mensagem alterada na fila entrega ao promotor transcrição de outra mídia | 2 | 4 | 8 | MÉDIO |
| RI09 | AME9 | Promotor lota a fila com pedidos e atrasa transcrições dos colegas | 3 | 2 | 6 | MÉDIO |
| RI11 | AME11 | Promotor acumula mídias grandes até o Storage recusar novas gravações | 2 | 3 | 6 | MÉDIO |
| RI12 | AME12 | Promotor sobrecarrega o chat e esgota a I.A. ou o Backend | 2 | 3 | 6 | MÉDIO |
| RI06 | AME6 | Mídia maliciosa juntada ao processo executa código no Módulo I.A. | 1 | 4 | 4 | BAIXO |
| RI10 | AME10 | Promotor força falhas na I.A. e os retries esgotam a cota | 2 | 2 | 4 | BAIXO |
| RI14 | AME14 | Atacante altera transcrição finalizada e o promotor usa o texto falso | 1 | 4 | 4 | BAIXO |

---

#### Justificativas

A probabilidade estima a chance de o evento acontecer supondo presente a falha que a ameaça descreve, mesmo quando essa falha é a ausência de um controle exigido, e considera quem pode explorá-la, com que esforço e com que motivação. Os demais controles previstos nos [requisitos](requisitos.md) entram na conta. O impacto mede o dano se o evento acontecer.

##### RI01 - Acesso a transcrição de outro promotor

O promotor vê o id das próprias transcrições em cada requisição, e nenhum requisito exige ids imprevisíveis; com ids sequenciais, como no CAE1, basta subtrair um para chegar à transcrição de um colega. Como qualquer promotor autenticado faz isso sem preparo, a probabilidade é 4. O impacto é 4 porque ele lê transcrições sigilosas de qualquer promotoria, descobre em que processos cada colega atua e pode apagá-las, restando o backup diário da RQNF5.7 para recuperá-las.

##### RI07 - Download das transcrições

O download em .docx é função do sistema (RQF8), e a AME7 descreve o uso seguinte, de boa-fé: levar o texto a outra I.A. ou a um aplicativo de formatação. Nenhuma falha técnica ou esforço é necessário, e o dever de sigilo é norma de conduta, não controle técnico: nada no sistema impede o uso, e por isso a probabilidade é 4. O impacto é 4 porque a cópia sai do ambiente do MP com dados do processo e das pessoas envolvidas, e o MP deixa de saber quem a lê.

##### RI02 - Backend como procurador confuso na consulta ao Sistema Processos

Na AME2, se o Backend omite a identidade, o promotor só informa o número do processo; se a tira da requisição, ele a intercepta e põe a de um colega autorizado, como no CAE2. Em ambos ele tem de querer um processo específico e conhecer seu número, o que deixa a probabilidade em 3. O impacto chega a 4 porque a credencial do Backend alcança processos sigilosos de todo o Estado, cujas mídias o promotor copia e transcreve.

##### RI04 - Atualização de status sem autenticação de serviço

O Backend aceita login de qualquer máquina (AME8), e a AME4 supõe o endpoint de status aberto a quem o alcance. Achar esse endpoint, que a interface não usa, e o formato da chamada é o trabalho que mantém a probabilidade em 3. A RQNF6.3 não o detém, porque o hash do texto chega pelo próprio Módulo I.A., que ele imita. O impacto é 4 porque o promotor lê o depoimento de outro processo e pode usá-lo como prova.

##### RI13 - Falsificação de identidade de um promotor

A conta é a institucional do provedor de identidade, que fica fora do escopo deste serviço, e nenhum documento diz se ele exige segundo fator. Phishing e senhas reutilizadas fora do MP rendem contas a quem as procura, mas só parte delas é de promotor. Por isso a probabilidade é 3. O impacto é 4 porque o atacante age como o promotor em todos os processos dele, até apagar transcrições, enquanto a senha valer.

##### RI03 - Usuário do MP que não é promotor entra como promotor

O provedor de identidade, onde fica o cadastro de promotores (seção 4 da descrição do serviço), pode barrar o login direto de quem não é promotor. Sobra o caminho da AME3: extrair o token recebido em outro sistema do MP e apresentá-lo ao Backend. Qualquer usuário do MP consegue dar esse passo técnico, e por isso a probabilidade é 3. O impacto é 3 porque o intruso ganha os direitos de um promotor, mas o Sistema Processos só libera os processos a que a identidade dele tem acesso, sem alcançar todo o acervo.

##### RI08 - Acesso em ambientes externos

Nenhum requisito limita o login por rede ou por equipamento, e a AME8 prevê o uso em máquinas fora do MP. Basta o promotor usar um computador compartilhado e não encerrar a sessão (RQF11) para um terceiro encontrá-la aberta; por isso a probabilidade é 3. O impacto fica em 3 porque o terceiro vê o que o promotor vê apenas enquanto aquela sessão durar e, sem a senha, não tem como voltar depois, ao contrário do atacante do RI13.

##### RI05 - Mensagem forjada na fila

Só o Backend e o Módulo I.A. usam a fila (seção 6 da descrição do serviço). Publicar nela exige a credencial da fila, como no CAE5, ou acesso de rede a uma fila sem autenticação, ao alcance de poucos; a probabilidade é 2. O hash da RQNF6.1 viaja sem assinatura e pode ser copiado de uma mensagem legítima. O impacto é 4 porque o atacante manda transcrever mídias do Storage e gravar o resultado sobre transcrições alheias ou onde as lê depois.

##### RI15 - Adulteração de mídia antes da transcrição

Com a conferência de hash ausente, como supõe a AME15, a troca passa sem alerta. Mesmo assim o atacante precisa de credencial com escrita no Storage, como a do Módulo I.A. (AME6), e de saber qual mídia foi pedida antes do processamento. O prazo curto e o alvo específico dão probabilidade 2. O impacto é 4 porque o promotor trabalha com um texto que não corresponde ao depoimento gravado, e o hash no rodapé do .docx (RQNF6.4) confirma o texto falso.

##### RI16 - Adulteração de mensagens da fila

Com acesso à fila, como no RI05, o atacante retira uma mensagem legítima e a republica com outro caminho. A RQNF6.1 não impede a troca, porque o hash vai na mesma mensagem, sem assinatura, e ele o troca pelo da mídia nova, copiado de outra mensagem. O acesso restrito à fila deixa a probabilidade em 2. O impacto é 4 porque o promotor recebe como sua a transcrição de uma mídia de outro processo e pode usá-la como prova no processo dele.

##### RI09 - Volume alto de requisições na fila

Nenhum requisito limita o volume de pedidos, e a RQNF4.2 obriga a aceitar várias mídias do mesmo usuário; a fila enche até sem má intenção, num lote grande de mídias legítimas, o que sustenta a probabilidade 3 mesmo sem ganho em sabotar colegas, cujos pedidos aparecem na lista de quem os fez (RQF5). O impacto é 2 porque transcrições alheias atrasam e, passada 1 hora, terminam em falha (RQNF3.2, RQNF5.3), mas podem ser pedidas de novo quando a rajada acaba.

##### RI11 - Esgotamento de Storage

Um promotor sozinho acrescenta uma fração da carga que a RQNF4.3 já prevê, de até 2.000 transcrições por dia com mídias de até 80MB (RQNF3.1). Se o Storage atingir um limite de espaço ou de custo, a causa principal será a falta de retenção, não o abuso; por isso a probabilidade é 2. O impacto é 3 porque, com o Storage cheio, novas transcrições falham para todos os promotores até alguém ampliar o limite ou apagar dados.

##### RI12 - Sobrecarga de perguntas no chat com a I.A.

Basta um promotor com uma transcrição finalizada para abrir vários chats e disparar perguntas, e nenhum requisito limita isso. Diferente da fila, que enche até com uso legítimo, aqui o volume exige intenção, e o abuso não traz ganho a quem o pratica; a probabilidade fica em 2. O impacto é 3 porque, além da cota da I.A., que se recompõe sozinha, o esgotamento da memória do Backend que guarda o histórico derruba o sistema para todos, ferindo a disponibilidade mínima da RQNF5.6.

##### RI06 - Mídia maliciosa executa código no Módulo I.A.

O atacante não tem conta no serviço e precisa que a mídia preparada entre no processo. A inclusão de mídias no Sistema Processos cabe à polícia, aos cartórios e aos servidores do MP (escopo em servico.md); mesmo no CAE6, em que a mídia vem do advogado de um investigado, a juntada passa por esses atores. Montar o arquivo exige conhecer a versão do decodificador, e a AME6 afirma que a RQNF6.5 não o barra. Como o caminho depende de um ator institucional e de preparo raro, a probabilidade é 1. O impacto é 4 porque o código roda com a credencial do Módulo I.A., que alcança todo o Storage e a fila.

##### RI10 - Usuário força retries na I.A.

Pela descrição do serviço (seções 2 e 4), o promotor escolhe mídias que outros atores juntaram ao processo e não envia arquivos próprios. Para forçar retries, precisa de mídias que passem pela RQNF6.5, que confere formato e malware, e ainda assim falhem na I.A.; depender delas mantém a probabilidade em 2. O impacto é 2 porque a cota da I.A. externa se recompõe sozinha: transcrição e chat falham para todos durante esse intervalo, e as transcrições perdidas podem ser pedidas de novo.

##### RI14 - Adulteração de dados da transcrição

A AME14 supõe ausente a conferência de hash da RQNF6.3, mas não a regra da RQNF6.2, que proíbe alterar o arquivo depois do status finalizada. A probabilidade 1 conta com essa regra aplicada no próprio Storage, como bloqueio de escrita, que só um acesso administrativo à nuvem remove. Status e caminho ficam no Backend, e alterá-los sem autenticação é o cenário do RI04. O impacto é 4 porque o promotor usa como prova um texto adulterado, sem motivo para duvidar dele.

---

#### Priorização

1. RI01, RI07 (16): os dois MUITO ALTO. Um dá acesso ao acervo alheio trocando um id; o outro vaza dados sigilosos por uma função de uso rotineiro, sem nenhuma barreira técnica.
2. RI02, RI04, RI13 (12): acesso indevido e desvio de conteúdo com dano máximo e caminho de exploração plausível.
3. RI03, RI08 (9): intrusos com alcance limitado ao que a identidade ou a sessão aberta libera.
4. RI05, RI15, RI16 (8): adulterações e desvios que exigem credencial da fila ou escrita no Storage, ao alcance de poucos.
5. RI09, RI11, RI12 (6): indisponibilidades de fila, Storage e chat, com recuperação possível.
6. RI06, RI10, RI14 (4): exploração custosa ou dependente de ator institucional, ou efeito que se recompõe sozinho.
