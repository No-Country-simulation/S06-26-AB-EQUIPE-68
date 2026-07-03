// Infraestrutura central de i18n do BiT App (PT/ES). Fonte única de verdade:
// localStorage 'bit_idioma'. Escopo declarado: conteúdo de DADOS (cursos/vagas
// do catálogo, pontos de Lazer) permanece PT; INTERFACE e textos gerados pelo
// backend (acolhimento, fallbacks, comoResolver) viram bilíngues.
const IDIOMA_KEY = 'bit_idioma';

export function getIdioma() {
    return localStorage.getItem(IDIOMA_KEY) === 'es' ? 'es' : 'pt';
}

export function setIdioma(lang) {
    localStorage.setItem(IDIOMA_KEY, lang === 'es' ? 'es' : 'pt');
    location.reload();
}

const DICT = {
    pt: {
        nav: {
            dashboard: 'Dashboard', vagas: 'Vagas', cursos: 'Cursos', lazer: 'Lazer',
            saudeMental: 'Saúde Mental', meuPerfil: 'Meu Perfil', sair: 'Sair',
            login: 'Login', cadastro: 'Cadastro', meuPainel: 'Meu Painel',
            abrirMenu: 'Abrir menu',
        },
        common: {
            termosDeUso: 'Termos de Uso', suporteCvv: 'Suporte 188 (CVV)',
            copyright: '© 2026 Ecossistema de Inclusão Tecnológica.',
            redeEstavel: 'Rede Estável — {tec}', redeInstavel: 'Rede Instável',
            statusRede: 'Status da rede', pularConteudo: 'Pular para o conteúdo',
            mapaInterativo: 'Mapa interativo',
        },
        index: {
            bemVindo: 'Bem-vindo de volta', subtitulo: 'Acesse sua conta para continuar sua jornada.',
            authRequired: 'Você precisa fazer login ou criar uma conta para acessar este conteúdo.',
            emailLabel: 'E-mail', senhaLabel: 'Senha', entrar: 'Entrar', entrando: 'Entrando...',
            senhaHintEmail: 'Por favor, insira um e-mail válido.',
            senhaHintSenha: 'A senha deve ter no mínimo 6 caracteres.',
            primeiraVez: 'Primeira vez no BiT App?', criarConta: 'Primeiro Acesso — Criar Conta',
            acessibilidadeTitulo: 'Acessibilidade',
            acessibilidadeTab: 'Use <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Tab</kbd> para navegar entre os campos do formulário.',
            acessibilidadeEnter: 'Pressione <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Enter</kbd> para enviar o formulário.',
            acessibilidadeRotulos: 'Todos os campos possuem rótulos acessíveis para leitores de tela.',
            acessibilidadeCvv: 'Em caso de dificuldade, ligue para <a href="tel:188" class="text-rose-400 font-semibold hover:text-rose-300 transition">188 (CVV)</a>.',
            erroLogin: 'E-mail ou senha incorretos.',
        },
        cadastro: {
            titulo: 'Crie sua conta', subtitulo: 'Comece sua jornada na tecnologia agora.',
            nomeCompleto: 'Nome Completo', email: 'E-mail', senha: 'Senha',
            senhaMinimo: '(mínimo 6 caracteres)', senhaHint: 'Use pelo menos 6 caracteres com letras e números.',
            cidadeRegiao: 'Cidade / Região', selecioneRegiao: 'Selecione sua região',
            whatsapp: 'WhatsApp', momentoCarreira: 'Momento de Carreira',
            estudante: 'Estudante', transicaoCarreira: 'Transição de Carreira', graduadoSemExp: 'Graduado sem experiência',
            areaAlvo: 'Área Alvo', habilidadesAtuais: 'Habilidades Atuais',
            habilidadesPlaceholder: 'Ex: HTML, CSS, Java básico',
            criarConta: 'Criar Conta', criando: 'Criando...', limpar: 'Limpar',
            jaTemConta: 'Já tem uma conta?', voltarLogin: 'Voltar para o Login',
            acessibilidadeTitulo: 'Acessibilidade',
            acessibilidadeTab: 'Use <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Tab</kbd> para navegar entre os campos do formulário.',
            acessibilidadeObrigatorios: 'Campos obrigatórios estão marcados com <span class="text-rose-400">*</span>.',
            acessibilidadeRotulos: 'Todos os campos possuem rótulos acessíveis para leitores de tela.',
            acessibilidadeEnter: 'Use <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Enter</kbd> para enviar o formulário.',
            acessibilidadeCvv: 'Em caso de dificuldade, ligue para <a href="tel:188" class="text-rose-400 font-semibold hover:text-rose-300">188 (CVV)</a>.',
            erroCamposObrigatorios: 'Preencha todos os campos obrigatórios corretamente.',
            erroGenerico: 'Erro ao criar conta. Tente novamente.',
        },
        dashboard: {
            ola: 'Olá, {nome}', matchOportunidades: '🎯 Match de Oportunidades',
            lacunas: 'Lacunas:', aguardando: 'Aguardando...',
            trilhaFormacao: '📚 Trilha de Formação', aguardandoRecomendacoes: 'Aguardando recomendações...',
            recomendado: 'Recomendado', oportunidadesRegiao: 'Oportunidades na sua região',
            vagasTitulo: 'Vagas', cursosTitulo: '📚 Cursos', baseadoNaRegiao: 'Baseado na sua região — {regiao}',
            nivelEstimado: 'Nível estimado: {nivel}', perfilAnalisado: 'Perfil analisado',
            pontosFortes: 'Pontos fortes: {lista}.',
            compatibilidadeMercado: 'Compatibilidade de {pct}% com o mercado. Foque no seu plano de desenvolvimento.',
            analiseIndisponivel: 'Análise indisponível',
        },
        cursos: {
            categoria: 'Formação Profissional', titulo: 'Cursos Profissionalizantes',
            subtitulo: 'Capacitação em tecnologia com foco em cursos gratuitos e instituições beneficentes.',
            statsTexto: 'cursos em', regioes: 'regiões',
            todos: 'Todos', gratuitos: '🆓 Gratuitos', beneficentes: '❤️ Beneficentes',
            buscarPlaceholder: 'Buscar por curso, instituição ou área...',
            todasRegioes: 'Todas as regiões', todasAreas: 'Todas as áreas', todasModalidades: 'Todas as modalidades',
            carregando: 'Carregando cursos...', nenhumEncontrado: 'Nenhum curso encontrado',
            ajusteFiltros: 'Tente ajustar os filtros de busca.', encontrados: '{n} curso(s) encontrado(s)',
            gratuito: 'Gratuito', pago: 'Pago', certificado: 'Certificado', verDetalhes: 'Ver detalhes →',
            regiao: 'Região', duracao: 'Duração', vagasLabel: 'Vagas', vagasDisponiveis: '{n} disponíveis', area: 'Área',
            sobreCurso: 'Sobre o Curso', semDescricao: 'Sem descrição detalhada.', publicadoEm: 'Publicado em {data}',
            inscreverSe: 'Inscrever-se', inscrevendo: 'Inscrevendo...', inscrito: 'Inscrito ✓',
            inscricaoSucesso: 'Inscrição realizada com sucesso!', erroInscrever: 'Erro ao inscrever-se.',
            erroCarregar: 'Erro ao carregar cursos', indisponivel: 'Cursos indisponíveis no momento',
            verifiqueBackend: 'Verifique se o backend está rodando.',
        },
        lazer: {
            categoria: 'Lazer & Cultura', titulo: 'Pontos de Interesse',
            subtitulo: 'Parques, teatros, atrações gratuitas e pontos culturais mapeados pela rede VISent.',
            statsTexto: 'pontos em', regioes: 'regiões',
            buscarPlaceholder: 'Buscar por nome, tipo ou região...',
            todasRegioes: 'Todas as regiões', todosTipos: 'Todos os tipos', todos: 'Todos', apenasGratuitos: 'Apenas gratuitos',
            minhaLocalizacao: '📍 Minha localização', carregando: 'Carregando pontos de interesse...',
            nenhumEncontrado: 'Nenhum ponto encontrado com os filtros selecionados.',
            legendaAntena: 'Antena VISent (5G/4G)', legendaParques: 'Parques & Praias',
            legendaTeatros: 'Teatros & Museus', legendaFeiras: 'Feiras & Bibliotecas',
            gratuito: 'Gratuito', pago: 'Pago', acessivel: 'Acessível', acessivelParcial: 'Parcial',
            rota: '🛣️ Rota', noMapa: '📍 No mapa', detalhes: 'Detalhes →',
            horario: 'Horário', acessibilidade: 'Acessibilidade', entrada: 'Entrada', regiao: 'Região',
            voceEstaAqui: 'Você está aqui',
            erroGeoNaoSuportada: 'Seu navegador não suporta geolocalização.',
            erroGeoNegada: 'Permissão de localização negada. Você pode ativá-la nas configurações do navegador.',
            erroGeoFalhou: 'Não foi possível obter sua localização.',
            erroSemLocalizacao: 'Primeiro clique em "Minha localização" para definir o ponto de partida.',
            erroSemCoordenada: 'Este ponto não tem coordenada disponível.',
            rotaTracada: 'Rota traçada: {dist} km - aprox. {min} min de carro até {nome}',
            erroCarregar: 'Erro ao carregar pontos de interesse.',
            zonaTranquila: 'Zona tranquila', zonaModerada: 'Zona moderada', zonaMovimentada: 'Zona movimentada',
        },
        perfil: {
            titulo: '👤 Meu Perfil', subtitulo: 'Edite suas informações e preferências.',
            nomeCompleto: 'Nome Completo', email: 'E-mail', cidadeRegiao: 'Cidade / Região',
            selecioneRegiao: 'Selecione sua região', whatsapp: 'WhatsApp', momentoCarreira: 'Momento de Carreira',
            estudante: 'Estudante', transicaoCarreira: 'Transição de Carreira', graduadoSemExp: 'Graduado sem experiência',
            areaAlvo: 'Área Alvo', habilidadesAtuais: 'Habilidades Atuais',
            habilidadesPlaceholder: 'Ex: HTML, CSS, Java básico',
            salvarAlteracoes: 'Salvar Alterações', salvando: 'Salvando...', cancelar: 'Cancelar',
            sucesso: 'Perfil atualizado com sucesso!', erroNome: 'O nome é obrigatório.',
            erroSalvar: 'Erro ao salvar perfil.',
        },
        saude: {
            titulo: '🌱 Check-in de Saúde Mental', subtitulo: 'Compartilhe seu estado para receber acolhimento.',
            humorLeve: 'Leve', humorCansado: 'Cansado', humorAnsioso: 'Ansioso', humorTriste: 'Triste', humorSobrecarregado: 'Sobrecarregado',
            notaLabel: 'De 0 a 10, que nota você dá para a sua semana?',
            notaAncoraMin: '0 — semana muito difícil', notaAncoraMax: '10 — semana excelente',
            contextoLabel: 'Contexto (opcional)', contextoPlaceholder: 'Compartilhe como se sente...',
            enviarRegistro: 'Enviar Registro', salvando: 'Salvando...',
            modalTitulo: 'Um instante',
            modalMsg: 'Percebemos que a semana foi muito difícil. Você gostaria de receber apoio agora?',
            modalSim: 'Sim, quero apoio', modalNao: 'Cliquei sem querer',
            reforcadoTitulo: 'Estamos com você', reforcadoCvv: 'O CVV está disponível agora, 24h — ligue 188.',
            preventivoTitulo: 'Um recurso pra você',
            preventivoCvv: 'Conversar ajuda — o 188 escuta 24h, sem julgamento, sobre qualquer assunto.',
            normalTitulo: '✨ Resposta do Agente BiT', acaoLabel: 'Ação:',
            alertaSemNota: 'Escolha uma nota de 0 a 10 para a sua semana.', alertaSemHumor: 'Selecione um emoji.',
            erroSalvar: 'Erro ao salvar check-in.',
            dicasLazerTitulo: '🎯 Dicas de Lazer na sua região', baseadoNaRegiao: 'Baseado na sua região — {regiao}',
            historicoTitulo: '📋 Histórico de Check-ins', historicoVazio: 'Nenhum registro anterior encontrado.',
            historicoErro: 'Erro ao carregar histórico.', semContexto: 'Sem contexto',
        },
        vagas: {
            categoria: 'Mercado de Trabalho', titulo: 'Vagas de Emprego',
            subtitulo: 'Oportunidades mapeadas a partir dos dados de mobilidade regional.',
            statsTexto: 'vagas em', regioes: 'regiões',
            buscarPlaceholder: 'Buscar por título, empresa ou tecnologia...',
            todasRegioes: 'Todas as regiões', todosNiveis: 'Todos os níveis', todasAreas: 'Todas as áreas',
            carregando: 'Carregando vagas...', nenhumaEncontrada: 'Nenhuma vaga encontrada',
            ajusteFiltros: 'Tente ajustar os filtros de busca.', encontradas: '{n} vaga(s) encontrada(s)',
            descricao: 'Descrição', tecnologias: 'Tecnologias', semDescricao: 'Sem descrição detalhada.',
            regiaoLabel: 'Região', salarioLabel: 'Salário', aCombinar: 'A combinar',
            publicadaEm: 'Publicada em {data}', enviarCurriculo: 'Enviar Currículo', enviando: 'Enviando...',
            enviado: 'Enviado ✓', curriculoSucesso: 'Currículo enviado com sucesso!',
            erroCurriculo: 'Erro ao enviar currículo.', erroCarregar: 'Erro ao carregar vagas',
            indisponivel: 'Vagas indisponíveis no momento', verifiqueBackend: 'Verifique se o backend está rodando.',
            matchLabel: 'Match', semDadosBadge: 'Sem dados de match',
            facaLoginBadge: 'Faça login para ver seu match', matchTitulo: 'Compatibilidade com a vaga',
            carregandoMatch: 'Calculando seu match...', facaLoginDetalhe: 'Faça login para ver seu match com esta vaga.',
            semDadosDetalhe: 'Esta vaga não tem tecnologias cadastradas — sem dados de match.',
            voceAtende: 'Você atende {pct}% dos requisitos desta vaga.',
            atendidasTitulo: 'Você já tem:', faltantesTitulo: 'Ainda falta:',
            comoResolverLabel: 'Como resolver:', erroMatch: 'Não foi possível calcular seu match agora.',
        },
        termos: {
            titulo: 'Termos de Uso', selo: 'Documento de demonstração — hackathon', voltar: 'Voltar',
            p1: 'Este documento é um placeholder criado para fins de demonstração do BiT App durante um hackathon. Ele não constitui um Termo de Uso juridicamente válido e não deve ser tratado como tal em um produto em produção.',
            p2: 'Em uma versão real, esta página descreveria as condições de uso da plataforma, incluindo cadastro, coleta e tratamento de dados pessoais (em conformidade com a LGPD), responsabilidades do usuário e da equipe responsável pelo BiT App, e as regras de utilização das funcionalidades de vagas, cursos, lazer e suporte à saúde mental.',
            p3: 'Também constariam aqui informações sobre limitação de responsabilidade — por exemplo, deixando claro que o BiT App é uma ferramenta de apoio e orientação, e não substitui atendimento profissional em situações de saúde mental, sendo o CVV (188) e demais canais especializados sempre a referência para situações de crise.',
            p4: 'Como este é um projeto de hackathon, recomendamos que qualquer uso além de fins de demonstração passe antes por revisão jurídica adequada.',
        },
    },
    es: {
        nav: {
            dashboard: 'Panel', vagas: 'Empleos', cursos: 'Cursos', lazer: 'Ocio',
            saudeMental: 'Salud Mental', meuPerfil: 'Mi Perfil', sair: 'Salir',
            login: 'Iniciar sesión', cadastro: 'Registro', meuPainel: 'Mi Panel',
            abrirMenu: 'Abrir menú',
        },
        common: {
            termosDeUso: 'Términos de Uso', suporteCvv: 'Soporte 188 (CVV)',
            copyright: '© 2026 Ecosistema de Inclusión Tecnológica.',
            redeEstavel: 'Red Estable — {tec}', redeInstavel: 'Red Inestable',
            statusRede: 'Estado de la red', pularConteudo: 'Saltar al contenido',
            mapaInterativo: 'Mapa interactivo',
        },
        index: {
            bemVindo: 'Bienvenido de nuevo', subtitulo: 'Accede a tu cuenta para continuar tu camino.',
            authRequired: 'Necesitas iniciar sesión o crear una cuenta para acceder a este contenido.',
            emailLabel: 'Correo electrónico', senhaLabel: 'Contraseña', entrar: 'Entrar', entrando: 'Entrando...',
            senhaHintEmail: 'Por favor, ingresa un correo electrónico válido.',
            senhaHintSenha: 'La contraseña debe tener al menos 6 caracteres.',
            primeiraVez: '¿Primera vez en BiT App?', criarConta: 'Primer Acceso — Crear Cuenta',
            acessibilidadeTitulo: 'Accesibilidad',
            acessibilidadeTab: 'Usa <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Tab</kbd> para navegar entre los campos del formulario.',
            acessibilidadeEnter: 'Presiona <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Enter</kbd> para enviar el formulario.',
            acessibilidadeRotulos: 'Todos los campos tienen etiquetas accesibles para lectores de pantalla.',
            acessibilidadeCvv: 'Si tienes dificultades, llama al <a href="tel:188" class="text-rose-400 font-semibold hover:text-rose-300 transition">188 (CVV)</a>.',
            erroLogin: 'Correo electrónico o contraseña incorrectos.',
        },
        cadastro: {
            titulo: 'Crea tu cuenta', subtitulo: 'Comienza tu camino en la tecnología ahora.',
            nomeCompleto: 'Nombre Completo', email: 'Correo electrónico', senha: 'Contraseña',
            senhaMinimo: '(mínimo 6 caracteres)', senhaHint: 'Usa al menos 6 caracteres con letras y números.',
            cidadeRegiao: 'Ciudad / Región', selecioneRegiao: 'Selecciona tu región',
            whatsapp: 'WhatsApp', momentoCarreira: 'Momento de Carrera',
            estudante: 'Estudiante', transicaoCarreira: 'Transición de Carrera', graduadoSemExp: 'Graduado sin experiencia',
            areaAlvo: 'Área Objetivo', habilidadesAtuais: 'Habilidades Actuales',
            habilidadesPlaceholder: 'Ej: HTML, CSS, Java básico',
            criarConta: 'Crear Cuenta', criando: 'Creando...', limpar: 'Limpiar',
            jaTemConta: '¿Ya tienes una cuenta?', voltarLogin: 'Volver al inicio de sesión',
            acessibilidadeTitulo: 'Accesibilidad',
            acessibilidadeTab: 'Usa <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Tab</kbd> para navegar entre los campos del formulario.',
            acessibilidadeObrigatorios: 'Los campos obligatorios están marcados con <span class="text-rose-400">*</span>.',
            acessibilidadeRotulos: 'Todos los campos tienen etiquetas accesibles para lectores de pantalla.',
            acessibilidadeEnter: 'Usa <kbd class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono text-[10px]">Enter</kbd> para enviar el formulario.',
            acessibilidadeCvv: 'Si tienes dificultades, llama al <a href="tel:188" class="text-rose-400 font-semibold hover:text-rose-300">188 (CVV)</a>.',
            erroCamposObrigatorios: 'Completa todos los campos obligatorios correctamente.',
            erroGenerico: 'Error al crear la cuenta. Inténtalo de nuevo.',
        },
        dashboard: {
            ola: 'Hola, {nome}', matchOportunidades: '🎯 Match de Oportunidades',
            lacunas: 'Brechas:', aguardando: 'Esperando...',
            trilhaFormacao: '📚 Ruta de Formación', aguardandoRecomendacoes: 'Esperando recomendaciones...',
            recomendado: 'Recomendado', oportunidadesRegiao: 'Oportunidades en tu región',
            vagasTitulo: 'Empleos', cursosTitulo: '📚 Cursos', baseadoNaRegiao: 'Basado en tu región — {regiao}',
            nivelEstimado: 'Nivel estimado: {nivel}', perfilAnalisado: 'Perfil analizado',
            pontosFortes: 'Puntos fuertes: {lista}.',
            compatibilidadeMercado: 'Compatibilidad del {pct}% con el mercado. Enfócate en tu plan de desarrollo.',
            analiseIndisponivel: 'Análisis no disponible',
        },
        cursos: {
            categoria: 'Formación Profesional', titulo: 'Cursos de Formación Profesional',
            subtitulo: 'Capacitación en tecnología enfocada en cursos gratuitos e instituciones benéficas.',
            statsTexto: 'cursos en', regioes: 'regiones',
            todos: 'Todos', gratuitos: '🆓 Gratuitos', beneficentes: '❤️ Benéficos',
            buscarPlaceholder: 'Buscar por curso, institución o área...',
            todasRegioes: 'Todas las regiones', todasAreas: 'Todas las áreas', todasModalidades: 'Todas las modalidades',
            carregando: 'Cargando cursos...', nenhumEncontrado: 'Ningún curso encontrado',
            ajusteFiltros: 'Intenta ajustar los filtros de búsqueda.', encontrados: '{n} curso(s) encontrado(s)',
            gratuito: 'Gratuito', pago: 'Pago', certificado: 'Certificado', verDetalhes: 'Ver detalles →',
            regiao: 'Región', duracao: 'Duración', vagasLabel: 'Vacantes', vagasDisponiveis: '{n} disponibles', area: 'Área',
            sobreCurso: 'Sobre el Curso', semDescricao: 'Sin descripción detallada.', publicadoEm: 'Publicado el {data}',
            inscreverSe: 'Inscribirse', inscrevendo: 'Inscribiendo...', inscrito: 'Inscrito ✓',
            inscricaoSucesso: '¡Inscripción realizada con éxito!', erroInscrever: 'Error al inscribirse.',
            erroCarregar: 'Error al cargar cursos', indisponivel: 'Cursos no disponibles en este momento',
            verifiqueBackend: 'Verifica que el backend esté funcionando.',
        },
        lazer: {
            categoria: 'Ocio & Cultura', titulo: 'Puntos de Interés',
            subtitulo: 'Parques, teatros, atracciones gratuitas y puntos culturales mapeados por la red VISent.',
            statsTexto: 'puntos en', regioes: 'regiones',
            buscarPlaceholder: 'Buscar por nombre, tipo o región...',
            todasRegioes: 'Todas las regiones', todosTipos: 'Todos los tipos', todos: 'Todos', apenasGratuitos: 'Solo gratuitos',
            minhaLocalizacao: '📍 Mi ubicación', carregando: 'Cargando puntos de interés...',
            nenhumEncontrado: 'Ningún punto encontrado con los filtros seleccionados.',
            legendaAntena: 'Antena VISent (5G/4G)', legendaParques: 'Parques y Playas',
            legendaTeatros: 'Teatros y Museos', legendaFeiras: 'Ferias y Bibliotecas',
            gratuito: 'Gratuito', pago: 'Pago', acessivel: 'Accesible', acessivelParcial: 'Parcial',
            rota: '🛣️ Ruta', noMapa: '📍 En el mapa', detalhes: 'Detalles →',
            horario: 'Horario', acessibilidade: 'Accesibilidad', entrada: 'Entrada', regiao: 'Región',
            voceEstaAqui: 'Estás aquí',
            erroGeoNaoSuportada: 'Tu navegador no admite geolocalización.',
            erroGeoNegada: 'Permiso de ubicación denegado. Puedes activarlo en la configuración de tu navegador.',
            erroGeoFalhou: 'No fue posible obtener tu ubicación.',
            erroSemLocalizacao: 'Primero haz clic en "Mi ubicación" para definir el punto de partida.',
            erroSemCoordenada: 'Este punto no tiene coordenadas disponibles.',
            rotaTracada: 'Ruta trazada: {dist} km - aprox. {min} min en auto hasta {nome}',
            erroCarregar: 'Error al cargar puntos de interés.',
            zonaTranquila: 'Zona tranquila', zonaModerada: 'Zona moderada', zonaMovimentada: 'Zona concurrida',
        },
        perfil: {
            titulo: '👤 Mi Perfil', subtitulo: 'Edita tu información y preferencias.',
            nomeCompleto: 'Nombre Completo', email: 'Correo electrónico', cidadeRegiao: 'Ciudad / Región',
            selecioneRegiao: 'Selecciona tu región', whatsapp: 'WhatsApp', momentoCarreira: 'Momento de Carrera',
            estudante: 'Estudiante', transicaoCarreira: 'Transición de Carrera', graduadoSemExp: 'Graduado sin experiencia',
            areaAlvo: 'Área Objetivo', habilidadesAtuais: 'Habilidades Actuales',
            habilidadesPlaceholder: 'Ej: HTML, CSS, Java básico',
            salvarAlteracoes: 'Guardar Cambios', salvando: 'Guardando...', cancelar: 'Cancelar',
            sucesso: '¡Perfil actualizado con éxito!', erroNome: 'El nombre es obligatorio.',
            erroSalvar: 'Error al guardar el perfil.',
        },
        saude: {
            titulo: '🌱 Check-in de Salud Mental', subtitulo: 'Comparte tu estado para recibir acompañamiento.',
            humorLeve: 'Leve', humorCansado: 'Cansado', humorAnsioso: 'Ansioso', humorTriste: 'Triste', humorSobrecarregado: 'Sobrecargado',
            notaLabel: 'De 0 a 10, ¿qué nota le das a tu semana?',
            notaAncoraMin: '0 — semana muy difícil', notaAncoraMax: '10 — semana excelente',
            contextoLabel: 'Contexto (opcional)', contextoPlaceholder: 'Comparte cómo te sientes...',
            enviarRegistro: 'Enviar Registro', salvando: 'Guardando...',
            modalTitulo: 'Un momento',
            modalMsg: 'Notamos que la semana fue muy difícil. ¿Te gustaría recibir apoyo ahora?',
            modalSim: 'Sí, quiero apoyo', modalNao: 'Fue sin querer',
            reforcadoTitulo: 'Estamos contigo', reforcadoCvv: 'El CVV está disponible ahora, 24h — llama al 188.',
            preventivoTitulo: 'Un recurso para ti',
            preventivoCvv: 'Hablar ayuda — el 188 escucha 24h, sin juzgar, sobre cualquier tema.',
            normalTitulo: '✨ Respuesta del Agente BiT', acaoLabel: 'Acción:',
            alertaSemNota: 'Elige una nota de 0 a 10 para tu semana.', alertaSemHumor: 'Selecciona un emoji.',
            erroSalvar: 'Error al guardar el registro.',
            dicasLazerTitulo: '🎯 Ideas de Ocio en tu región', baseadoNaRegiao: 'Basado en tu región — {regiao}',
            historicoTitulo: '📋 Historial de Check-ins', historicoVazio: 'Ningún registro anterior encontrado.',
            historicoErro: 'Error al cargar el historial.', semContexto: 'Sin contexto',
        },
        vagas: {
            categoria: 'Mercado Laboral', titulo: 'Empleos',
            subtitulo: 'Oportunidades mapeadas a partir de los datos de movilidad regional.',
            statsTexto: 'empleos en', regioes: 'regiones',
            buscarPlaceholder: 'Buscar por título, empresa o tecnología...',
            todasRegioes: 'Todas las regiones', todosNiveis: 'Todos los niveles', todasAreas: 'Todas las áreas',
            carregando: 'Cargando empleos...', nenhumaEncontrada: 'Ningún empleo encontrado',
            ajusteFiltros: 'Intenta ajustar los filtros de búsqueda.', encontradas: '{n} empleo(s) encontrado(s)',
            descricao: 'Descripción', tecnologias: 'Tecnologías', semDescricao: 'Sin descripción detallada.',
            regiaoLabel: 'Región', salarioLabel: 'Salario', aCombinar: 'A convenir',
            publicadaEm: 'Publicada el {data}', enviarCurriculo: 'Enviar Currículum', enviando: 'Enviando...',
            enviado: 'Enviado ✓', curriculoSucesso: '¡Currículum enviado con éxito!',
            erroCurriculo: 'Error al enviar el currículum.', erroCarregar: 'Error al cargar empleos',
            indisponivel: 'Empleos no disponibles en este momento', verifiqueBackend: 'Verifica que el backend esté funcionando.',
            matchLabel: 'Match', semDadosBadge: 'Sin datos de match',
            facaLoginBadge: 'Inicia sesión para ver tu match', matchTitulo: 'Compatibilidad con la vacante',
            carregandoMatch: 'Calculando tu match...', facaLoginDetalhe: 'Inicia sesión para ver tu match con esta vacante.',
            semDadosDetalhe: 'Esta vacante no tiene tecnologías registradas — sin datos de match.',
            voceAtende: 'Cumples con el {pct}% de los requisitos de esta vacante.',
            atendidasTitulo: 'Ya tienes:', faltantesTitulo: 'Todavía falta:',
            comoResolverLabel: 'Cómo resolver:', erroMatch: 'No fue posible calcular tu match ahora.',
        },
        termos: {
            titulo: 'Términos de Uso', selo: 'Documento de demostración — hackathon', voltar: 'Volver',
            p1: 'Este documento es un placeholder creado con fines de demostración del BiT App durante un hackathon. No constituye un Término de Uso jurídicamente válido y no debe tratarse como tal en un producto en producción.',
            p2: 'En una versión real, esta página describiría las condiciones de uso de la plataforma, incluyendo registro, recopilación y tratamiento de datos personales (conforme a la LGPD), responsabilidades del usuario y del equipo responsable del BiT App, y las reglas de uso de las funcionalidades de empleos, cursos, ocio y soporte a la salud mental.',
            p3: 'También constaría aquí información sobre limitación de responsabilidad — por ejemplo, dejando claro que el BiT App es una herramienta de apoyo y orientación, y no sustituye la atención profesional en situaciones de salud mental, siendo el CVV (188) y demás canales especializados siempre la referencia para situaciones de crisis.',
            p4: 'Como este es un proyecto de hackathon, recomendamos que cualquier uso más allá de fines de demostración pase antes por una revisión jurídica adecuada.',
        },
    },
};

export function t(chave, vars) {
    const idioma = getIdioma();
    const buscar = (dict) => chave.split('.').reduce((o, k) => (o && typeof o === 'object') ? o[k] : undefined, dict);
    let valor = buscar(DICT[idioma]);
    if (valor === undefined) valor = buscar(DICT.pt);
    if (valor === undefined) return chave;
    if (vars) {
        Object.keys(vars).forEach(k => { valor = valor.replace(`{${k}}`, vars[k]); });
    }
    return valor;
}

export function applyI18n(root = document) {
    root.querySelectorAll('[data-i18n]').forEach(el => {
        el.textContent = t(el.getAttribute('data-i18n'));
    });
    root.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        el.setAttribute('placeholder', t(el.getAttribute('data-i18n-placeholder')));
    });
    root.querySelectorAll('[data-i18n-aria-label]').forEach(el => {
        el.setAttribute('aria-label', t(el.getAttribute('data-i18n-aria-label')));
    });
    // Uso restrito a chaves do próprio DICT (conteúdo estático nosso, não input do usuário).
    root.querySelectorAll('[data-i18n-html]').forEach(el => {
        el.innerHTML = t(el.getAttribute('data-i18n-html'));
    });
    document.documentElement.setAttribute('lang', getIdioma() === 'es' ? 'es' : 'pt-BR');
}

function renderToggle() {
    const atual = getIdioma();
    const btnClasse = (ativo) => ativo
        ? 'px-1.5 py-0.5 rounded text-cyan-400 font-bold'
        : 'px-1.5 py-0.5 rounded text-slate-500 hover:text-slate-300 transition';
    document.querySelectorAll('[data-idioma-toggle]').forEach(container => {
        container.innerHTML = `
            <button type="button" data-lang="pt" aria-pressed="${atual === 'pt'}" aria-label="Português" class="${btnClasse(atual === 'pt')}">PT</button>
            <span class="text-slate-700 select-none">|</span>
            <button type="button" data-lang="es" aria-pressed="${atual === 'es'}" aria-label="Español" class="${btnClasse(atual === 'es')}">ES</button>
        `;
        container.querySelectorAll('button[data-lang]').forEach(btn => {
            btn.addEventListener('click', () => setIdioma(btn.dataset.lang));
        });
    });
}

function init() {
    applyI18n();
    renderToggle();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}
