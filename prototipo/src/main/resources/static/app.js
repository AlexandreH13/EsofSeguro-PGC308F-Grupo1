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