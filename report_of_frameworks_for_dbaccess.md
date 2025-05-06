# Quarkus Native 模式下 PostgreSQL 資料存取框架比較

- [Chatgpt](https://chatgpt.com/share/6819d2c0-1138-8010-bce2-22dad97730de)

背景與需求
在 Quarkus 應用中使用 PostgreSQL 資料庫時，有多種 Java 資料存取框架可供選擇。本報告針對 Quarkus Native (原生編譯) 模式 下整合 PostgreSQL 的幾個主要資料存取方案進行比較，包括 jOOQ、Hibernate/JPA、JDBC、MyBatis，以及其他常被推薦的方案（如 Quarkus Panache、Vert.x Reactive SQL Client、JDBI 等）。假設使用者對啟動速度或執行效能沒有特別要求，也不偏好宣告式或命令式的資料操作方式，而主要查詢風格類似 MyBatis：可以直接在介面上書寫 SQL，支援基本到中等複雜度的 SQL 操作。 由於 Quarkus Native 模式下使用 GraalVM 進行原生映像編譯，必須考量框架在 反射、動態代理 等方面的相容性，否則可能造成編譯錯誤。我們將從以下面向分析每個框架的可行性與穩定性：
原生模式適配：是否適合 Native 編譯（有無因反射、代理導致的問題，是否需要額外設定）。
社群解法與建議：開發者社群常用的解決方案（如 GraalVM 的 hint 或 @RegisterForReflection 註冊）。
官方支援：有無 Quarkus 官方或社群提供的擴充套件（extension）支援。
開發體驗與學習曲線：使用該框架進行開發的便利性，SQL 撰寫方式，以及新手上手難易度。
整體推薦程度：綜合上述因素，在 Quarkus 原生環境下的建議使用程度。
框架評比
jOOQ
原生模式適配： jOOQ 是一套強型別的 SQL DSL 框架，直接讓開發者使用 Java 程式碼構建 SQL 查詢。由於 jOOQ 內部使用反射（例如其 DefaultRecordMapper 會透過反射讀取 JPA 標註），在 Native 編譯時可能遇到困難
github.com
。實務上已有開發者成功在 Quarkus 中使用 jOOQ 並部署 Native 執行檔超過半年，但為了避開 GraalVM 的限制，需要對 jOOQ 產生的所有記錄類別進行反射註冊設定
github.com
stackoverflow.com
。例如，有人以 Gradle 腳本自動掃描並註冊 jOOQ 所有生成的 POJO、Record 類別及 routines，讓 GraalVM 知道要保留這些類別的反射資訊
stackoverflow.com
。此外，早期 jOOQ 核心還依賴部分 JPA API（如 javax.persistence.Column），因此即使不使用 Hibernate 也需要額外引入 JPA 的介面套件以通過編譯
github.com
。最新版本的 jOOQ 已在逐步改善這種依賴，但使用時仍需留意。 社群建議： 社群建議兩種方式來解決 jOOQ 在 Native 下的反射問題：其一是使用 Quarkus 提供的 @RegisterForReflection 註解將相關類別標記；其二是提供一份 reflection-config.json 讓 GraalVM 在建構映像時包含指定類別
github.com
。針對 Quarkus，已有社群開發的 jOOQ 擴充套件（Quarkiverse quarkus-jooq）可協助這些設定。早期曾有開發者反映該擴充版本較舊且對 jOOQ 專業版相容性不佳
stackoverflow.com
；不過截至 2025 年，該擴充已更新至 2.x 版（支援 Quarkus 3.x）並持續維護
quarkus.io
。總體而言，使用最新版 quarkus-jooq 擴充可以簡化 jOOQ 的 Native 相容性設定。 官方支援： jOOQ 並非 Quarkus 官方內建支援的ORM，但 Quarkus 官方擴充列表中已收錄社群提供的 Quarkus jOOQ 擴充套件（最新版 2.1.0，Java 17）
quarkus.io
。該擴充能讓開發者在 Quarkus 中使用 jOOQ 進行型別安全的 SQL 查詢，同時確保在 Native 編譯時適當保留所需的類別資訊。 開發體驗與學習曲線： 使用 jOOQ 的體驗偏向書寫程式化的 SQL。開發者可以利用 jOOQ 的 DSL API 以類似寫原生 SQL 的風格構建查詢，同時享有編譯期型別檢查的好處。這對於喜歡完全掌控 SQL 的開發者非常有吸引力，尤其當查詢相對複雜時，jOOQ 能更直觀地表達 SQL 邏輯
medium.com
。「當查詢變得複雜或龐大時，jOOQ 往往表現更佳，而 Hibernate 可能因抽象層與自動抓取（auto-fetching）造成效能損失
medium.com
。」不過，jOOQ 的使用需要先行對資料庫Schema進行代碼產生（code generation）設定，對新手而言學習其 DSL 語法和設定流程需要一些時間，學習曲線屬於中等偏高。相較傳統 ORM，jOOQ 提供較少的自動化（如關聯映射、快取），但換來對 SQL 最大的掌控度。 整體推薦程度： 在 Quarkus Native 環境下，jOOQ 是可行且強大的選擇，但需要較多前期設定與經驗。如果團隊擅長 SQL 且查詢邏輯複雜，jOOQ 能提供型別安全與高彈性的查詢能力。然而，由於需要處理反射相容性與學習DSL，對於簡單應用或不熟悉 jOOQ 的團隊而言，初始成本較高。在有擴充套件支援的情況下，其穩定性已有許多生產案例驗證
stackoverflow.com
。綜合而言，若追求高度客製化的 SQL 操作且願意投入學習，jOOQ 在 Quarkus Native 下是值得考慮的方案。
Hibernate/JPA
原生模式適配： Hibernate ORM（JPA 實作）是 Quarkus 官方重點支援的關聯式資料庫解決方案。Quarkus 將 Hibernate 與 JPA 緊密整合，提供了預先配置的擴充套件和編譯期優化，使其能夠在原生映像中良好運作
quarkus.io
。實際上，Quarkus 將大量 Hibernate 的初始化工作移至建置階段，包含對 JPA 實體類別的編譯期增強（Bytecode Enhancement），避免在執行時使用動態代理或反射產生代理類別
quarkus.io
。例如，Quarkus 在編譯時處理了 lazy loading 等 Hibernate 特性所需的攔截器或代理，確保這些運作機制在 Native 環境中不會出錯。結果是開發者幾乎不需為相容性煩惱 —— Hibernate/JPA 在 Quarkus Native 下可開箱即用，一般不會因反射問題導致編譯錯誤。 社群建議： 由於 Quarkus 官方已經為 Hibernate 提供完善支援，社群經驗中較少需要開發者自行處理額外設定。典型建議是直接使用 Quarkus 提供的 Hibernate ORM 擴充（io.quarkus:quarkus-hibernate-orm）以及對應的 JDBC Driver 擴充（例如 PostgreSQL 用 io.quarkus:quarkus-jdbc-postgresql）
quarkus.io
quarkus.io
。這些擴充會自動包含 GraalVM Native 所需的配置。開發者僅需按照 Quarkus 指引標註 @Entity 等 JPA 註解、在應用設定中定義資料庫連線資訊，Quarkus 便會在建置時自動進行必要的設定與優化
quarkus.io
。社群也建議避免在 Native 模式下使用少數 Hibernate 不支援的特性（例如 JMX 的統計功能在 Native 模式被停用
quarkus.io
），但大部分正常的 JPA 操作在 Quarkus 都有替代方案或已被處理。 官方支援： Hibernate ORM 是 Quarkus 官方一級公民般的存在。Quarkus 提供完整的官方擴充，包含傳統同步版的 Hibernate ORM，以及對應的 Hibernate Reactive（若需要非阻塞式IO）。對於標準 Hibernate ORM，Quarkus 官方文件提到：「Hibernate ORM 是 Jakarta Persistence 的標準實作...在 Quarkus 上運行表現非常優異
quarkus.io
。」此外，Quarkus 還提供了周邊工具（如 Panache，下節詳述）進一步簡化 Hibernate 的使用。因此在官方支援度上，Hibernate/JPA 是最完善的。 開發體驗與學習曲線： 使用 Hibernate/JPA 的體驗屬於傳統 ORM 模式：先定義實體類別對應資料表，透過標註配置關聯和欄位，再使用 Entity Manager 或高階 API 進行資料操作。對於習慣物件導向資料存取的開發者，這種模式能減少手寫 SQL 的頻率，常見操作只需呼叫方法或撰寫 JPQL 查詢即可。然而，若開發者傾向直接書寫 SQL（如 MyBatis 風格），JPA 也提供原生查詢（Native Query）功能，可在 @Query 或 entityManager.createNativeQuery() 中直接嵌入 SQL，但整合度不若專門的 SQL Mapper 靈活。Hibernate 的學習曲線視開發者背景而定：有經驗者會覺得駕輕就熟，初學者則需要理解 ORM 概念（級聯、延遲載入、二級快取等），學習成本中等。值得慶幸的是，Quarkus 提供許多自動配置，例如毋需傳統的 persistence.xml 就能運行 JPA
quarkus.io
。整體開發體驗在 Quarkus 中十分順暢。 整體推薦程度： Hibernate/JPA 幾乎是 Quarkus 原生模式下最穩定保險的選擇之一。有了官方的深度優化與擴充支援，它在 Native 編譯環境中表現最為穩定，開發者無需為相容性操心。如果團隊希望減少直接寫 SQL，並利用 ORM 提供的物件映射便利性，那麼 Hibernate 在 Quarkus 上是首選。即便需要撰寫部分原生 SQL，Hibernate 也能兼顧。而在沒有特殊偏好的前提下，本報告傾向推薦使用 Quarkus 官方支援的 Hibernate/JPA 作為資料層方案，其成熟度和社群經驗最豐富。
Quarkus Panache（Hibernate ORM Panache）
原生模式適配： Panache 是 Quarkus 為 Hibernate ORM 提供的一套語法糖與模型簡化工具。由於 Panache 本質上擴充自 Hibernate/JPA，其底層還是透過 Quarkus 的 Hibernate ORM 擴充運行，因此在 Native 模式下的相容性與 Hibernate 相同：完全由官方支援，無須額外處理反射問題。Panache 所做的額外工作（例如在編譯期產生主動式 Active Record 模式的方便方法）也都是靜態進行的，對 GraalVM 編譯不會有負面影響
quarkus.io
。換言之，使用 Panache 不會降低 Quarkus 在原生編譯下的穩定性。 社群建議： 社群普遍建議，如果選擇 Hibernate/JPA，又希望減少樣板程式碼、提高開發效率，可以使用 Panache。只需引入 quarkus-hibernate-orm-panache 擴充，即可使用 Panache 提供的各種便捷功能。常見建議包括使用 Active Record 模式（讓實體類別繼承 PanacheEntity 或 PanacheEntityBase，直接在實體類上操作資料庫），或者 Repository 模式（建立 Repository 類別實作 PanacheRepository 介面）
quarkus.io
。Panache 本身已內建許多常用的查詢方法（如 find()、list()、delete() 等），因此社群也建議優先使用這些API實現簡單查詢；對於複雜查詢，可以結合 Panache 提供的 @Query（對應 JPQL）或直接使用原生 SQL Query。此外，需要注意的是 Panache 採用一些慣例（Convention）來減少配置，例如實體主鍵名稱預設為 id 等，若有特殊需求可能需要調整，但大多數情況下這些慣例令開發更為簡潔。 官方支援： Hibernate ORM with Panache 是 Quarkus 官方主推的特性之一，在文件中著墨甚多。官方表示：「Hibernate ORM with Panache 專注於讓您在 Quarkus 中編寫實體變得簡潔有趣
quarkus.io
。」Panache 由 Quarkus 團隊維護，與 Quarkus 主版本同步更新，可靠度高。由於 Panache 是官方擴充的一部分，其疑難問題能夠直接從官方Issue追蹤，且有廣大的使用者群提供經驗分享。總之，Panache 屬於官方一等公民級支援，在 Quarkus 生態中非常成熟。 開發體驗與學習曲線： Panache 的宗旨是簡化 Hibernate 的使用。開發體驗上，使用 Panache 之後，實體類別可以省略大部分樣板程式：不再需要手動撰寫 DAO 或 Repository 的基本 CRUD，Panache 提供了 Active Record 風格的靜態方法或Repository介面的預設實作。例如，官方文件中的 Person 實體，繼承 PanacheEntity 後，就能直接呼叫 Person.findByName(name) 等方法完成查詢
quarkus.io
。這使代碼更精簡可讀，將簡單常見的資料存取操作變得幾乎一行就能完成
quarkus.io
。對開發者而言，Panache 的學習曲線相當低淺 —— 基本上熟悉 JPA 後，只需學習Panache所提供的那些約定俗成的方法命名和用法即可。甚至對從未使用過 Hibernate 的新手來說，Panache 的 Active Record 模式直觀易懂（直接對物件調用CRUD），學習成本低。需要留意的是，Panache 的Active Record模式將資料存取邏輯放在實體類本身，這種模式在領域驅動設計或嚴格分層架構下可能未必符合習慣。如果團隊偏好傳統的分層架構，可以改用Panache的Repository模式——學習成本仍舊不高，只是改為撰寫簡短的 Repository 類別而非繼承實體。 整體推薦程度： 若已選用 Hibernate/JPA，強烈建議同步採用 Panache 來提升開發效率。在 Quarkus Native 下，Panache 與 Hibernate 同樣穩定，但能大幅簡化程式碼、降低心智負擔。對沒有特定框架偏好的團隊而言，Hibernate 搭配 Panache 通常是最優路線：兼顧穩定性、功能豐富度和開發體驗。如果開發者需要撰寫部分自訂 SQL，Panache 也允許直接編寫 JPQL 或原生 SQL 查詢，因此靈活性並不受太大限制。唯一可能的顧慮在於設計哲學上的取捨，但從生產應用角度，Panache 所帶來的便利通常勝過其局限。因此綜合而言，在 Quarkus 平台上 Panache 是非常推薦的一種資料存取方案。
原生 JDBC（純 JDBC 使用）
原生模式適配： 直接使用 JDBC 存取資料庫在 Quarkus Native 模式下是完全可行且相對簡單的。因為 JDBC 本身主要透過資料庫驅動進行操作，不存在高階框架的代理或大量反射。Quarkus 提供對各種 JDBC Driver 的官方擴充，例如 PostgreSQL 對應的 quarkus-jdbc-postgresql，會自動引入 PostgreSQL 驅動程式並設定好連線池（Quarkus 使用 Agroal 作為連線池管理）
quarkus.io
。只要引入對應擴充，GraalVM 會在原生映像中包含所需的驅動類別。PostgreSQL JDBC 驅動本身在 GraalVM Native 下運作良好；Quarkus 預先替它做了一些必要的配置，確保例如 Driver 類別能正確被識別和初始化。因此，以純 JDBC 進行資料庫操作幾乎不會遇到 Native 編譯相容性的問題。 社群建議： 使用 JDBC 時，社群主要建議注意兩點：其一，務必使用 Quarkus 的資料庫連線擴充（如前述的 quarkus-jdbc-postgresql），避免手動引入第三方 JDBC Driver jar 造成類別沒被納入 Native 映像；其二，充分利用 Quarkus 提供的 CDI 功能來管理 DataSource，例如可以直接使用 javax.inject.Inject 注入 javax.sql.DataSource 或使用 @DataSource 來取得命名的 DataSource。這些 DataSource 由 Quarkus/Agroal 管理，能自動在應用啟動時初始化。至於反射設定方面，JDBC 操作通常不需要手動 register 類別。只有在使用非常罕見的 JDBC 特性時（例如自定義 Driver 實作）才可能需要 GraalVM 額外設定，但對一般的 PostgreSQL JDBC 而言，社群經驗是開箱即用即可運作。總體而言，社群普遍認為直接使用 JDBC 是 Native 模式下的穩妥路線，尤其當不想引入額外框架時。 官方支援： Quarkus 對 JDBC 的支援體現在官方擴充和配置上。每種主流資料庫都有官方的 JDBC 擴充套件，確保相容性。這意味著在 Quarkus 官方眼中，直接使用 JDBC 是被完全支援並被視為正常用例的。另外，Quarkus 文件中也多次演示了不透過 ORM，直接使用 JDBC 連線查詢資料的範例。雖然Quarkus更鼓勵使用ORM或Reactive等現代方式，但是對 JDBC 的支援屬於基礎且穩固的部分。 開發體驗與學習曲線： 選擇純 JDBC 代表完全由開發者掌控 SQL 和資料映射。在開發體驗上，這意味著需要撰寫 PreparedStatement、ResultSet 處理等樣板程式碼。對於只想專注在 SQL 本身的人來說，JDBC 提供了極大自由度，但也伴隨較繁瑣的資源管理（需要確保關閉連線、處理例外等）。Quarkus 幫助減輕了一部分負擔，例如可以使用 try-with-resources 確保連線釋放，或透過 CDI 注入減少樣板碼，但跟高階框架相比，JDBC 的程式碼量通常較多。學習曲線方面，JDBC 是最低的：只要懂基本的 SQL 和 JDBC API，就沒有額外框架知識需要學習。因此對初學者或小型專案，JDBC 其實很直觀。但是，隨著專案變大，直接用 JDBC 可能會出現重複的碼和潛在錯誤風險，維護成本上升。 整體推薦程度： 在 Quarkus Native 下使用 JDBC 十分穩定，但開發生產力取決於團隊對 SQL 與資料庫操作的掌握。如果專案規模不大、查詢簡單或者團隊中有人樂於封裝 JDBC 操作，那麼這是最直接了當的方案，無引入框架的額外複雜度。其優點是確定性高——所有SQL都由開發者明確撰寫，性能表現可預期。缺點是樣板代碼多，且缺乏對物件的自動映射，需要手工將 ResultSet 資料轉成物件。綜合來看，除非團隊明確決定「不用 ORM/框架」，否則在 Quarkus 平台上還是可以考慮更高階的方案。然而，純 JDBC 仍是一個安全的後盾：當其他框架遇到無法解決的 Native 相容問題時，JDBC 幾乎永遠可以作為備援方案。
MyBatis
原生模式適配： MyBatis 是一個廣受歡迎的 SQL Mapper 框架，允許在 Java 介面中編寫 SQL 查詢（可註解或 XML 定義），由框架自動將結果映射為物件。傳統上，MyBatis 透過反射和動態代理實現接口的具體執行，在 GraalVM Native 編譯時，如果沒有適當配置可能導致問題。然而，Quarkus 社群已提供 MyBatis 的擴充套件來解決這些相容性挑戰。該擴充會在建置時分析 MyBatis 的 Mapper 介面並註冊必要的反射資訊，使得介面代理和結果物件在 Native 模式下可順利生成。根據 Quarkus 擴充資料，quarkus-mybatis 擴充自 2021 年開始發展，目前版本已相當成熟，在 2025 年發布的 2.4.0 版被標示為 Stable
quarkus.io
quarkus.io
。因此，使用 Quarkus MyBatis 擴充後，MyBatis 在原生編譯環境下運行良好。實際案例中，早期曾有人嘗試直接使用 MyBatis (搭配 mybatis-cdi) 導致 GraalVM 編譯錯誤
stackoverflow.com
；而透過官方建議改用 Quarkus 等價的擴充方案後，此類問題迎刃而解
stackoverflow.com
。 社群建議： 社群對在 Quarkus 中使用 MyBatis 的建議是務必使用 Quarkiverse 提供的擴充套件而非直接引入 MyBatis 原生套件。透過 quarkus-mybatis，大部分 MyBatis 功能（CRUD 映射、動態 SQL 等）都能開箱即用，同時避免手動配置反射。若遇到特殊情境需要額外設定（例如使用非常動態的 SQL 特性），可以透過在 application.properties 中增加 Quarkus 提供的 MyBatis 設定屬性或加入額外的 @RegisterForReflection 來解決。不過，根據擴充開發者的說明，目前 MyBatis 大部分功能在 Native 模式下都已有相應支援，只是尚未經過和 Hibernate 一樣大規模的長時間考驗，因此社群建議進行充分的測試。整體而言，經社群驗證，MyBatis 在 Quarkus Native 下穩定度不輸常規 JVM 模式，常見問題都有解方。 官方支援： MyBatis 屬於 Quarkus Quarkiverse 社群支援的範疇，而非 Quarkus 核心官方擴充。但這個社群擴充已被列在 Quarkus 官方網站的擴充列表中並提供使用指南
quarkus.io
。最新版本顯示其 狀態為 Stable，意味著功能和 API 已定型且適用於生產環境
quarkus.io
。雖然不如 Hibernate/Panache 那樣由官方團隊直接維護，該擴充的主要貢獻者也與 Quarkus 社群有密切互動（GitHub 上有定期的問題修復）。因此從支援角度看，Quarkus 對 MyBatis 的支援屬於間接官方：有正式渠道提供，但由社群主導維護。 開發體驗與學習曲線： MyBatis 的開發體驗對熟悉 SQL 的開發者非常友好。查詢撰寫風格與 SQL 幾乎相同：開發者可以在 Mapper 介面上以 @Select、@Insert 等註解直接寫 SQL，或使用 XML 檔案編寫更複雜的查詢和動態 SQL。相比 ORM，MyBatis 不會隱藏 SQL，因此開發時能確切知道執行的語句，這一點與使用 JDBC 相似，但 MyBatis 省去了大量重複的樣板碼（如結果集映射、自動把資料填充到 POJO）。學習曲線方面，如果開發者本來就了解 MyBatis，在 Quarkus 中幾乎不需要新的知識；如果沒用過 MyBatis，但熟悉 SQL，學習成本也不高：只需掌握 MyBatis 的Mapper介面定義方式和少量註解配置。總體學習曲線屬於低到中等。需要注意的是，MyBatis 並不像 Hibernate 提供完整的關聯管理和快取，因此開發者需要手動關注部分資料庫交互細節，但這也是很多人選擇 MyBatis 的原因 —— 它簡單直接、沒有太多黑箱操作。 整體推薦程度： **若團隊偏好以 SQL 為中心的資料存取方式，MyBatis 在 Quarkus Native 下是相當值得推薦的方案。**它結合了直接編寫 SQL 的掌控力與框架提供的便利性（自動映射等），在沒有嚴苛效能要求的前提下表現穩定。和 jOOQ 相比，MyBatis 更接近編寫原生 SQL 字串的形式，對於從傳統 JDBC 或 Spring JDBC Template 過渡而來的工程師較為親切。與 Hibernate 相比，MyBatis 不做過多抽象，適合想完全掌控 SQL 的情境。綜合評估，在 Quarkus 環境中 MyBatis 已有完善的擴充支援和不錯的社群基礎，如果應用需要中等複雜度的 SQL 且開發者希望直接書寫這些 SQL，那麼 MyBatis 非常適合。唯一需要權衡的是，與 ORM 相比少了一些高階功能（例如跨物件關聯自動處理），但在給定需求下這可能並非問題。
其他推薦方案：Vert.x Reactive SQL Client, JDBI 等
除了上述框架外，在 Quarkus Native 模式下還有一些值得注意的資料庫存取方案，可能更適合特定需求：
Vert.x Reactive SQL Client（含 Mutiny）：這是 Quarkus 提供的非阻塞式資料庫用戶端，底層基於 Eclipse Vert.x。對於希望以Reactive (反應式)方式操作資料庫的應用，Vert.x SQL Client是首選。Quarkus 有官方的 Reactive PostgreSQL Client 擴充（如 io.quarkus:quarkus-reactive-pg-client），讓開發者使用 Mutiny API 進行非同步資料庫操作。其特色是在單一執行緒上可處理大量連線與請求，適合高併發場景
vertx.io
。Vert.x 客戶端的 API 相當直覺，以SQL字串配合參數執行查詢，並返回 reactive 型別（如 Uni<List<Row>>）。由於沒有 ORM 的抽象層，在 Native 下完全相容，基本不涉及反射問題。開發體驗方面，如果熟悉 reactive 流程，使用感受和 JDBC 類似（只是以 reactive 方式接收結果）；學習曲線取決於對 Reactive 編程的掌握，若先前沒有相關經驗，理解 Mutiny 流程需要一定時間，但 Quarkus 有完善文件與範例支援。整體而言，如果應用不介意採用 Reactive 模式，Vert.x SQL Client 能提供極高的效能與穩定性，且SQL書寫自由度高。在無特別同步阻塞需求下，它是 Quarkus 極力推廣的一種現代化方案
vertx.io
。
JDBI：JDBI 是一個介於 JDBC 與 ORM 之間的輕量級框架，它提供了方便的DAO層實作，允許透過在介面上宣告 SQL（類似 MyBatis 的作法）以及簡單的物件映射。Quarkus 有對應的 JDBI 擴充（quarkus-jdbi）
quarkus.io
。由於 JDBI 大量使用反射進行資料映射與介面實現，該擴充專門為 Native 模式做了調整，「讓在原生執行檔中使用 JDBI 成為可能」
quarkus.io
。實際上，擴充在建置階段會為 JDBI 的各種反射操作註冊類別，避免 GraalVM 擋下它們
docs.quarkiverse.io
。使用 JDBI 的好處是開發體驗接近手寫 SQL，但更簡潔：你可以在介面上用註解標註 SQL 查詢並直接得到映射後的物件結果，不需要手動迭代 ResultSet。學習曲線也不高，只需學習 JDBI 的幾個核心註解和API。相較 MyBatis，JDBI 更輕量但功能稍弱（例如沒有完善的XML動態SQL支持），適合查詢不算太複雜的場景。綜合來說，在 Quarkus Native 下有了擴充支援，JDBI 是另一個穩定且易用的SQL Mapper選擇，尤其對於想減少依賴大型框架又希望簡化 JDBC 操作的團隊非常有吸引力。
Hibernate Reactive：如果應用需要在 ORM 層面使用 Reactive 編程，Quarkus 提供 Hibernate Reactive (以及 Panache Reactive)。這讓開發者以非阻塞方式使用類似JPA的API。Hibernate Reactive 目前也有原生模式支援，不過相對於 Vert.x 原生客戶端，其複雜度較高，且因為Reactive模式尚新，社群經驗稍少。在沒有明確Reactive需求時，可以不特別考慮這個方案。但值得知悉其存在，供將來擴展時使用。
其他：在 Java 生態中還有諸如 Spring Data JPA、Micronaut Data 等資料存取技術。在 Quarkus 環境下，Spring Data JPA 可以透過 Quarkus 的 Spring API 相容層來使用，但這主要針對從 Spring 遷移的情況，非必要不建議在 Quarkus 原生專案中採用，因為直接使用 Quarkus 原生支援的框架會更精簡。Micronaut Data 則類似輕量的JPA實作，但Quarkus並無直接支援，需額外整合成本，本文不詳述。
總的來說，除了前面詳細討論的幾個主要方案，Panache、Vert.x Reactive SQL Client、JDBI 等在各自側重的領域都表現出色。如果應用場景與它們的優勢契合，完全可以考慮採用。例如：要求代碼最簡潔可選 Panache，要求極致非阻塞性能可選 Vert.x Reactive，要求介面SQL靈活性可選 MyBatis 或 JDBI。下一節我們將整理一張表格，對比各框架在幾個關鍵面向的表現，以供快速參考。
框架對照表
下表彙總了各框架在 Quarkus Native 模式下的重要特性比較：
框架	原生編譯相容性	Quarkus 擴充支援	資料操作風格	開發體驗與學習曲線	推薦程度
Hibernate/JPA	完全相容（官方優化，無需額外配置）	官方核心擴充（Hibernate ORM）⭐	ORM/宣告式（JPA、JPQL）	傳統且成熟；學習成本中等（熟悉ORM需時間）	★★★★☆ 非常推薦（穩定首選）
Panache (Hibernate)	完全相容（編譯期增強確保原生運行）	官方擴充（Panache）⭐	ORM強化風格（Active Record/Repository）	體驗極佳；學習成本低（API簡潔直觀）	★★★★★ 極力推薦（Hibernate用戶應用）
MyBatis	相容良好（需擴充支援避免反射問題）	Quarkiverse社群擴充✅	SQL為主（XML或註解映射）	靠近純SQL；學習成本低（熟悉SQL即可）	★★★★☆ 推薦（偏好手寫SQL者）
jOOQ	可相容（需手動註冊反射或擴充配置）	Quarkiverse社群擴充✅	SQL為主（類型安全DSL）	強大靈活；學習成本偏高（需熟悉DSL與配置）	★★★☆☆ 推薦（進階需求，願投入學習）
JDBI	相容良好（擴充處理反射）	Quarkiverse社群擴充✅	SQL為主（介面註解映射）	輕量方便；學習成本低（上手容易）	★★★★☆ 推薦（簡化JDBC，輕量應用）
原生 JDBC	完全相容（無框架開銷）	官方擴充（JDBC Driver）⭐	SQL為主（手寫JDBC程式碼）	靈活但繁瑣；學習成本極低（基礎JDBC知識）	★★★☆☆ 一般（除非特別需求，否則可用更佳方案）
Vert.x Reactive 客戶端	完全相容（非阻塞，無需反射）	官方擴充（Reactive PgClient）⭐	SQL為主（Reactive API）	高效現代；學習成本中等（需理解Reactive）	★★★★☆ 推薦（高並發與Reactive場合）
說明：五星「★★★★★」表示極為推薦，三星「★★★☆☆」表示中等推薦程度。官方擴充以⭐標示，社群提供的Quarkiverse擴充以✅標示。
結論與建議
綜合以上分析，在 Quarkus Native 模式下整合 PostgreSQL 資料庫時，各框架各有適用情境：
如果追求成熟穩定並減少相容性風險，Hibernate/JPA 是首選方案，建議搭配 Quarkus Panache 使用以提升開發效率。這組合由官方深度支援，幾乎不用擔心 Native 模式下的問題，適合大部分一般應用。特別是當團隊希望淡化 SQL 細節、專注於業務邏輯時，ORM 帶來的便利和Panache的精簡讓開發更快樂
quarkus.io
。
如果團隊偏好直接書寫 SQL並完全掌控查詢，則可在以下方案中選擇：
MyBatis：提供類似傳統 SQL Mapper 的體驗，使用擴充後在 Native 模式下表現良好。適合需要撰寫大量自訂查詢，又希望減少樣板代碼的人員，學習成本低且社群經驗豐富。
jOOQ：適合對 SQL 有高度客製需求且追求型別安全的團隊。它在 Native 下可行但實現細節較多，需要投入設定。但對複雜報表或特殊查詢，jOOQ 能提供其他框架難以比擬的強大 DSL 功能
medium.com
。若團隊具備 jOOQ 經驗或願意投入學習，這是不錯的技術選擇。
JDBI：在 Quarkus Native 中作為 MyBatis 的輕量替代也非常有吸引力。它讓介面式 SQL 查詢變得簡單直觀，同時透過擴充確保相容性
quarkus.io
。適合中小型應用或對框架侵入性敏感的場合。
如果應用考慮未來的高併發或反應式架構，可以從一開始就採用 Vert.x Reactive SQL Client 或 Hibernate Reactive。尤其是 Vert.x 客戶端，在 Native 模式下性能表現優異，又無額外相容性負擔，非常適合對吞吐量要求高的服務。
vertx.io
原生 JDBC 雖然在任何情況下都「能用」，但除非團隊極為熟悉並已封裝好自己的資料層模板，否則直接以 JDBC 開發大型應用會增加不少樣板工作和潛在錯誤風險。在 Quarkus 提供了眾多更高層次的選項下，我們通常建議將 JDBC 作為底線方案或用於極簡單的場景。
最後，基於問題中的需求敘述（對性能無特別要求、查詢風格類似 MyBatis、對方式無強制偏好），我們的建議優先順序如下：
Hibernate ORM + Panache：作為通用解決方案，兼顧開發效率與穩定性。如果團隊沒有強烈的SQL掌控需求，這是最省心的選擇。
MyBatis：滿足直接寫SQL的偏好，同時保持與 Quarkus Native 的相容，非常符合題示的查詢風格。尤其適合已有 MyBatis 經驗的團隊。
JDBI：作為MyBatis的替代，如果更喜歡輕量工具，可考慮。但相較而言 MyBatis 社群更大，遇到問題資源更多。
jOOQ：在需要高度複雜查詢或希望利用DSL避免SQL錯誤時選用。若採用，建議投入時間設定 Quarkus 擴充並充分測試。
Vert.x Reactive SQL Client：若未來可能需要Reactive能力或更高的可擴展性，可以考慮此方案。當前若無此需求，可延後評估。
原生 JDBC：除非有額外原因（如框架限制、既有大量JDBC代碼），一般不特別推薦作為首選，但它始終是可靠的備援方案。
總而言之，Quarkus 生態讓上述各種資料存取技術都能在原生編譯模式下運行。選擇適合的框架應視團隊技能組合、專案性質及長遠考量而定。希望本報告的比較能協助您評估出最適合您項目的方案。
quarkus.io
vertx.io
引用
Favicon
GraalVM native image compatibility · Issue #8779 · jOOQ/jOOQ · GitHub

https://github.com/jOOQ/jOOQ/issues/8779
Favicon
GraalVM native image compatibility · Issue #8779 · jOOQ/jOOQ · GitHub

https://github.com/jOOQ/jOOQ/issues/8779
Favicon
maven - Running Quarkus in native Mode fails: Image Generation failed. Does Quarkus native Mode support Jooq Generation - Stack Overflow

https://stackoverflow.com/questions/71516158/running-quarkus-in-native-mode-fails-image-generation-failed-does-quarkus-nati
Favicon
GraalVM native image compatibility · Issue #8779 · jOOQ/jOOQ · GitHub

https://github.com/jOOQ/jOOQ/issues/8779
Favicon
maven - Running Quarkus in native Mode fails: Image Generation failed. Does Quarkus native Mode support Jooq Generation - Stack Overflow

https://stackoverflow.com/questions/71516158/running-quarkus-in-native-mode-fails-image-generation-failed-does-quarkus-nati
Favicon
All extensions - Quarkus

https://quarkus.io/extensions/
Favicon
Comparing Hibernate vs jOOQ vs MyBatis for High-Performance ...

https://medium.com/@ShantKhayalian/comparing-hibernate-vs-jooq-vs-mybatis-for-high-performance-database-queries-e60d5ea47212
Favicon
Using Hibernate ORM and Jakarta Persistence - Quarkus

https://quarkus.io/guides/hibernate-orm
Favicon
Simplified Hibernate ORM with Panache - Quarkus

https://quarkus.io/guides/hibernate-orm-panache
Favicon
Using Hibernate ORM and Jakarta Persistence - Quarkus

https://quarkus.io/guides/hibernate-orm
Favicon
Using Hibernate ORM and Jakarta Persistence - Quarkus

https://quarkus.io/guides/hibernate-orm
Favicon
Using Hibernate ORM and Jakarta Persistence - Quarkus

https://quarkus.io/guides/hibernate-orm
Favicon
Using Hibernate ORM and Jakarta Persistence - Quarkus

https://quarkus.io/guides/hibernate-orm
Favicon
Using Hibernate ORM and Jakarta Persistence - Quarkus

https://quarkus.io/guides/hibernate-orm
Favicon
Simplified Hibernate ORM with Panache - Quarkus

https://quarkus.io/guides/hibernate-orm-panache
Favicon
Simplified Hibernate ORM with Panache - Quarkus

https://quarkus.io/guides/hibernate-orm-panache
Favicon
Simplified Hibernate ORM with Panache - Quarkus

https://quarkus.io/guides/hibernate-orm-panache
Favicon
Simplified Hibernate ORM with Panache - Quarkus

https://quarkus.io/guides/hibernate-orm-panache
Favicon
MyBatis SQL Mapper | Extensions

https://quarkus.io/extensions/io.quarkiverse.mybatis/quarkus-mybatis/
Favicon
MyBatis SQL Mapper | Extensions

https://quarkus.io/extensions/io.quarkiverse.mybatis/quarkus-mybatis/
Favicon
java - Quarkus native image build fails - Stack Overflow

https://stackoverflow.com/questions/61013910/quarkus-native-image-build-fails
Favicon
java - Quarkus native image build fails - Stack Overflow

https://stackoverflow.com/questions/61013910/quarkus-native-image-build-fails
Favicon
MyBatis SQL Mapper | Extensions

https://quarkus.io/extensions/io.quarkiverse.mybatis/quarkus-mybatis/
Favicon
PostgreSQL | Eclipse Vert.x

https://vertx.io/docs/vertx-pg-client/java/
Favicon
Jdbi | Extensions

https://quarkus.io/extensions/io.quarkiverse.jdbi/quarkus-jdbi/
Favicon
Quarkus Jdbi :: Quarkiverse Documentation

https://docs.quarkiverse.io/quarkus-jdbi/dev/index.html
すべての情報源
Favicongithub
Faviconstackoverflow
Faviconquarkus
Faviconmedium
Faviconvertx
Favicondocs.quarkiverse