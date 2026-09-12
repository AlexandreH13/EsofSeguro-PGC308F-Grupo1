## Engenharia de Software Seguro

### Trabalho da Disciplina PGC308F - Grupo 1

---

### 1. Descrição

* **Objetivo**: Repositório para centralizar a documentação das etapas exigidas para o trabalho final.

* **Nome do grupo**: Grupo 1.

* **Membros do grupo**: Alexandre, Bruno, Carlos e Rocha.

---

### 2. Estrutura do Repositório

* O documento [SERVICO.md](SERVICO.md) descreve com detalhes o funcionamento do case usado para este trabalho. Também possui um diagrama high-level da arquitetura para ajudar a identificar os componentes principais. Portanto, dúvidas relacionadas a funcionalidades e arquitetura podem ser sanadas neste documento.

* O documento [REQUISITOS.md](REQUISITOS.md) possui a listagem dos requisitos funcionais e não funcionais. 

* A pasta [requisitos funcionais](requisitos%20funcionais) contém os casos de uso: um arquivo por requisito funcional, com o diagrama de sequência das interações entre os componentes do serviço. O [README](requisitos%20funcionais/README.md) da pasta reúne convenções, premissas e índice.

* A pasta [resources/diagramas](resources/diagramas) contém os diagramas de `requisitos funcionais/` exportados em PNG com fundo transparente. Para regenerar, rode `npm install` e `npm run diagramas`.

* A pasta [mockups](mockups) contém as telas de referência em HTML, e [resources/mockups](resources/mockups) as mesmas telas exportadas em PNG. Para regenerar, rode `npm run mockups`.

* O documento [MODELAGEM_AMEACAS.md](MODELAGEM_AMEACAS.md) possui a modelagem das ameaças utilizando o framework STRIDE.