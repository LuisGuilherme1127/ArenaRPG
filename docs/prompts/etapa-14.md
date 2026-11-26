# Registro do prompt — Etapa 14A

- Ferramenta: OpenAI Codex, agente de desenvolvimento.
- Modelo: GPT-6 conforme identificado pelo ambiente da execução; a versão exata não é exposta nesta sessão.
- Prompts usados para gerar SQL nesta etapa: nenhum. Esta etapa registra apenas requisitos e planejamento; prompts de SQL serão documentados quando a implementação do banco começar.
- O texto integral abaixo é o prompt fornecido para a implementação da Etapa 14A.

---
# ARENA RPG — ETAPA 14A: INTEGRAÇÃO FRONTEND E BACKEND

## Contexto

O Arena RPG é um projeto acadêmico integrado das disciplinas de Programação Orientada a Objetos e Banco de Dados.

Tecnologias:
- Frontend: HTML, CSS, JavaScript e Vite.
- Backend: Java e Spring Boot.
- Banco de dados futuro: PostgreSQL e PL/pgSQL.
- Ambiente de desenvolvimento: Windows 11 e VS Code.

O frontend já possui menu principal, seleção de personagens, tabuleiro 8x8, combate por turnos, habilidades e suporte a sprites.

O backend possui classes de personagens, conceitos de POO, API REST e implementação do motor de partidas solicitada na Etapa 13.

## 1. Diagnóstico inicial

Antes de modificar arquivos, examine o estado atual do frontend e do backend.

Verifique se a Etapa 13 foi efetivamente implementada e se os testes do motor de combate estão funcionando. Caso existam problemas impeditivos, informe-os antes de prosseguir.

Identifique os endpoints, DTOs, formatos JSON e regras de combate existentes. Não invente contratos incompatíveis com a implementação atual.

## 2. Integração com a API

Crie uma camada de comunicação HTTP reutilizável no frontend.

Integre os endpoints existentes para:

- Consultar personagens.
- Criar partidas.
- Consultar o estado da partida.
- Movimentar personagens.
- Executar ataques básicos.
- Executar habilidades especiais.
- Finalizar turnos.
- Identificar vitória e derrota.

Utilize o backend Java como fonte oficial do estado e das regras de combate.

O frontend deve ser responsável pela apresentação, interação e efeitos visuais, sem calcular novamente dano, consumo de mana ou resultado da partida.

## 3. Configuração do ambiente

Configure a comunicação entre o Vite e o Spring Boot.

Preferencialmente, utilize um proxy de desenvolvimento do Vite para encaminhar as requisições /api ao backend em localhost:8080.

Documente as configurações e não libere permissões CORS indiscriminadamente.

## 4. Tratamento de erros

Implemente:

- Indicador de carregamento.
- Tratamento de backend indisponível.
- Mensagens de ações inválidas.
- Prevenção de requisições duplicadas.
- Bloqueio de ações durante requisições pendentes.
- Atualização consistente do tabuleiro após cada resposta.
- Tratamento de partidas inexistentes ou expiradas.

Não simule sucesso quando o backend retornar erro.

## 5. Preservação visual

Mantenha rigorosamente o design atual do Arena RPG.

Preserve o menu, a seleção de personagens, as cores, as fontes, o tabuleiro, os placeholders, os sprites e as animações existentes.

Não gere imagens nem altere a identidade visual nesta etapa.

## 6. Requisitos acadêmicos obrigatórios de Banco de Dados

Registre no planejamento do projeto os seguintes requisitos fornecidos pelo professor:

1. Criação das tabelas com as quatro integridades: entidade, domínio, referencial e regras definidas pelo usuário.
2. Criação de pelo menos uma trigger PostgreSQL utilizando PL/pgSQL.
3. Criação de pelo menos um índice explícito em um campo relevante.
4. Criação de contas e permissões de acesso ao banco de dados.
5. Criação de pelo menos uma view.

Cada requisito vale 1 ponto.

O banco de dados deverá utilizar PostgreSQL.

Planejamento inicial:
- Tabelas de jogadores, personagens e partidas.
- Trigger para atualização das estatísticas após a conclusão de partidas.
- Índice para consultas ao histórico de partidas.
- Roles distintas para administração, aplicação e consulta.
- View para exibição do ranking.

Essas estruturas deverão ser implementadas e testadas em uma etapa posterior. Não adicione JPA, migrations ou conexão com PostgreSQL nesta etapa.

## 7. Documentação

Crie na raiz do projeto, caso ainda não existam:

- docs/requisitos-banco-dados.md
- docs/prompts/etapa-14.md

Registre os requisitos acadêmicos no primeiro arquivo e o prompt completo utilizado nesta etapa no segundo.

O professor exige a identificação da ferramenta de IA, do modelo utilizado e dos prompts relacionados à criação das estruturas SQL. Não invente a identificação do modelo; registre-a quando estiver disponível.

## 8. Testes

Verifique:

- Build do frontend.
- Testes existentes do backend.
- Criação de partidas pelo frontend.
- Seleção de personagens pela API.
- Movimento válido e inválido.
- Ataque básico.
- Habilidade e consumo de mana.
- Alternância de turnos.
- Vitória e derrota.
- Comportamento com backend indisponível.
- Prevenção de ações duplicadas.
- Preservação da interface visual.

## 9. Restrições

Não implementar autenticação nesta etapa.
Não implementar persistência nesta etapa.
Não alterar atributos ou regras de combate sem justificativa.
Não remover funcionalidades existentes.
Não adicionar dependências desnecessárias.
Não apagar arquivos ou alterações do usuário.

## 10. Relatório final

Apresente:

- Diagnóstico inicial.
- Arquivos criados e modificados.
- Endpoints integrados.
- Regras que passaram a ser controladas exclusivamente pelo Java.
- Testes executados e respectivos resultados.
- Comandos exatos para executar frontend e backend no Windows.
- Pendências para a próxima etapa.

Você está autorizado a implementar a Etapa 14A no projeto.