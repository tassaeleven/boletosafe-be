# BoletoSafe — Backend (3 microsserviços)

Java 21 · Spring Boot 4.1.1 · Maven · pacote `com.example`

Este documento explica **o que cada arquivo faz**, serviço por serviço.
Se você é novo no projeto, leia primeiro a seção "Como tudo se conecta"
no fim — ela mostra o caminho de uma requisição de ponta a ponta.

⚠️ **Aviso de honestidade:** este backend foi escrito sem acesso ao Maven
Central no ambiente onde foi gerado, então nunca rodou `mvn compile` de
verdade. A primeira compilação real vai acontecer na sua máquina. Se
aparecer erro, é provável que seja pequeno (import faltando, nome de
método de uma lib) — me manda o log que eu conserto rápido.

---

## Como rodar

```bash
# sobe Postgres (3 bancos) + RabbitMQ + os 3 serviços
docker compose up --build
```

Ou, rodando cada serviço na mão (útil para depurar um de cada vez):

```bash
cd auth-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd boleto-risk-engine && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd email-scanner-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Nesse caso você precisa ter Postgres e RabbitMQ rodando localmente e
ajustar as variáveis de ambiente (ver `application.yml` de cada serviço).

O perfil `dev` ativa os `DataSeeder`s (dados de teste) — sem ele, os
bancos sobem vazios.

---

## Portas e responsabilidades

| Serviço | Porta | Responsabilidade |
|---|---|---|
| `auth-service` | 8081 | Login e emissão de JWT |
| `boleto-risk-engine` | 8082 | Regras de risco, score, alertas |
| `email-scanner-service` | 8083 | Varredura de e-mail (Gmail), extração de PDF |

Cada serviço tem **seu próprio banco Postgres** (database-per-service) —
`boletosafe_auth`, `boletosafe_risk`, `boletosafe_scanner`. Isso é
proposital: reforça que são serviços independentes de verdade, e é um
ponto que vale citar pra banca (Sistemas Distribuídos).

---

## 1. auth-service

```
src/main/java/com/example/authservice/
├── AuthServiceApplication.java    # ponto de entrada (main), liga o Spring Boot
├── config/
│   ├── JwtProperties.java         # lê "boletosafe.jwt.*" do application.yml
│   ├── SecurityConfig.java        # define o que é público (login) x protegido
│   └── DataSeeder.java            # cria usuário de teste (perfil "dev")
├── domain/
│   ├── Perfil.java                 # tabela PERFIL do ERD
│   └── Usuario.java                 # tabela USUARIO do ERD (senha em hash, nunca em texto puro)
├── repository/
│   ├── PerfilRepository.java
│   └── UsuarioRepository.java      # findByEmail() usado no login
├── dto/
│   ├── LoginRequest.java           # { email, senha } — igual ao que o Angular envia
│   ├── LoginResponse.java          # { token, usuarioId, nome, perfil } — igual ao AuthService.ts
│   └── ErroResponse.java           # formato padrão de erro
├── security/
│   ├── JwtService.java              # GERA o token (só este serviço emite)
│   └── JwtAuthenticationFilter.java # lê o header Authorization e autentica a requisição
├── service/
│   └── AuthService.java             # regra do login: busca usuário, confere senha, gera token
├── controller/
│   └── AuthController.java          # expõe POST /api/v1/auth/login
└── exception/
    ├── CredenciaisInvalidasException.java
    └── GlobalExceptionHandler.java  # transforma exceptions em respostas HTTP consistentes
```

**Fluxo do login:** `AuthController` recebe o `LoginRequest` → `AuthService`
busca o `Usuario` pelo e-mail, confere a senha com `PasswordEncoder`
(BCrypt) → se bater, `JwtService` gera o token → devolve `LoginResponse`.

---

## 2. boleto-risk-engine (o maior dos três)

```
src/main/java/com/example/boletoriskengine/
├── BoletoRiskEngineApplication.java
├── domain/                      # as 9 entidades do seu ERD (Figma)
│   ├── BancoInstituicao.java
│   ├── OrigemCobranca.java
│   ├── Beneficiario.java         # guarda ultimoBancoConhecido — chave da regra mais forte
│   ├── Boleto.java
│   ├── AnaliseRisco.java         # registro central de cada análise feita
│   ├── NivelRisco.java           # enum BAIXO / ATENCAO / ALTO
│   ├── RegraRisco.java           # CONFIGURAÇÃO de cada regra (peso, se está ativa)
│   ├── ResultadoRegra.java       # uma linha por regra avaliada (disparou ou não, e por quê)
│   ├── Alerta.java
│   └── LogAuditoria.java
├── repository/                  # um JpaRepository por entidade acima
├── dto/
│   ├── BoletoAnaliseRequest.java   # espelha boleto.model.ts do frontend
│   ├── BoletoAnaliseResultado.java # espelha boleto.model.ts do frontend
│   ├── AlertaResponse.java         # espelha alerta.model.ts do frontend
│   └── ErroResponse.java
├── regras/                       # O MOTOR DE REGRAS — strategy pattern
│   ├── RegraDeRisco.java          # interface que toda regra implementa
│   ├── RegraContexto.java         # "pacote" com tudo que uma regra pode consultar
│   ├── ResultadoAvaliacaoRegra.java # resultado de rodar 1 regra (disparou? por quê?)
│   ├── RegraDigitoVerificadorLinhaDigitavel.java   # [estrutural] valida DV da linha digitável
│   ├── RegraBancoEmissorValido.java                # [estrutural] banco existe?
│   ├── RegraDocumentoBeneficiarioValido.java        # [beneficiário] CPF/CNPJ válido?
│   ├── RegraBeneficiarioNovo.java                    # [beneficiário] 1ª vez que aparece
│   ├── RegraMudancaDadosBancarios.java                # [beneficiário] ⚠️ sinal mais forte de golpe
│   ├── RegraNomeSimilarSuspeito.java                  # [beneficiário] nome "quase igual" a outro conhecido
│   ├── RegraValorForaMediaHistorica.java              # [valor] valor muito acima da média do beneficiário
│   ├── RegraVencimentoProximoDemais.java               # [vencimento] vence hoje/já venceu (urgência)
│   ├── RegraOrigemCobrancaSuspeita.java                 # [origem] veio por e-mail/WhatsApp/SMS
│   └── RegraReincidenciaAlertaAlto.java                  # [reincidência] beneficiário já deu alerta ALTO antes
├── service/
│   ├── BoletoRiskEngineService.java  # ORQUESTRADOR: monta contexto, roda as regras, soma score, salva tudo
│   └── AlertaService.java            # lista alertas (com filtro por status)
├── controller/
│   ├── BoletoAnaliseController.java  # POST /api/v1/boleto/analise
│   └── AlertaController.java         # GET  /api/v1/boleto/alertas
├── messaging/
│   ├── RabbitMQConfig.java           # declara a fila compartilhada com o Email Scanner
│   ├── BoletoEncontradoMessage.java  # formato da mensagem recebida
│   └── BoletoEncontradoListener.java # consome a fila e roda a MESMA análise do fluxo manual
├── security/                     # igual ao auth-service, mas só VALIDA token (não gera)
├── config/
│   ├── JwtProperties.java / SecurityConfig.java
│   └── DataSeeder.java            # cria as 10 regras + bancos + origens de cobrança
└── exception/GlobalExceptionHandler.java
```

**Como o score é calculado** (`BoletoRiskEngineService.analisar`):
1. Resolve/cria `Beneficiario` e `Boleto`.
2. Monta um `RegraContexto` com tudo que as regras podem precisar
   (beneficiário já existia? qual o banco? qual a média histórica?).
3. Busca as `RegraRisco` ativas no banco e, pra cada uma, roda a
   implementação `RegraDeRisco` correspondente (casadas pelo campo `codigo`).
4. Soma o peso de cada regra que disparou → `scoreTotal` (capado em 100).
5. `0–30 = BAIXO`, `31–70 = ATENÇÃO`, `71–100 = ALTO`.
6. Salva `AnaliseRisco` + um `ResultadoRegra` por regra avaliada (mesmo as
   que não dispararam — isso é o que torna o resultado explicável).
7. Se o nível não for BAIXO, cria um `Alerta` pra cada regra que disparou.

---

## 3. email-scanner-service

```
src/main/java/com/example/emailscannerservice/
├── EmailScannerServiceApplication.java
├── domain/
│   ├── EmailBoletoEncontrado.java  # cada boleto (ou tentativa) achado no e-mail
│   ├── StatusBoletoEncontrado.java # enum: BLOQUEADO / ANALISADO / ERRO_EXTRACAO
│   └── PerfilSenhaUsuario.java     # CPF/data nasc. que o usuário cadastra por opção (p/ chutar senha de PDF)
├── repository/
├── dto/
│   ├── BoletoEncontradoResponse.java
│   ├── DesbloquearBoletoRequest.java  # { senha } — desbloqueio manual
│   ├── CadastrarPerfilSenhaRequest.java
│   └── ErroResponse.java
├── service/
│   ├── GmailScannerService.java        # fala com a Gmail API (busca e-mails, baixa anexo PDF)
│   ├── PdfBoletoExtractorService.java  # abre o PDF (com/sem senha) e extrai os campos via regex
│   ├── SenhaPatternService.java        # gera candidatos de senha (CPF, data nascimento)
│   └── EmailScannerOrchestrationService.java  # ORQUESTRADOR: junta os 3 acima + publica na fila
├── controller/
│   └── EmailScannerController.java     # os 4 endpoints (ver abaixo)
├── messaging/
│   ├── RabbitMQConfig.java             # MESMA fila do boleto-risk-engine
│   ├── BoletoEncontradoMessage.java
│   └── BoletoQueuePublisher.java
├── security/                       # igual aos outros dois (valida JWT)
├── config/
│   ├── JwtProperties.java
│   └── SecurityConfig.java          # única diferença: também habilita OAuth2 login (Google)
└── exception/
    ├── BoletoNaoEncontradoException.java
    ├── SenhaIncorretaException.java
    └── GlobalExceptionHandler.java
```

**Endpoints:**
- `POST /api/v1/email-scanner/perfil-senha` — usuário cadastra CPF/nascimento (opcional, pra habilitar o chute de senha)
- `POST /api/v1/email-scanner/sincronizar` — dispara a varredura no Gmail
- `GET /api/v1/email-scanner/boletos` — lista o que foi encontrado (com status)
- `POST /api/v1/email-scanner/boletos/{id}/desbloquear` — usuário informa a senha manualmente

**Fluxo por boleto encontrado** (`EmailScannerOrchestrationService.sincronizar`):
1. `GmailScannerService` busca e-mails com anexo PDF dos últimos 30 dias.
2. `PdfBoletoExtractorService` tenta abrir sem senha.
3. Se falhar, tenta os candidatos do `SenhaPatternService` (baseados no `PerfilSenhaUsuario`, se cadastrado).
4. Deu certo → extrai os dados, publica na fila (`BoletoQueuePublisher`) → o `boleto-risk-engine` roda a análise normalmente.
5. Não deu certo → salva como `BLOQUEADO`, guardando o PDF (base64) pra permitir desbloqueio manual depois.

### ⚠️ Limitações honestas deste serviço (importante saber antes de apresentar)

- **Extração por regex, não OCR real.** `PdfBoletoExtractorService` funciona
  bem em PDFs "nativos" com texto selecionável no layout Febraban padrão.
  Boletos escaneados como imagem, ou em layouts muito fora do padrão,
  caem como `ERRO_EXTRACAO`.
- **Autenticação híbrida simplificada.** Este serviço mistura JWT
  (stateless, como os outros dois) com sessão OAuth2 do Google (que
  precisa de estado). Por isso o endpoint `/sincronizar` ainda recebe
  `usuarioId` como query param em vez de extrair do JWT — está comentado
  no `EmailScannerController`. Unificar isso é um próximo passo natural.
- **Pré-requisito fora do código:** você precisa criar um projeto no
  Google Cloud Console, ativar a Gmail API e configurar um OAuth Client
  ID, colocando `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` no `.env` (ver
  `docker-compose.yml`). Sem isso, o login com Google não funciona.

---

## Como os 3 serviços se conectam

```
Frontend Angular
   │
   ├── POST /api/v1/auth/login ─────────────► auth-service (gera JWT)
   │
   ├── POST /api/v1/boleto/analise ─────────► boleto-risk-engine (roda as regras)
   │        (Authorization: Bearer <JWT>)         │
   │                                               ├─ salva AnaliseRisco + ResultadoRegra
   │                                               └─ cria Alerta se risco > BAIXO
   │
   └── GET  /api/v1/boleto/alertas ──────────► boleto-risk-engine

email-scanner-service
   │
   ├── Gmail API ── busca e extrai boletos do e-mail
   │
   └── publica na fila RabbitMQ ─────────────► boleto-risk-engine consome
            "boletosafe.email-scanner.boletos-encontrados"    e roda a MESMA análise
```

O JWT é validado da mesma forma nos 3 serviços (mesma chave secreta,
`JWT_SECRET`), mas só o `auth-service` sabe **gerar** um token novo — os
outros dois só conferem se um token que chegou é válido. Isso evita que
o Risk Engine e o Email Scanner precisem chamar o Auth Service a cada
requisição.

---

## Próximos passos sugeridos

- Trocar `ddl-auto: update` por migrations versionadas (Flyway/Liquibase) antes de qualquer deploy real.
- Testes unitários das regras em `regras/` — são pequenas e fáceis de testar isoladas (só montar um `RegraContexto` na mão).
- Unificar a autenticação híbrida do `email-scanner-service` (ver limitação acima).
- Adicionar circuit breaker (Resilience4j) na comunicação entre serviços, já que é um ponto que costuma ser cobrado em Sistemas Distribuídos.
