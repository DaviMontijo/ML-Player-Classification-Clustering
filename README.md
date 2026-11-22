<h1 align="center">Machine Learning — Player Profiling & Classification System</h1>

<p align="center">
  <img
    src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=20&pause=1000&color=2E9EF7&center=true&vCenter=true&width=600&lines=Machine+Learning+in+Pure+Java+%F0%9F%9A%80;KNN+%26+K-Means+Implemented+from+Scratch;MySQL+JDBC+%7C+DAO+Pattern+%7C+Polymorphism;Zero+External+ML+Libraries+%F0%9F%92%BB"
    alt="Typing SVG"
  />
</p>

---

### 📌 About the Project

**Machine Learning — Player Profiling & Classification System** is a backend data science and object-oriented software engineering project built to analyze, classify, and segment **150 online game players** stored in a relational **MySQL** database (`jogo_db`). Based on four behavioral metrics — **weekly hours played**, **weekly matches**, **monthly spending (R$)**, and **win rate (%)** — the system categorizes users into three behavioral tiers: **`CASUAL`**, **`REGULAR`**, and **`RANQUEADO`**.

Unlike conventional machine learning pipelines that rely on high-level frameworks (such as Scikit-Learn, Weka, or Smile), **every mathematical and algorithmic component in this repository was implemented manually from scratch in pure Java**, using only the standard JDK API and the official MySQL JDBC driver. The project bridges **relational database querying (SQL)** with **Object-Oriented Programming (OOP)** principles, evaluating how feature scaling (Min-Max normalization) and distance metrics affect model accuracy and cluster purity.

---

### 🧠 Logic & System Architecture

#### 1. Manual Machine Learning Engine (Zero External ML Libraries)
- **Supervised Classification (`KNN`):** Implements *K-Nearest Neighbors* from scratch, evaluating candidate neighbors, handling three-class voting ties via a deterministic tie-breaking rule (selecting the tied profile with the closest individual neighbor), and preventing *data leakage* by fitting the `Normalizador` exclusively on the 80% training split before transforming the 20% test set.
- **Unsupervised Clustering (`KMeans`):** Implements centroid initialization, iterative point assignment, empty-cluster recovery, inertia calculation, and cluster purity evaluation across **20 distinct random seeds** per experiment to avoid local minima.
- **Native Chart Rendering (`GeradorGrafico`):** Generates the Elbow Method curve (`grafico_cotovelo.png`) directly in Java using `BufferedImage`, `Graphics2D`, and `ImageIO`, with zero external plotting dependencies.

#### 2. Polymorphic Distance Metrics & Scaling
- **Decoupled Metrics (`MedidaDeDistancia`):** Uses an interface-driven design that allows `KNN` to switch seamlessly between **Euclidean Distance** and **Manhattan Distance** via dependency injection without altering the classifier's core logic.
- **Min-Max Normalization (`Normalizador`):** Rescales all four behavioral attributes to the [0,1] interval so that high-magnitude variables (such as `gasto_mensal` in BRL) do not dominate distance calculations over percentage or hourly metrics, while supporting inverse transformation (`desnormalizar`) to interpret K-Means centroids on their original scales.

#### 3. Relational Persistence & Codebase Organization
- **DAO Pattern (`JogadorDAO` / `JogadorDAOJdbc`):** Encapsulates all MySQL interactions behind an interface using parameterized `PreparedStatement` queries and automatic database/schema bootstrapping from `sql/jogadores.sql`.
- **Immutable Domain Model (`Jogador` & `Perfil`):** Represents player entities as `final` immutable objects with defensive array copying (`getCaracteristicas()`) and strongly typed `Enum` profiles.

---

### 📊 Key Experimental Results

- **Dataset & SQL Profiling (Part A):** Perfectly balanced dataset of **150 players** (50 per profile), showing clear behavioral progression from `CASUAL` (`4.85 h/week`, `41.35%` win rate) to `REGULAR` (`14.29 h/week`, `50.91%` win rate) and `RANQUEADO` (`26.39 h/week`, `59.87%` win rate).
- **KNN Classification & Distance Investigation (Parts B & E):**
  - Achieved **100.00% test accuracy** on the baseline split (K = 3, Euclidean + Min-Max Normalization), compared to **86.67%** without normalization.
  - Across **30 randomized 80/20 splits**, **Euclidean** and **Manhattan** deliver statistically equivalent accuracy when data is normalized (**96.44% to 98.33%** mean accuracy, with differences under `1.25 p.p.` within standard deviation bounds). Without normalization, Manhattan consistently outperforms Euclidean (`93.67%` vs. `90.89%` at K = 3) because it does not quadratically amplify the scale distortion of `gasto_mensal`.
- **K-Means Unsupervised Clustering (Part C):**
  - The **Elbow Method** (K = 1..6) confirmed K = 3 as the natural inflection point (inertia dropping from `27.94` at K=1 to `10.55` at K=3).
  - Feature scaling increased overall cluster purity from **51.33%** (unnormalized) to **80.00%** (normalized), isolating **100% of the 50 `RANQUEADO` players** into a single cluster without using labels.

---

### 🚀 How to Run the Project

#### Prerequisites
- **Java JDK 17+** installed and configured in your system `PATH`.
- **MySQL Server** running on the default port (`localhost:3306`) with a valid username and password (e.g., `root`).
- **MySQL Connector/J (`.jar`)** located inside the `lib/` directory.

#### Execution Directory Notice
> **Important:** All commands below must be executed from the **root directory of the project** (where `src/`, `sql/`, and `lib/` are located), and **never** from inside `src/`. If your terminal is currently inside `src/`, navigate one level up before compiling:
> ```bash
> cd ..
> ```

#### Step-by-Step Terminal Commands

- **Windows — PowerShell (default VS Code terminal):**
  ```
  javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName
  java -cp "bin;lib/*" app.Main

- **Windows — CMD (Command Prompt):**
  ```
  dir /s /B src\*.java > fontes.txt
  javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin @fontes.txt
  del fontes.txt
  java -cp "bin;lib/*" app.Main

- **Linux / macOS(Bash/Zsh)**
  ```
  javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin $(find src -name "*.java")
  java -cp "bin:lib/*" app.Main

Upon starting, enter your MySQL credentials in the console. The application will automatically initialize the jogo_db schema, import the 150 records from sql/jogadores.sql, run all SQL queries and ML experiments, and export grafico_cotovelo.png to the project root.

---

### 🛠️ Technologies Used

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,mysql,git,github" alt="Technologies Used" />
  </a>
</p>

---

### 📂 Repository Structure

```text
ML-Player-Classification-Clustering/
├── lib/
│   └── mysql-connector-j-26.10.0.jar     # Official MySQL JDBC driver
├── sql/
│   └── jogadores.sql                     # Database schema and initial 150-player dataset
├── src/
│   ├── app/
│   │   └── Main.java                     # Application entry point, DB bootstrap & orchestration
│   ├── dao/
│   │   ├── JogadorDAO.java               # Data Access Object interface
│   │   └── JogadorDAOJdbc.java           # JDBC implementation using PreparedStatement
│   ├── grafico/
│   │   └── GeradorGrafico.java           # Native PNG chart renderer for the Elbow Method
│   ├── ml/
│   │   ├── MedidaDeDistancia.java        # Polymorphic distance metric interface
│   │   ├── DistanciaEuclidiana.java      # Euclidean distance (L2) implementation
│   │   ├── DistanciaDeManhattan.java     # Manhattan distance (L1) implementation
│   │   ├── Normalizador.java             # Min-Max [0, 1] scaler and inverse transformer
│   │   ├── knn/
│   │   │   ├── KNN.java                  # K-Nearest Neighbors classifier & tie-breaker
│   │   │   └── ExperimentoKNN.java       # K evaluation, confusion matrix & distance study
│   │   └── kmeans/
│   │       ├── KMeans.java               # K-Means clustering, inertia & purity calculation
│   │       └── ExperimentoKmeans.java    # 20-seed Elbow Method & normalization comparison
│   └── model/
│       ├── Jogador.java                  # Immutable player domain entity
│       └── Perfil.java                   # Strongly typed player profile Enum
├── .gitignore                            # Git ignore rules
├── grafico_cotovelo.png                  # Generated Elbow Method inertia plot
└── README.md                             # Project documentation

  
  
