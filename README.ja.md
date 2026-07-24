[English](README.md) | [한국어](README.ko.md) | [简体中文](README.zh.md) | **日本語**

# Secure Spring MVC JSP Starter

Secure Spring MVC JSP Starter は、Spring MVC、JSP、MariaDB で構築する業務アプリケーション向けのセキュリティ基盤です。単なるログイン例ではなく、アカウントロック、セッション失効、管理者保護、セキュリティ監査、SQL 観測、コンテナ強化、データベースによる多言語管理、マルチタブのフルスクリーンワークスペースを一つの基準にまとめています。

現在の基準は Java 21、Spring Boot 4.1.0、MariaDB 12.3.2 LTS です。JSP は Spring Boot の実行可能 JAR では利用できないため、アプリケーションは実行可能 WAR としてパッケージ化します。

![Spring MVC Starter の英語ホーム画面](docs/images/home.png)

社内システム、バックオフィス、運用ポータルのように、認証・認可・監査・管理画面を毎回作り直すプロジェクトに適しています。画面だけでなく、アカウントとセッションの不変条件、DB マイグレーション、運用ログ、コンテナ実行、公開前の検証基準も含みます。

## 概要

| 領域 | 標準で含まれるもの |
|---|---|
| サーバー | Java 21、Spring Boot、Spring MVC、JSP/JSTL、実行可能 WAR |
| データ | MariaDB、Spring Data JPA、Flyway、HikariCP |
| セキュリティ | Spring Security、Argon2、CSRF、CSP、セッション制限、アカウントロック、監査ログ |
| 管理 | ユーザー、ログイン履歴、アクティブセッション、動的メニュー、多言語、パスワード変更 |
| 業務 UI | フルスクリーンシェル、権限別ナビゲーション、マルチタブ、レスポンシブレイアウト |
| 多言語 | 英語が既定、英語・韓国語・簡体字中国語・日本語の完全な UI、実行時の言語・翻訳管理 |
| 運用 | Docker Compose、ヘルスプローブ、分離ログ、SQL・スロークエリ観測 |
| サプライチェーン | サードパーティライセンスレポート、CycloneDX SBOM、依存関係の自動更新 |

## アプリケーション基準

### フルスクリーン業務ワークスペース

- ログイン後はデスクトップ型のフルスクリーンレイアウト
- DB メニューをグループ、ロール、表示順で並べて権限別に表示
- 同一オリジンの iframe タブで複数の JSP 業務画面を同時に保持
- ユーザー別の開いているタブとアクティブタブを `sessionStorage` に保存し、更新後に復元
- メニュー検索、サイドバー折りたたみ、現在タブ更新、既定タブ整理、モバイルナビゲーション
- カードの羅列ではなく、ツールバー、指標ストリップ、データグリッド、編集パネル中心の管理 UI
- 構造と挙動を `app.css`、色・密度・角丸・影を `theme-modern.css` で分離
- 選択タブは色帯、下線、下端のアクセントを使わず、背景・文字色・太さ・アクセシビリティ状態で区別

業務画面の URL はデータベースのメニュー定義から取得します。サーバーが許可した内部パスだけをタブで開き、タブ状態はブラウザセッション内に限定します。

![英語のマルチタブ業務ワークスペース](docs/images/workspace.png)

### データベース管理の多言語

- 英語を既定値とし、ホーム、ログイン、登録、エラー、ワークスペース、すべての管理画面を英語・韓国語・簡体字中国語・日本語で提供
- 言語グリッドの各行で BCP 47 タグ、名称、順序、有効状態を直接編集し、行単位で保存
- メッセージキーを行、登録言語を列にした翻訳マトリクスで複数言語を同時に比較・編集
- セルを空にするとバンドル値へ戻り、MariaDB にコミットした変更は次のリクエストから反映
- トランザクションのコミット後にキャッシュを即時無効化し、再デプロイせず次のリクエストから反映
- 初回アクセスでは DB の有効言語から `Accept-Language` を照合し、不一致なら DB の既定言語を使用
- 選択言語を `HttpOnly`、`SameSite=Lax` Cookie に保存し、本番 HTTPS では `Secure` を有効化
- 安全な内部クエリだけを言語変更後も保持し、外部 URL、プロトコル相対 URL、パストラバーサルを拒否
- 動的翻訳はプレーンテキストに限定し、制御文字と `MessageFormat` プレースホルダーを検証
- キャッシュの件数と TTL を設定で制限し、アプリケーション外からの DB 変更も TTL 後に再読込

実行中に言語を追加するには、**多言語管理**の新規行へ BCP 47 タグを入力し、翻訳マトリクスに追加された列を埋めます。同じメッセージ行で複数言語を編集して一度だけ保存でき、セルでは `Ctrl/⌘ + Enter` でも保存できます。完全にレビューした初期翻訳をアプリケーションと一緒に配布する場合は、`messages_<言語タグ>.properties` も追加してください。自動テストは内蔵 4 言語のキー集合、空値、動的列挙メッセージを検証します。

| English | 한국어 | 简体中文 | 日本語 |
|---|---|---|---|
| ![英語ログイン](docs/images/login-en.png) | ![韓国語ログイン](docs/images/login.png) | ![中国語ログイン](docs/images/login-zh.png) | ![日本語ログイン](docs/images/login-ja.png) |

| インライン言語グリッド | 多言語翻訳マトリクス |
|---|---|
| ![実行時インライン言語グリッド](docs/images/localization.png) | ![DB 多言語翻訳マトリクス](docs/images/translations.png) |

### 基本管理画面

| 画面 | 主な機能 | 必要なロール |
|---|---|---|
| 業務状況 | アカウント、ログイン、セッション指標、最近のセキュリティイベント | ユーザー |
| ユーザーアカウント | アカウント検索、ロール・状態変更、ロック解除 | 管理者 |
| ログイン履歴 | 成否、リクエスト ID、日時、接続元アドレスの確認 | 管理者 |
| セッション管理 | アクティブセッションの確認と選択セッションの失効 | 管理者 |
| メニュー管理 | グループ、順序、内部パス、ロール、表示状態の編集 | 管理者 |
| 多言語管理 | 言語行とメッセージ×言語翻訳マトリクスを直接編集 | 管理者 |
| パスワード変更 | 現在のパスワード確認、ポリシー適用、全セッション失効 | ユーザー |

### 認証とアカウントの不変条件

- ユーザー名の正規化と重複防止、登録機能は既定で無効
- Argon2id 系を既定にした `DelegatingPasswordEncoder`
- 既存 bcrypt、PBKDF2、scrypt ハッシュを読み取り、ログイン成功時に自動更新
- 長さ、制御文字、ユーザー名・メールアドレスの包含を検証するパスワードポリシー
- 現在のパスワード確認、同一パスワード再利用防止、変更後の全セッション失効
- ユーザー名・IP 単位のログインレート制限と DB に保持するアカウントロック
- 存在しないユーザー、誤ったパスワード、ロック済みアカウントに同じ失敗メッセージを返し、列挙を防止
- 高コストなハッシュ処理の前に認証情報の長さとフォーム全体のサイズを制限
- 初期管理者を一度だけ作成
- 自己無効化、自己管理者ロール削除、最後の管理者削除を防止
- ロールまたはアカウント状態変更後に既存セッションを失効

### Web セキュリティ

- Spring Security の CSRF 防御と、状態変更操作の POST 限定
- ログイン後のセッション ID 変更、同時セッション制限、ログアウト時の Cookie・セッション削除
- `HttpOnly`、`Secure`、`SameSite` セッション Cookie
- CSP、Permissions-Policy、Referrer-Policy、HSTS、同一オリジンフレーム、MIME スニッフィング防御
- 認証ページのキャッシュ禁止と、内部例外を隠すエラー応答
- `/WEB-INF/views` 配下の JSP と、動的出力のエスケープまたは入力制限
- リクエスト ID、ログ改行除去、機密入力を記録しないアクセスログ

### データと運用

- バージョン管理された Flyway、Hibernate `validate`、Open Session in View 無効
- HikariCP のタイムアウト、接続寿命、プールサイズを環境変数化
- アプリケーション、セキュリティ監査、SQL ログを分離してローテーション
- SQL バインド値のログは既定で無効
- ローカル環境で MariaDB スロークエリテーブルを有効化
- セキュリティイベントを DB と専用監査ログの両方に保存
- Flyway V2 で動的メニュー、V3 で実行時言語・メッセージカタログを導入
- Actuator health/info は公開し、その他の管理エンドポイントは管理者限定
- Graceful shutdown、liveness/readiness、非 root ユーザー、読み取り専用アプリケーションコンテナ

## クイックスタート

必要なのは Docker だけです。ローカル Java と Maven は任意です。

### 必要条件

- Docker Engine または Docker Desktop と Compose v2
- `make`、または `Makefile` に記載された同等の Docker Compose コマンド
- ローカルポート `8080` と `3306`。必要に応じて `.env` で変更可能

リポジトリのルートでローカル環境ファイルを作成します。

```sh
make init
```

ランダムな DB パスワードと初期管理者パスワードを生成し、`.env` を権限 `600` で保存します。既存ファイルは上書きしません。`.env` は機密情報を含むため、コミットや共有をしないでください。

サービスを開始します。

```sh
make up
```

コンテナが healthy になったら次を確認します。

1. `.env` の `APP_BOOTSTRAP_ADMIN_USERNAME` と `APP_BOOTSTRAP_ADMIN_PASSWORD` で `http://127.0.0.1:8080/login` にログインします。
2. 英語、韓国語、中国語、日本語を切り替え、更新後も選択が維持されることを確認します。
3. 一時パスワードをすぐに変更します。変更すると既存セッションはすべて終了します。
4. 次の起動前に `APP_BOOTSTRAP_ADMIN_ENABLED=false` にします。
5. スモークテストを実行します。

```sh
make smoke
```

既定では `127.0.0.1` のみにバインドします。ポートが競合する場合は `.env` の `APP_PORT` または `DB_PORT` だけを変更してください。

```sh
make logs         # アプリと DB のログを追跡
make test         # 全自動テストを実行
make package      # テストして実行可能 WAR を生成
make public-check # 公開リポジトリ向け安全検査
make down         # DB ボリュームを保持して停止
```

ローカルに Java 21 があれば、`./mvnw test` と `./mvnw clean package` も利用できます。WAR は `target/secure-mvc-starter.war` に生成されます。

### ヘルスチェック

| URL | 期待する結果 |
|---|---|
| `http://127.0.0.1:8080/login` | ログイン画面 |
| `http://127.0.0.1:8080/internal/actuator/health` | アプリケーションの状態 |
| `http://127.0.0.1:8080/internal/actuator/health/liveness` | プロセスの生存状態 |
| `http://127.0.0.1:8080/internal/actuator/health/readiness` | リクエスト受付準備 |

`make logs` でアプリケーションと DB の出力を確認できます。`make down` は DB ボリュームを保持します。ローカルデータを消す場合だけ明示的にボリュームを削除してください。

## クローン後のカスタマイズ

1. `pom.xml` の `groupId`、`artifactId`、`name`、`finalName` を変更します。
2. `com.example.webstarter` を組織の逆ドメインパッケージに変更します。
3. `messages*.properties` の製品文言と既定メニュー翻訳を変更し、`theme-modern.css` のセマンティックトークンをブランドに合わせます。
4. 既存の Flyway `V1` から `V3` は変更せず、業務テーブルは新しいマイグレーションで追加します。
5. `USER` と `ADMIN` を超える権限が必要なら、機能単位の権限を導入し、サービスの `@PreAuthorize` も更新します。
6. デプロイ前に[セキュリティチェックリスト](docs/SECURITY-CHECKLIST.md)と[運用ガイド](docs/OPERATIONS.md)を確認します。

適用済み Flyway ファイルを変更するとチェックサムが変わります。運用 DB では過去のファイルを書き換えず、後続マイグレーションで互換性を保ってください。

## プロジェクト構造

```text
src/main/java/com/example/webstarter
├── config       環境設定と Spring Security チェーン
├── security     認証、パスワード、レート制限、セッション失効
├── user         ユーザードメインとアカウントサービス
├── admin        管理機能と不変条件
├── audit        DB・ファイルのセキュリティイベント
├── bootstrap    一度だけ実行する管理者作成
├── navigation   DB メニュー、安全なパス、ロールフィルター
├── localization 実行時言語カタログ、翻訳上書き、検証、キャッシュ
└── web          MVC、言語切替、リクエスト ID、ヘッダー、アクセスログ
```

リクエストフロー、データモデル、拡張境界は[アーキテクチャ文書](docs/ARCHITECTURE.md)を参照してください。

## 設定方針

環境固有値はアプリケーションコードに固定しません。`application.yml` の運用設定は環境変数で上書きできます。

| 領域 | 代表的な変数 | 基準 |
|---|---|---|
| DB | `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`DB_POOL_MAX_SIZE` | シークレット外部注入、制限付きプール |
| セッション | `SESSION_COOKIE_SECURE`、`SESSION_COOKIE_SAME_SITE`、`SESSION_TIMEOUT` | 本番 HTTPS、30 分、Lax |
| 多言語 | `APP_I18N_DEFAULT_LOCALE`、`APP_I18N_SUPPORTED_LOCALES`、`APP_I18N_COOKIE_*`、`APP_I18N_CACHE_*` | DB カタログ、英語フォールバック、安全な Cookie、キャッシュ上限 |
| パスワード | `APP_PASSWORD_ALGORITHM`、`APP_ARGON2_*` | Argon2、起動時検証 |
| ログイン保護 | `APP_LOGIN_*`、`APP_ACCOUNT_LOCK_*` | IP・識別子制限とアカウントロック |
| ヘッダー | `APP_CONTENT_SECURITY_POLICY`、`APP_FRAME_OPTIONS`、`APP_HSTS_*` | 外部フレーム拒否、同一オリジンタブ許可 |
| ログ | `APP_SQL_LOG_LEVEL`、`APP_SQL_BIND_LOG_LEVEL`、`APP_LOG_*` | 本番 SQL 無効、バインド値無効 |
| 管理 | `MANAGEMENT_ENDPOINTS`、`APP_ADMIN_PAGE_SIZE` | 最小エンドポイント、ページ上限 |
| HTTP 入力 | `SERVER_MAX_FORM_POST_SIZE`、`SERVER_MAX_PARAMETER_COUNT` | 過大リクエストの早期拒否 |
| ワークスペース | `APP_WORKSPACE_MAX_TABS`、`APP_WORKSPACE_DEFAULT_MENU_KEY` | タブ上限と既定画面 |

全設定とデプロイ例は[運用ガイド](docs/OPERATIONS.md)にあります。

## SQL とスロークエリの確認

ローカルプロファイルは Hibernate SQL を `/app/logs/sql.log` に書き込みます。バインド値にはパスワード、トークン、個人情報が含まれる可能性があるため、`APP_SQL_BIND_LOG_LEVEL=OFF` が既定であり、本番でも推奨です。

```sh
docker compose --env-file .env exec app sh -c 'tail -f /app/logs/sql.log'
docker compose --env-file .env exec db mariadb -uroot -p
```

DB コンソールでスロークエリを確認します。

```sql
SELECT start_time, query_time, rows_examined, sql_text
FROM mysql.slow_log
ORDER BY start_time DESC
LIMIT 50;
```

本番ではスローログの保持期間を設定してください。高トラフィック環境ではファイル出力または集中型の観測基盤を使用します。

## 意図的な境界

このリポジトリは安全な出発点であり、完成した ID 製品ではありません。メール所有確認、パスワード再設定メール、MFA・パスキー、SSO/OIDC、CAPTCHA、個人データ保持ポリシーは業務固有の基盤が必要なため、仮の動作では実装していません。

インターネット向け登録を有効にする前に、少なくともメール確認とアカウント復旧を追加してください。複数インスタンスではメモリ内ログイン制限と `SessionRegistry` を共有レートリミッターと Spring Session に置き換え、DB マイグレーション用と実行時用のアカウントを分離します。

## ライセンスとサプライチェーン

プロジェクトのソースは [Apache License 2.0](LICENSE) で公開しています。サードパーティライセンスと再配布上の注意は [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) に記載しています。

`clean package` は直接・推移依存関係のライセンスを `target/generated-sources/license/THIRD-PARTY.txt` に生成します。ライセンス情報がない依存関係はビルドを失敗させ、CycloneDX SBOM は `META-INF/sbom` に含まれます。

```sh
make license-report
make package
```

## 公開リポジトリの安全対策

[AGENTS.md](AGENTS.md) はベンダー中立の表現、ワークステーション情報とシークレットの排除、スクリーンショット確認、依存ライセンス確認をリポジトリルールとして定めています。クローン後に Git hooks を有効にします。

```sh
git config core.hooksPath .githooks
./scripts/public-release-check.sh repository
```

スキャナーは環境ファイル、鍵、証明書、ログ、ビルド成果物、過大ファイル、ユーザーの絶対パス、信頼度の高いシークレットパターンを拒否します。個人的な拒否語は追跡されない `.git/info/public-release-deny-patterns` にだけ保存します。

コントリビューション手順は [CONTRIBUTING.md](CONTRIBUTING.md) を参照してください。

## 参照標準

- [Spring Boot Servlet/JSP ドキュメント](https://docs.spring.io/spring-boot/4.0/reference/web/servlet.html)
- [Spring Boot traditional WAR デプロイ](https://docs.spring.io/spring-boot/how-to/deployment/traditional-deployment.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html)
- [Spring Security セキュリティヘッダー](https://docs.spring.io/spring-security/reference/features/exploits/headers.html)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [MariaDB Connector/J](https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j)
