Problema
Pessoas de grupos sub-representados enfrentam barreiras simultâneas de emprego, formação e saúde mental sem um suporte integrado e humanizado.

Descrição
## App de Orientação Pessoal — App BiT

O desafio B2C propõe o desenvolvimento de uma web app responsiva com agente de IA, criada para apoiar pessoas de grupos sub-representados de forma ampla, integrada e verdadeiramente humana.

A proposta é atuar, ao mesmo tempo, em cinco dimensões essenciais da jornada dessas pessoas: formação, empregabilidade, experiências, mentorias e saúde mental.

Não se trata apenas de um app de vagas.
Não é apenas uma plataforma de cursos.
Também não é somente uma solução voltada ao bem-estar.

É um ecossistema pensado para olhar cada participante de forma 360°, com empatia, acolhimento e relevância cultural — reunindo, em um só lugar, o suporte necessário para que cada pessoa possa se desenvolver, pertencer e avançar com mais oportunidade, confiança e perspectiva de futuro.

---

PERFIL DO USUÁRIO

Estudante em formação, universitário, graduado sem emprego na área ou profissional buscando mudança. Ao criar a conta preenche:

Dados pessoais: nome, e-mail, data de nascimento, gênero, escolaridade, continente, país, estado (BR), cidade, WhatsApp

Dados profissionais: nível, área de tecnologia, o que busca — estudar / definir caminho / buscar emprego / mudar de emprego

Dores reais que a solução precisa endereçar:
. Baixa autoestima e complexo de inferioridade
. Ciclo de exclusão — uma barreira leva a outra
. Desvantagens socioeconômicas acumuladas
. Falta de senso de pertencimento no mercado de tecnologia
. Networking restrito — não conhecem as pessoas certas
. Sensação de que sempre falta algo para ser elegível

---

OS 5 SERVIÇOS — MVP

1. FORMAÇÕES
Cursos gratuitos (Programa GEAR do Google Cloud, Programa ONE da Oracle & Alura) e outros pagos. Trilhas personalizadas baseadas no gap identificado no perfil do usuário. O agente cruza o perfil com as trilhas disponíveis e recomenda o próximo passo concreto.

2. EMPREGABILIDADE
Match automático entre perfil e vagas disponíveis. O app exibe o gap de forma clara: "Você atende 70% dos requisitos desta vaga — veja o que falta e como resolver". A lógica é simples: o mercado já atende 70% das necessidades do usuário — a plataforma mostra o 30% que falta e oferece uma solução concreta. Se o usuário for contratado via plataforma, a app recebe um percentual da empresa. O usuário não paga nada.

3. EXPERIÊNCIAS ESTRUTURANTES
Eventos ao vivo e gravados com testemunhos de pessoas que viveram trajetórias semelhantes: CEOs, líderes e profissionais que superaram as mesmas barreiras. O usuário se identifica com as histórias e encontra referências reais de que é possível. O engajamento acontece quando as pessoas reconhecem sua própria dor na trajetória de outra pessoa e encontram ali uma saída.

4. MENTORIAS
Networking humanizado — mentores que convidam o usuário para uma prática, não apenas para uma entrevista formal. Outras formas de entrar no mercado além da porta convencional. "Você quer vir a uma prática comigo?" é o espírito desse serviço: uma conexão real, baseada em confiança, não em currículo.

5. SAÚDE MENTAL
Check-in diário via emojis (feliz, cansado, triste, ansioso, sobrecarregado...) ao entrar na app. O agente de IA detecta o estado emocional do usuário e sugere ações concretas e humanas: um capítulo de livro, um episódio de podcast, caminhar descalço no gramado, uma série na Netflix, uma caminhada sob a chuva. A referência inspiradora é o modelo dos Alcoólicos Anônimos — escutar sem julgar já é o início da cura. Em situações de crise (nota abaixo de 4), o agente deriva automaticamente para o CVV (Centro de Valorização da Vida).

---

FLUXO DO USUÁRIO

1. Cria conta e preenche perfil pessoal e profissional completo
2. App analisa o perfil e mostra vagas compatíveis + gap percentual ("você atende 70%")
3. Recebe trilha de formação concreta para fechar o gap identificado
4. Acessa mentores disponíveis e agenda conversa ou prática
5. Agente de saúde mental faz o primeiro check-in: "Como você está hoje?"
6. Recebe sugestões de ações concretas de bem-estar baseadas no seu estado e contexto regional
7. Visualiza eventos e recursos próximos por geolocalização (dataset Vísent CDRView)

---

DATASET VÍSENT CDRVIEW

Dados de concentração de pessoas por zona + cobertura de rede ERB (5G/4G/3G) com coordenadas reais de antenas Anatel. Dados emulados com coordenadas reais. Disponível em: github.com/wongola-bit/appbit-hackathon (inclui README e dicionário de colunas).

Uso neste desafio: mostrar eventos e recursos próximos conforme zona e conectividade do usuário. Se cobertura de rede baixa na região, o agente pode sugerir conteúdo offline para garantir acesso mesmo sem internet estável.

---

ENDPOINTS PRINCIPAIS

POST /orientar
Request: { usuario_id, perfil, nivel, regiao, idioma, lat, lng }
Response: { gap_percentual, gap_itens, trilha_sugerida, vagas_compativeis, confianca }

POST /saude
Request: { usuario_id, humor, nota_semanal, contexto }
Response: { mensagem, acao_sugerida, derivar_cvv, nota_atual, alerta }
Nota: nota_semanal < 4 aciona derivar_cvv: true (situação de crise)

---

FUNCIONALIDADES EXIGIDAS — MVP

. Onboarding completo: dados pessoais e profissionais
. Endpoint /orientar com gap percentual + trilha sugerida
. OU endpoint /saude com check-in via emojis + ação sugerida
. Interface responsiva com ao menos home + uma tela funcional
. README com instruções de execução local e exemplos de request/response

---

FUNCIONALIDADES OPCIONAIS

. Ambos os endpoints em produção e integrados
. Integração com dataset Vísent CDRView para eventos por geolocalização
. Seção Experiências Estruturantes com vídeos e depoimentos reais
. Módulo de mentorias com agenda e convite de prática
. Download offline de recursos para regiões com baixa conectividade
. Notificações push diárias de bem-estar
. Suporte multilíngue PT + ES
. Derivação automática para CVV em situações de crise (nota < 4)

---

ORIENTAÇÕES TÉCNICAS

. Plataforma: Web App Responsiva (PWA) — funciona no celular e no desktop. Use a tecnologia que sua equipe já domina: React, Vue, Node.js, Spring Boot, Python, Java ou qualquer outra.
. O stack não é obrigatório — cada equipe escolhe o que melhor conhece.
. Comece pelo contrato de integração entre os membros da equipe no Dia 1.
. O agente de saúde mental é sensível — teste exaustivamente antes de colocar em produção.
. Nunca suba credenciais ou chaves de API no repositório.
. Deploy: Railway ou Render para o MVP. 

---

POR ONDE COMEÇAR — DIA 1

1. Reunião de equipe: apresentação, divisão de responsabilidades e alinhamento do contrato de integração
2. Configurar ambiente local: repositório GitHub, arquivo .env, banco de dados
3. Dividir as frentes: interface com tela de onboarding / API com /orientar retornando dados mockados / agente com primeiro prompt isolado

---

NOTA DE OPORTUNIDADE

Este desafio é parte de um produto maior com alcance em Brasil, Angola e LATAM. Os melhores projetos poderão ser apresentados a investidores reais no Shark Tank BiT para seed funding e contratos piloto.

---

REFERÊNCIAS CULTURAIS

Os filmes a seguir foram selecionados para ampliar a compreensão sobre a dimensão de impacto que buscamos alcançar — uma abordagem que vá além do assistencialismo e promova autonomia, pertencimento, protagonismo e transformação real.

Filmes:
. The Boy Who Harnessed the Wind — jovem africano que resolve a seca com engenharia e determinação
. Gênio Indomável (Good Will Hunting) — potencial reprimido por falta de oportunidade
. Infinito — superação pessoal e propósito
. À Procura da Felicidade (The Pursuit of Happyness) — resiliência e empreendedorismo desde a adversidade
. Mãos Talentosas (Gifted Hands) — talento que supera barreiras socioeconômicas
. Rainha de Katwe — protagonismo feminino negro em tecnologia

Livros:
. Apaixone-se pelo Problema, Não Pela Solução — Uri Levine (cofundador do Waze)
. De Onde Vêm as Boas Ideias — Steven Johnson
