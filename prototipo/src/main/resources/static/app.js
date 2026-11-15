// Funções compartilhadas pelas telas. Tudo em JS puro, sem build.
// Regras: nunca montar HTML com dados do servidor (só textContent) e nunca guardar o token fora da aba.

const Sessao = {
  token: () => sessionStorage.getItem('tokenSessao'),
  guardar: (token) => sessionStorage.setItem('tokenSessao', token),
  limpar: () => sessionStorage.removeItem('tokenSessao'),
};

async function api(caminho, opcoes = {}) {
  const cabecalhos = { 'Content-Type': 'application/json', ...(opcoes.headers || {}) };
  const token = Sessao.token();
  if (token) cabecalhos['Authorization'] = 'Bearer ' + token;
  const resposta = await fetch(caminho, { ...opcoes, headers: cabecalhos });
  if (resposta.status === 401 && caminho !== '/api/sessao') {
    Sessao.limpar();
    location.href = 'index.html';
    throw new Error('sessão inválida');
  }
  return resposta;
}

function exigirSessao() {
  if (!Sessao.token()) location.href = 'index.html';
}

async function montarCabecalho() {
  const resposta = await api('/api/sessao');
  const quem = await resposta.json();
  document.querySelector('[data-usuario]').textContent = quem.nome;
  document.querySelector('[data-sair]').addEventListener('click', sair);
}

async function sair() {
  await api('/api/sessao', { method: 'DELETE' });
  Sessao.limpar();
  location.href = 'sessao-encerrada.html';
}

const STATUS = {
  NA_FILA: { classe: 'queue', rotulo: 'Na fila' },
  EM_PROCESSAMENTO: { classe: 'running', rotulo: 'Em processamento' },
  FINALIZADA: { classe: 'done', rotulo: 'Finalizada' },
  FALHA: { classe: 'fail', rotulo: 'Falha' },
};

function badge(status) {
  const span = el('span', 'badge ' + STATUS[status].classe, STATUS[status].rotulo);
  return span;
}

function dataHora(iso) {
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

function el(tag, classe, texto) {
  const e = document.createElement(tag);
  if (classe) e.className = classe;
  if (texto !== undefined) e.textContent = texto;
  return e;
}

async function inicializarTranscricoes() {
  const grupos = document.getElementById('grupos');
  if (!grupos) return;

  const estado = document.getElementById('estado-lista');
  const modal = document.getElementById('confirmacao');
  const erroExclusao = document.getElementById('erro-exclusao');
  const botaoConfirmar = document.getElementById('confirmar');
  let selecionada = null;
  let atualizando = false;

  function fecharConfirmacao() {
    modal.hidden = true;
    selecionada = null;
    erroExclusao.hidden = true;
    botaoConfirmar.disabled = false;
    botaoConfirmar.textContent = 'Excluir';
  }

  function abrirConfirmacao(transcricao) {
    selecionada = transcricao;
    document.getElementById('midia-excluir').textContent = transcricao.midia;
    erroExclusao.hidden = true;
    modal.hidden = false;
    botaoConfirmar.focus();
  }

  function renderizar(transcricoes) {
    grupos.replaceChildren();
    if (transcricoes.length === 0) {
      estado.className = 'callout';
      estado.textContent = 'Você ainda não tem transcrições.';
      estado.hidden = false;
      return;
    }

    estado.hidden = true;
    const porProcesso = new Map();
    for (const transcricao of transcricoes) {
      const chave = `${transcricao.processo}\u0000${transcricao.vara || ''}`;
      if (!porProcesso.has(chave)) porProcesso.set(chave, []);
      porProcesso.get(chave).push(transcricao);
    }

    for (const itens of porProcesso.values()) {
      const titulo = el('div', 'group-title', `Processo ${itens[0].processo}${itens[0].vara ? ` · ${itens[0].vara}` : ''}`);
      grupos.appendChild(titulo);
      const tabela = el('div', 'table-wrap');
      const table = el('table', 'grid');
      const head = document.createElement('thead');
      const cabecalho = document.createElement('tr');
      for (const texto of ['Mídia', 'Solicitada em', 'Status', 'Ações']) {
        const th = el('th', texto === 'Ações' ? 'actions' : '', texto);
        cabecalho.appendChild(th);
      }
      head.appendChild(cabecalho);
      table.appendChild(head);

      const corpo = document.createElement('tbody');
      for (const transcricao of itens) {
        const linha = document.createElement('tr');
        linha.appendChild(el('td', '', transcricao.midia));
        linha.appendChild(el('td', '', dataHora(transcricao.solicitadaEm)));

        const celulaStatus = document.createElement('td');
        const configuracaoStatus = STATUS[transcricao.status];
        if (configuracaoStatus) celulaStatus.appendChild(badge(transcricao.status));
        else celulaStatus.appendChild(el('span', 'badge fail', 'Status desconhecido'));
        if (transcricao.detalheStatus) {
          celulaStatus.appendChild(el('span', 'muted small status-detail', transcricao.detalheStatus));
        }
        linha.appendChild(celulaStatus);

        const acoes = el('td', 'actions');
        if (transcricao.status === 'FINALIZADA') {
          const abrir = el('a', 'btn small secondary', 'Abrir');
          abrir.href = `transcricao.html?id=${encodeURIComponent(transcricao.id)}`;
          acoes.appendChild(abrir);
          acoes.appendChild(document.createTextNode(' '));
        }
        const podeExcluir = transcricao.status === 'FINALIZADA' || transcricao.status === 'FALHA';
        const excluir = el('button', `btn small danger${podeExcluir ? '' : ' disabled'}`, 'Excluir');
        excluir.type = 'button';
        excluir.disabled = !podeExcluir;
        excluir.addEventListener('click', () => abrirConfirmacao(transcricao));
        acoes.appendChild(excluir);
        linha.appendChild(acoes);
        corpo.appendChild(linha);
      }
      table.appendChild(corpo);
      tabela.appendChild(table);
      grupos.appendChild(tabela);
    }
  }

  async function carregar() {
    if (atualizando) return;
    atualizando = true;
    try {
      const resposta = await api('/api/transcricoes');
      if (!resposta.ok) throw new Error('Não foi possível carregar as transcrições.');
      const transcricoes = await resposta.json();
      renderizar(transcricoes);
      if (transcricoes.some(item => item.status === 'NA_FILA' || item.status === 'EM_PROCESSAMENTO')) {
        window.setTimeout(carregar, 15000);
      }
    } catch (erro) {
      estado.className = 'callout warn';
      estado.textContent = erro.message || 'Ocorreu um erro ao carregar as transcrições.';
      estado.hidden = false;
    } finally {
      atualizando = false;
    }
  }

  document.getElementById('cancelar').addEventListener('click', fecharConfirmacao);
  modal.addEventListener('click', evento => {
    if (evento.target === modal) fecharConfirmacao();
  });
  document.addEventListener('keydown', evento => {
    if (evento.key === 'Escape' && !modal.hidden) fecharConfirmacao();
  });
  botaoConfirmar.addEventListener('click', async () => {
    if (!selecionada) return;
    botaoConfirmar.disabled = true;
    botaoConfirmar.textContent = 'Excluindo...';
    try {
      const resposta = await api(`/api/transcricoes/${encodeURIComponent(selecionada.id)}`, { method: 'DELETE' });
      if (!resposta.ok) {
        const corpo = await resposta.json().catch(() => ({}));
        throw new Error(corpo.erro || 'Não foi possível excluir a transcrição.');
      }
      fecharConfirmacao();
      await carregar();
    } catch (erro) {
      erroExclusao.textContent = erro.message || 'Não foi possível excluir a transcrição.';
      erroExclusao.hidden = false;
      botaoConfirmar.disabled = false;
      botaoConfirmar.textContent = 'Tentar novamente';
    }
  });

  await carregar();
}

async function inicializarDetalheTranscricao() {
  const estado = document.getElementById('estado-transcricao');
  const detalhe = document.getElementById('detalhe-transcricao');
  if (!estado || !detalhe) return;

  const id = new URLSearchParams(location.search).get('id');
  if (!id) {
    estado.className = 'callout warn';
    estado.textContent = 'Identificador da transcrição ausente.';
    return;
  }

  try {
    const resposta = await api(`/api/transcricoes/${encodeURIComponent(id)}`);
    const corpo = await resposta.json().catch(() => ({}));
    if (!resposta.ok) throw new Error(corpo.erro || 'Não foi possível abrir esta transcrição.');

    document.getElementById('processo-crumb').textContent = `Processo ${corpo.processo}`;
    document.getElementById('midia-crumb').textContent = corpo.midia;
    document.getElementById('titulo-midia').textContent = corpo.midia;
    document.getElementById('status-transcricao').replaceChildren(badge(corpo.status));
    document.getElementById('metadados-transcricao').textContent =
      `Solicitada em ${dataHora(corpo.solicitadaEm)}${corpo.detalheStatus ? ` · ${corpo.detalheStatus}` : ''}`;

    if (corpo.status !== 'FINALIZADA' || !corpo.texto) {
      estado.textContent = corpo.status === 'FALHA'
        ? `A transcrição falhou${corpo.detalheStatus ? `: ${corpo.detalheStatus}` : '.'}`
        : `A transcrição ainda não está finalizada. Status atual: ${STATUS[corpo.status]?.rotulo || corpo.status}.`;
      detalhe.hidden = false;
      document.getElementById('texto-transcricao').hidden = true;
      document.getElementById('baixar-docx').hidden = true;
      document.getElementById('abrir-chat').hidden = true;
      return;
    }

    estado.hidden = true;
    detalhe.hidden = false;
    const texto = document.getElementById('texto-transcricao');
    texto.textContent = corpo.texto;
    texto.classList.add('transcript-content');

    document.getElementById('baixar-docx').addEventListener('click', async evento => {
      const botao = evento.currentTarget;
      botao.disabled = true;
      try {
        const arquivo = await api(`/api/transcricoes/${encodeURIComponent(id)}/docx`);
        if (!arquivo.ok) {
          const erro = await arquivo.json().catch(() => ({}));
          throw new Error(erro.erro || 'Não foi possível gerar o documento.');
        }
        const url = URL.createObjectURL(await arquivo.blob());
        const link = el('a');
        link.href = url;
        link.download = `transcricao-${id}.docx`;
        link.click();
        URL.revokeObjectURL(url);
      } catch (erro) {
        estado.className = 'callout warn';
        estado.textContent = erro.message || 'Não foi possível gerar o documento.';
        estado.hidden = false;
      } finally {
        botao.disabled = false;
      }
    });
    document.getElementById('abrir-chat').addEventListener('click', async evento => {
      const botao = evento.currentTarget;
      botao.disabled = true;
      try {
        const respostaChat = await api('/api/chats', {
          method: 'POST',
          body: JSON.stringify({ transcricaoId: id }),
        });
        const chat = await respostaChat.json().catch(() => ({}));
        if (!respostaChat.ok) throw new Error(chat.erro || 'Não foi possível iniciar o chat.');
        location.href = `chat.html?chatId=${encodeURIComponent(chat.id)}&transcricaoId=${encodeURIComponent(id)}`;
      } catch (erro) {
        estado.className = 'callout warn';
        estado.textContent = erro.message || 'Não foi possível iniciar o chat.';
        estado.hidden = false;
        botao.disabled = false;
      }
    });
  } catch (erro) {
    estado.className = 'callout warn';
    estado.textContent = erro.message || 'Ocorreu um erro ao abrir a transcrição.';
    estado.hidden = false;
  }
}

async function inicializarChat() {
  const estado = document.getElementById('estado-chat');
  const area = document.getElementById('area-chat');
  if (!estado || !area) return;

  const parametros = new URLSearchParams(location.search);
  const id = parametros.get('chatId');
  const transcricaoId = parametros.get('transcricaoId');
  if (!id || !transcricaoId) {
    estado.className = 'callout warn';
    estado.textContent = 'Este chat não está disponível. Abra-o a partir de uma transcrição finalizada.';
    return;
  }

  const mensagens = document.getElementById('mensagens');
  const pergunta = document.getElementById('pergunta');
  const enviar = document.getElementById('enviar-pergunta');
  const form = document.getElementById('form-pergunta');

  function renderizar(chat) {
    document.getElementById('chat-midia').textContent = chat.midia;
    document.getElementById('chat-processo').textContent = `Processo ${chat.processo}`;
    document.getElementById('voltar-transcricao').href = `transcricao.html?id=${encodeURIComponent(chat.transcricaoId)}`;
    mensagens.replaceChildren();
    for (const mensagem of chat.mensagens) {
      const bolha = el('div', `bubble ${mensagem.autor === 'usuario' ? 'user' : 'ai'}`);
      bolha.appendChild(el('div', 'who', mensagem.autor === 'usuario' ? 'Você' : 'IA (simulada)'));
      bolha.appendChild(document.createTextNode(mensagem.texto));
      mensagens.appendChild(bolha);
    }
    mensagens.scrollTop = mensagens.scrollHeight;
  }

  async function carregarChat() {
    const resposta = await api(`/api/chats/${encodeURIComponent(id)}`);
    const corpo = await resposta.json().catch(() => ({}));
    if (!resposta.ok) throw new Error(corpo.erro || 'Chat encerrado ou não encontrado.');
    renderizar(corpo);
    estado.hidden = true;
    area.hidden = false;
  }

  try {
    await carregarChat();
  } catch (erro) {
    estado.className = 'callout warn';
    estado.textContent = erro.message || 'Não foi possível abrir o chat.';
    return;
  }

  form.addEventListener('submit', async evento => {
    evento.preventDefault();
    const texto = pergunta.value.trim();
    if (!texto) return;
    enviar.disabled = true;
    pergunta.disabled = true;
    try {
      const resposta = await api(`/api/chats/${encodeURIComponent(id)}/mensagens`, {
        method: 'POST',
        body: JSON.stringify({ texto }),
      });
      const corpo = await resposta.json().catch(() => ({}));
      if (!resposta.ok) throw new Error(corpo.erro || 'Não foi possível enviar a pergunta.');
      pergunta.value = '';
      renderizar(corpo);
      estado.hidden = true;
    } catch (erro) {
      estado.className = 'callout warn';
      estado.textContent = erro.message || 'Falha ao enviar. Revise e tente novamente.';
      estado.hidden = false;
    } finally {
      enviar.disabled = false;
      pergunta.disabled = false;
      pergunta.focus();
    }
  });

  document.getElementById('encerrar-chat').addEventListener('click', async evento => {
    const botao = evento.currentTarget;
    botao.disabled = true;
    try {
      const resposta = await api(`/api/chats/${encodeURIComponent(id)}`, { method: 'DELETE' });
      if (!resposta.ok) throw new Error('Não foi possível encerrar o chat.');
      location.href = 'transcricoes.html';
    } catch (erro) {
      estado.className = 'callout warn';
      estado.textContent = erro.message || 'Não foi possível encerrar o chat.';
      estado.hidden = false;
      botao.disabled = false;
    }
  });
}