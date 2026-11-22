Como Executar o Projeto

####  Pré-requisitos
1. **Java JDK 17+** instalado e configurado no `PATH`.
2. **Servidor MySQL** ativo na porta padrão (`localhost:3306`) com um **usuário e senha válidos** (ex.: `root`).
3. Driver **MySQL Connector/J** (`.jar`) presente na pasta `lib/` do projeto[cite: 1].

####  Atenção ao Diretório de Execução
> **Importante:** Todos os comandos abaixo devem ser executados a partir da **pasta raiz do projeto** (onde ficam as pastas `src/`, `sql/` e `lib/`), e **nunca de dentro da pasta `src/`**[cite: 1]. Caso o seu terminal esteja dentro de `src/`, volte um nível antes de compilar:
> ```bash
> cd ..
> ```

#### Passo a Passo pelo Terminal

Escolha os comandos de acordo com o terminal utilizado:

- **Windows — PowerShell (terminal padrão do VS Code):**
  javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName
  java -cp "bin;lib/*" app.Main


- **Windows — CMD (Prompt de Comando):**

dir /s /B src\*.java > fontes.txt
javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin @fontes.txt
del fontes.txt
java -cp "bin;lib/*" app.Main


**Linux / macOS (Bash / Zsh):**

javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin $(find src -name "*.java")
java -cp "bin:lib/*" app.Main

