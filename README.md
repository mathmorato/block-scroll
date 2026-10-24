# 🛡️ ScrollGuard - Guardião de Bem-Estar Digital & Anti-Doomscrolling

ScrollGuard é um aplicativo Android nativo construído com **Kotlin**, **Jetpack Compose** e **Clean Architecture (MVVM)**, desenvolvido para proteger sua atenção contra algoritmos predatórios de retenção (Reels, Shorts, TikTok), impor pausas conscientes no feed infinito e filtrar domínios adultos.

---

## 🚀 Arquitetura & Stack Tecnológica

O projeto segue as diretrizes modernas do Android Jetpack e Clean Architecture:

- **Linguagem:** Kotlin 2.0+ com Coroutines & Flow (`kotlinx-coroutines-android`).
- **UI & Design:** Jetpack Compose (Material 3), Dark Mode nativo para redução de dopamina visual, animações sutis e gráficos com Compose `Canvas`.
- **Injeção de Dependências:** Dagger Hilt (`hilt-android`, `hilt-navigation-compose`) com KSP.
- **Navegação:** Jetpack Navigation Compose com type-safety e bottom bar dinâmica.
- **Persistência de Dados:**
  - **Room Database (`room-ktx`):** Armazena o log histórico de intervenções (`BlockLogEntity`), estatísticas agregadas por dia/semana e regras por aplicativo (`AppRuleEntity`).
  - **DataStore Preferences:** Armazenamento reativo de switches de proteção, limites de minutos, configurações do Modo Estrito e ofensiva (streak).
- **Widget de Tela Inicial:** Jetpack Glance (`glance-appwidget`, `glance-material3`) com status do escudo e contador de distrações bloqueadas no dia.
- **Background & Sistema:**
  - `ScrollGuardAccessibilityService`: Motor de alta performance para inspeção de nós de acessibilidade (`flagReportViewIds`, `flagRetrieveInteractiveWindows`).
  - `OverlayWindowManager`: Telas sobrepostas em Compose (`TYPE_APPLICATION_OVERLAY`) com contagem regressiva reflexiva de 5 segundos.
  - `WorkManager` (`DailyResetWorker` e `ServiceHealthWorker`): Reset diário de ofensivas e monitoramento de saúde do serviço contra encerramento pelo sistema.

---

## 📂 Estrutura de Pacotes

```text
com.scrollguard.app/
├── ScrollGuardApp.kt              # Configuração Hilt Application e WorkManager
├── MainActivity.kt                # Ponto de entrada Jetpack Compose
├── core/
│   ├── database/                  # Room Database, DAOs e Entidades
│   │   ├── dao/                   # BlockLogDao, AppRuleDao
│   │   └── entity/                # BlockLogEntity, AppRuleEntity
│   ├── datastore/                 # UserPreferencesRepository e DataStore
│   ├── di/                        # Módulos Hilt (Database, DataStore, Repository, App)
│   └── util/                      # DomainTrie (Trie de alta velocidade), PermissionHelper, ManufacturerHelper
├── domain/
│   ├── model/                     # Modelos de domínio (BlockLog, AppRule, GuardianStatus, StatsSummary)
│   ├── repository/                # Interfaces dos repositórios
│   └── usecase/                   # UseCases (LogBlockEvent, CheckUrlSafety, EvaluateScrollSession, GetDashboardStats)
├── data/
│   └── repository/                # Implementações dos repositórios (Room + DataStore)
├── service/
│   ├── accessibility/             # ScrollGuardAccessibilityService
│   │   └── detector/              # Detectores específicos (Instagram, YouTube, TikTok, Facebook, Browser)
│   ├── overlay/                   # OverlayWindowManager e Layouts de intervenção consciente
│   └── worker/                    # DailyResetWorker, ServiceHealthWorker, BootReceiver
├── presentation/
│   ├── common/                    # Componentes reutilizáveis (MetricCard, SwitchCard, StatusBanner, NavigationBar)
│   ├── dashboard/                 # Home Screen, métricas de hoje, toggles rápidos e ViewModel
│   ├── rules/                     # Configurações de bloqueio e limites individuais por aplicativo
│   ├── stats/                     # Gráficos Canvas de bloqueios nos últimos 7 dias e apps mais vorazes
│   ├── strictmode/                # Modo Estrito com barreiras reflexivas (digitação, timer de 30s, PIN)
│   ├── onboarding/                # Diagnóstico de permissões e Guia Anti-Kill para fabricantes (Xiaomi, Samsung, Huawei)
│   └── theme/                     # Paleta Material 3 sóbria e tipografia
└── widget/
    └── ScrollGuardWidget.kt       # Glance AppWidget e Receiver
```

---

## ⚡ Módulos Core em Detalhes

### 1. Motor de Acessibilidade (`ScrollGuardAccessibilityService`)
- **Detecção de Vídeos Curtos:** Inspeciona `AccessibilityNodeInfo` em tempo real em busca de nós como `reel_viewer_root`, `shorts_recycler_view`, `vertical_viewpager` e abas ativas de Reels/Shorts no Instagram, YouTube, TikTok e Facebook.
- **Intervenção:** Ao detectar um vídeo curto, executa imediatamente `GLOBAL_ACTION_BACK`, exibe feedback educativo e salva o tempo economizado no Room Database.
- **Guardião de Rolagem:** Escuta eventos `TYPE_VIEW_SCROLLED` e calcula o tempo de rolagem contínua. Ao atingir o limite estipulado (ex: 10 minutos), exibe uma sobreposição com timer consciente de 5 segundos antes de permitir fechar ou sair para a Home (`GLOBAL_ACTION_HOME`).
- **Bloqueador de Domínios Adultos:** Algoritmo Trie (`DomainTrie`) em memória cruzando URLs lidas da barra de navegação dos navegadores (Chrome, Brave, Firefox, Opera, Edge, Samsung Internet) com uma lista local em `assets/adult_domains.txt`.

### 2. Guia Anti-Kill e Diagnóstico de Fabricante
- Identifica se o aparelho é **Xiaomi / Redmi / POCO** (MIUI / HyperOS), **Samsung** ou **Huawei**.
- Fornece atalhos diretos via Intent para as telas de:
  - **Início Automático (Autostart)**
  - **Cadeado na tela de Recentes**
  - **Exceção de Economia de Bateria (Sem Restrições)**

### 3. Modo Estrito (Anti-Impulso)
- Evita que o usuário desligue a proteção por impulso em momentos de abstinência de dopamina.
- O desbloqueio exige cumprir uma barreira reflexiva:
  - **Digitação Consciente:** Digitar com precisão frases como *"Eu decido ter foco na minha vida real"*.
  - **Timer Reflexivo:** Espera obrigatória de 30 segundos respirando fundo.
  - **PIN de Segurança:** Senha numérica.

---

## 🛠️ Como Compilar e Executar

1. Abra o projeto no **Android Studio Hedgehog ou posterior** (Ladybug / Koala recomendado).
2. O projeto utiliza Gradle Version Catalogs em `gradle/libs.versions.toml`.
3. Sincronize o Gradle (`Sync Project with Gradle Files`).
4. Conecte um dispositivo Android físico (Android 8.0 / API 26 ou superior) ou emulador.
5. Execute a task `assembleDebug` ou clique no botão **Run** (`Shift + F10`).
