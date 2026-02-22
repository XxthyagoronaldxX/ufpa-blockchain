# Blockchain Java - Criptomoeda Distribuída Simplificada

Implementação de um sistema distribuído de blockchain para trabalho acadêmico de Sistemas Distribuídos, inspirado no Bitcoin com foco nos fundamentos de sistemas distribuídos.

## 🎯 Objetivo do Trabalho

Desenvolver um sistema distribuído de criptomoeda/transação simplificada, executando em múltiplos computadores do laboratório, no qual cada computador representa um nó da rede, mantendo uma cópia local de uma blockchain, comunicando-se com outros nós por meio de sockets e utilizando um mecanismo simples de consenso.

## ✅ Conformidade com Especificações

### Nó da Rede (Node)
- ✅ Executa como processo independente
- ✅ Porta configurável e única para todos os nós
- ✅ Bootstrap (conhece ao menos um nó inicial)
- ✅ Mantém cópia local da blockchain
- ✅ Mantém conjunto de transações pendentes
- ✅ **Sem servidor central** (arquitetura P2P pura)

### Transações
Campos obrigatórios implementados:
- ✅ **id**: Identificador único (UUID)
- ✅ **origem** (sender): Endereço do remetente
- ✅ **destino** (recipient): Endereço do destinatário
- ✅ **valor** (amount): Valor da transação
- ✅ **timestamp**: Marca temporal

Regras implementadas:
- ✅ Somente valores positivos (> 0)
- ✅ **Não permitir saldo negativo em nenhuma hipótese**

### Blocos
Campos obrigatórios implementados:
- ✅ **index**: Índice do bloco
- ✅ **previousHash**: Hash do bloco anterior
- ✅ **transactions**: Lista de transações
- ✅ **nonce**: Valor encontrado no PoW
- ✅ **timestamp**: Marca temporal
- ✅ **hash**: Hash do bloco atual (SHA-256)

### Blockchain
- ✅ Bloco gênesis fixo (índice 0)
- ✅ Cada bloco referencia corretamente o anterior
- ✅ Validação completa da cadeia

### Consenso Distribuído (Proof of Work)
- ✅ PoW simplificado: hash deve começar com **"000"**
- ✅ Dificuldade fixa
- ✅ Primeiro nó a encontrar bloco válido:
  - Adiciona à sua blockchain
  - Propaga para demais nós
- ✅ Blocos recebidos são validados antes de aceitos
- ✅ **Consenso pela cadeia mais longa**

### Protocolo de Comunicação
- ✅ **NEW_TRANSACTION**: Envio de nova transação
- ✅ **NEW_BLOCK**: Envio de bloco minerado
- ✅ **REQUEST_CHAIN**: Solicitação da blockchain completa
- ✅ **RESPONSE_CHAIN**: Envio da blockchain para sincronização
- ✅ Comunicação via **sockets TCP/IP**
- ✅ Mensagens serializadas em **JSON**

📄 **[Ver documentação completa do protocolo](PROTOCOL.md)**

## 🏗️ Estrutura do Projeto

```
BlockchainJava/
├── pom.xml                          # Configuração Maven
├── README.md                        # Documentação principal
├── PROTOCOL.md                      # Documentação do protocolo
├── QUICK_START.md                   # Guia rápido
└── src/
    ├── main/java/com/blockchain/
    │   ├── Block.java               # Bloco com PoW (SHA-256)
    │   ├── Blockchain.java          # Gerenciamento da cadeia + validações
    │   ├── Transaction.java         # Transação com ID único
    │   ├── Protocol.java            # Constantes do protocolo
    │   ├── Message.java             # Mensagens JSON
    │   ├── Node.java                # Nó P2P da rede
    │   └── Main.java                # Aplicação principal
    └── test/java/com/blockchain/
        ├── BlockTest.java           # Testes de bloco
        ├── BlockchainTest.java      # Testes de blockchain
        └── TransactionTest.java     # Testes de transação
```

## 🚀 Requisitos

- **Java 17** ou superior
- **Maven 3.6+**
- **Rede local** com conectividade TCP/IP entre máquinas

## 📦 Dependências

- **Gson 2.10.1**: Serialização/deserialização JSON
- **JUnit 5.10.0**: Testes unitários

## 🔧 Compilação

```bash
# Limpar e compilar
mvn clean compile

# Executar testes
mvn test

# Gerar JAR executável
mvn package
```

## 💻 Executando o Sistema

### Modo Demonstração (Testes Locais)

```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="--demo"
```

Demonstra:
- ✅ Validação de transações (apenas valores positivos)
- ✅ Validação de saldo (não permite negativo)
- ✅ Proof of Work (mineração)
- ✅ Validação de blockchain

### Modo Rede Distribuída

#### Laboratório - Máquinas Diferentes

**Máquina 1 (192.168.1.10):**
```bash
java -jar target/blockchain-java-1.0.0.jar 5000 192.168.1.10
```

**Máquina 2 (192.168.1.11):**
```bash
java -jar target/blockchain-java-1.0.0.jar 5000 192.168.1.11

# Conectar ao primeiro nó (bootstrap)
> 5. Conectar a peer
> Host: 192.168.1.10
> Porta: 5000
```

**Máquina 3 (192.168.1.12):**
```bash
java -jar target/blockchain-java-1.0.0.jar 5000 192.168.1.12

# Conectar a qualquer nó existente
> 5. Conectar a peer
> Host: 192.168.1.11
> Porta: 5000
```

#### Testes Locais (Mesma Máquina)

**Terminal 1:**
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5000"
```

**Terminal 2:**
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5001"
```

**Terminal 3:**
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5002"
```

## 📋 Menu Interativo

```
╔════════════════════════════════════╗
║        MENU BLOCKCHAIN             ║
╠════════════════════════════════════╣
║ 1. Criar transação                 ║
║ 2. Minerar bloco                   ║
║ 3. Mostrar blockchain              ║
║ 4. Consultar saldo                 ║
║ 5. Conectar a peer                 ║
║ 6. Listar peers                    ║
║ 7. Validar chain                   ║
║ 8. Informações do nó               ║
║ 9. Criar funding (teste)           ║
║ 0. Sair                            ║
╚════════════════════════════════════╝
```

## 🎓 Cenário de Uso no Laboratório

### 1. Preparação
```bash
# Em cada máquina, compilar o projeto
mvn clean package
```

### 2. Nó Bootstrap (Primeira Máquina)
```bash
# Iniciar nó principal
java -jar target/blockchain-java-1.0.0.jar 5000 192.168.1.10

# Criar saldo inicial para testes
> 9. Criar funding
> Alice: 1000.0

> 9. Criar funding
> Bob: 1000.0
```

### 3. Demais Nós (Outras Máquinas)
```bash
# Conectar à rede
> 5. Conectar a peer
> Host: 192.168.1.10
> Porta: 5000

# Verificar sincronização
> 3. Mostrar blockchain
```

### 4. Criar Transações (Qualquer Nó)
```bash
> 1. Criar transação
> Remetente: Alice
> Destinatário: Bob
> Valor: 100.0
✓ Transação criada e transmitida!
```

### 5. Minerar Blocos (Qualquer Nó)
```bash
> 2. Minerar bloco
Minerando 1 transações...
Bloco minerado: 000abc123...
✓ Bloco minerado e transmitido!
```

### 6. Verificar Consenso (Todos os Nós)
```bash
> 3. Mostrar blockchain
# Todos os nós devem ter a mesma blockchain!

> 4. Consultar saldo
> Alice
Saldo: 900.0

> 4. Consultar saldo
> Bob
Saldo: 1100.0
```

## 🔍 Validações Implementadas

### Transações
```java
// ✅ Aceita
new Transaction("Alice", "Bob", 50.0);

// ✗ Rejeita: valor zero/negativo
new Transaction("Alice", "Bob", 0.0);    // IllegalArgumentException
new Transaction("Alice", "Bob", -10.0);  // IllegalArgumentException

// ✗ Rejeita: saldo insuficiente
// Se Alice tem 40.0:
blockchain.addTransaction(new Transaction("Alice", "Bob", 50.0));
// IllegalArgumentException: "Saldo insuficiente!"
```

### Blocos
```java
// Validações automáticas:
// ✅ Índice sequencial
// ✅ Hash anterior correto
// ✅ Proof of Work válido (hash começa com "000")
// ✅ Hash calculado = hash armazenado
```

## 🧪 Executar Testes

```bash
# Todos os testes
mvn test

# Teste específico
mvn test -Dtest=BlockchainTest

# Com relatório detalhado
mvn test -Dtest=BlockchainTest#testNoNegativeBalance
```

## 🛠️ Configurações

### Alterar Dificuldade do PoW
Em [Protocol.java](src/main/java/com/blockchain/Protocol.java):
```java
public static final String DIFFICULTY = "0000"; // 4 zeros = mais difícil
```

### Alterar Porta do Nó
```bash
java -jar blockchain.jar <PORTA> [HOST]

# Exemplos:
java -jar blockchain.jar 5000
java -jar blockchain.jar 5000 192.168.1.10
```

## 📊 Características Técnicas

| Característica | Implementação |
|----------------|---------------|
| Linguagem | Java 17 |
| Build Tool | Maven 3.11 |
| Hash Algorithm | SHA-256 |
| Network | TCP/IP Sockets |
| Serialization | JSON (Gson) |
| Consensus | Longest Chain |
| PoW Difficulty | "000" (3 zeros) |
| ID Generation | UUID v4 |

## 📚 Documentação Adicional

- **[PROTOCOL.md](PROTOCOL.md)**: Documentação completa do protocolo de comunicação
- **[QUICK_START.md](QUICK_START.md)**: Guia rápido de início

## 👥 Trabalho em Grupo

Este projeto foi desenvolvido para ser executado em grupo (até 3 estudantes), com cada grupo mantendo um nó da rede blockchain.

**Responsabilidades do grupo:**
- Manter o nó funcionando durante os testes
- Participar ativamente da rede P2P
- Validar transações e blocos recebidos
- Contribuir com a mineração
- Sincronizar com outros nós

## 📄 Licença

Projeto acadêmico - Sistemas Distribuídos - Fevereiro 2026

## 📋 Características

- ✅ **Proof of Work**: Mineração com dificuldade configurável
- ✅ **Transações**: Sistema de transações entre endereços
- ✅ **Rede P2P**: Comunicação entre nós via sockets
- ✅ **Protocolo JSON**: Mensagens padronizadas (NEW_TRANSACTION, NEW_BLOCK, REQUEST_CHAIN, RESPONSE_CHAIN)
- ✅ **Sincronização**: Consenso automático pela cadeia mais longa
- ✅ **Recompensa de mineração**: Incentivo para mineradores
- ✅ **Validação de cadeia**: Verificação de integridade completa

## 🏗️ Estrutura do Projeto

```
BlockchainJava/
├── pom.xml                          # Configuração Maven
├── README.md
└── src/
    ├── main/java/com/blockchain/
    │   ├── Block.java               # Bloco da blockchain com PoW
    │   ├── Blockchain.java          # Gerenciamento da cadeia
    │   ├── Transaction.java         # Transações entre endereços
    │   ├── Protocol.java            # Constantes do protocolo
    │   ├── Message.java             # Mensagens de rede (JSON)
    │   ├── Node.java                # Nó P2P da rede
    │   └── Main.java                # Aplicação principal
    └── test/java/com/blockchain/
        ├── BlockTest.java
        ├── BlockchainTest.java
        └── TransactionTest.java
```

## 🚀 Requisitos

- Java 17 ou superior
- Maven 3.6+

## 📦 Dependências

- **Gson 2.10.1**: Serialização/deserialização JSON
- **JUnit 5.10.0**: Testes unitários

## 🔧 Instalação e Compilação

```bash
# Compilar o projeto
mvn clean compile

# Executar testes
mvn test

# Empacotar (gera JAR)
mvn package
```

## 💻 Como Usar

### Modo Demonstração

Executa operações básicas para demonstrar funcionalidades:

```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="--demo"
```

### Modo Interativo (Nó de Rede)

Inicia um nó da rede blockchain:

```bash
# Nó 1 na porta 5000
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5000"

# Nó 2 na porta 5001 (em outro terminal)
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5001"

# Nó 3 na porta 5002 (em outro terminal)
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5002"
```

### Menu Interativo

O menu permite:

1. **Criar transação**: Adiciona transação ao pool de pendentes
2. **Minerar bloco**: Executa PoW e adiciona bloco à chain
3. **Mostrar blockchain**: Exibe todos os blocos
4. **Consultar saldo**: Verifica saldo de um endereço
5. **Conectar a peer**: Conecta a outro nó e sincroniza
6. **Listar peers**: Mostra nós conectados
7. **Validar chain**: Verifica integridade da blockchain
8. **Informações do nó**: Status do nó atual

## 🌐 Protocolo de Rede

### Tipos de Mensagens

```java
NEW_TRANSACTION  // Broadcast de nova transação
NEW_BLOCK        // Broadcast de bloco minerado
REQUEST_CHAIN    // Solicita blockchain completa
RESPONSE_CHAIN   // Envia blockchain para sincronização
```

### Formato de Mensagem (JSON)

```json
{
  "type": "NEW_BLOCK",
  "data": { ...dados do bloco... },
  "timestamp": 1234567890,
  "senderHost": "localhost",
  "senderPort": 5000
}
```

### Configurações do Protocolo

- **BUFFER_SIZE**: 4096 bytes
- **ENCODING**: UTF-8
- **DIFFICULTY**: "000" (3 zeros - ajustável)
- **MINING_REWARD**: 10.0

## 📝 Exemplo de Uso em Rede

### Terminal 1 (Nó A - Porta 5000)
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5000"

# No menu:
1. Criar transação
   Remetente: Alice
   Destinatário: Bob
   Valor: 50

2. Minerar bloco
```

### Terminal 2 (Nó B - Porta 5001)
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5001"

# No menu:
5. Conectar a peer
   Host do peer: localhost
   Porta do peer: 5000

# A blockchain será sincronizada automaticamente!
```

## 🔍 Fluxo de Consenso

1. **Transação**: Criada e transmitida para todos os peers
2. **Mineração**: Nó executa PoW para encontrar nonce válido
3. **Broadcast**: Bloco minerado é enviado para a rede
4. **Validação**: Peers verificam:
   - Hash do bloco anterior
   - Proof of Work (hash com dificuldade correta)
   - Índice sequencial
5. **Adição**: Bloco válido é adicionado à chain local
6. **Sincronização**: Chain mais longa prevalece

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar teste específico
mvn test -Dtest=BlockTest

# Com cobertura
mvn test jacoco:report
```

## 🛠️ Configuração Avançada

### Alterar Dificuldade de Mineração

No [Protocol.java](src/main/java/com/blockchain/Protocol.java):
```java
public static final String DIFFICULTY = "0000"; // 4 zeros = mais difícil
```

### Alterar Recompensa de Mineração

```java
public static final double MINING_REWARD = 50.0; // Bitcoin-style
```

## 📚 Recursos Úteis

- [Whitepaper Bitcoin](https://bitcoin.org/bitcoin.pdf)
- [Proof of Work Explained](https://en.wikipedia.org/wiki/Proof_of_work)
- [Blockchain Consensus](https://en.wikipedia.org/wiki/Consensus_(computer_science))

## 🤝 Contribuindo

Contribuições são bem-vindas! Por favor:
1. Faça um fork do projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob licença MIT. Veja o arquivo LICENSE para mais detalhes.

## ✨ Autor

Desenvolvido como implementação educacional de blockchain P2P em Java.
