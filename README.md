🌐 BiT App — Ecossistema 360 de Orientação Pessoal e Profissional

O BiT App é uma plataforma inovadora desenvolvida durante a simulação da No Country (Grupo 68). O ecossistema foi projetado sob uma abordagem holística 360°, unindo desenvolvimento profissional, infraestrutura tecnológica regional e acompanhamento preventivo de bem-estar e saúde mental.

🚀 Principais Funcionalidades

Orientação de Carreira Orientada por IA: Mapeamento de competências (hard e soft skills) com cálculo dinâmico de desvios técnicos ($0\% \le \text{gap} \le 100\%$), sugestão inteligente de trilhas de aprendizagem reais e correspondência de vagas geolocalizadas.

Check-in de Saúde Mental com CNV: Escuta ativa e acolhimento emocional humanizado com base nos preceitos de Comunicação Não-Violenta (CNV), identificando estados de estresse extremo ou depressão com roteamento automático de canais de suporte nacional.

Mapeamento de Infraestrutura de Rede: Cadastro e gerenciamento de ativos de rede móvel regional (3G, 4G, 5G), otimizando a distribuição técnica com base na densidade populacional e cobertura de dados.

Motor Inteligente de Contingência (Fallback): Mecanismo de defesa resiliente que mantém a aplicação 100% funcional caso os limites de cota de chamadas de APIs externas (como Google Gemini) sejam atingidos.

🛠️ Tecnologias e Arquitetura

O ecossistema foi desenvolvido utilizando arquitetura de microsserviços/módulos acoplada a um modelo de desenvolvimento rápido e resiliente:

Linguagem: Java 21 LTS

Framework Core: Spring Boot 3.3.4

Persistência: Spring Data JPA / Hibernate ORM

Banco de Dados: MySQL Server / H2 Database em memória (para testes)

Engine Cognitiva: Google Gemini API (Modelo gemini-1.5-flash) via Client REST HTTP

Motor de Template: Thymeleaf (Renderização robusta no lado do servidor)

Styling: Tailwind CSS via CDN com suporte completo a Dark Mode

Manipulação de JSON: Jackson Databind

⚙️ Configuração e Inicialização Local

Pré-requisitos

Java JDK 21 instalado.

Maven 3.x configurado nas variáveis de ambiente.

MySQL Server ativo (caso utilize o perfil de produção local).

Passo 1: Clonar o Repositório

git clone https://github.com/No-Country-simulation/S06-26-AB-EQUIPE-68.git
cd S06-26-AB-EQUIPE-68


Passo 2: Configurar Variáveis de Ambiente

Crie um arquivo .env ou configure na inicialização da sua IDE a chave de acesso do Google Gemini:

GEMINI_API_KEY=sua_chave_do_google_ai_studio_aqui


Passo 3: Compilar o Projeto

Use o Maven Wrapper ou instalação global para limpar pacotes legados e gerar o novo empacotamento:

mvn clean compile


Passo 4: Executar a Aplicação

Inicie a aplicação utilizando o plugin nativo do Spring Boot:

mvn spring-boot:run

O servidor estará disponível para acesso em: http://localhost:8080


👥 Equipe de Desenvolvimento (Grupo 68) - Integrante


Andre Teixeira - Backend Developer & Tech Leader

Carlos Alexandre - Full Stack Developer

Tiago Farias - AI Engineer

Daniela Vieira - QA Engineer



📄 Licença = Este projeto é de uso exclusivo para fins acadêmicos e de avaliação de simulação de desenvolvimento de MVPs no ambiente No Country (2026).
