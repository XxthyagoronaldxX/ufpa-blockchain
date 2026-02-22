# Adaptações para o Trabalho Acadêmico

## ✅ Checklist de Conformidade

### Requisitos Implementados

#### 1. Nó da Rede ✅
- [x] Processo independente
- [x] Porta configurável e única
- [x] Bootstrap (conhece ao menos um nó inicial)
- [x] Cópia local da blockchain
- [x] Pool de transações pendentes
- [x] Sem servidor central (P2P puro)

#### 2. Transações ✅
**Campos implementados:**
- [x] `id`: Identificador único (UUID)
- [x] `sender`: Origem
- [x] `recipient`: Destino 
- [x] `amount`: Valor
- [x] `timestamp`: Marca temporal

**Regras implementadas:**
- [x] Somente valores positivos (amount > 0)
- [x] Não permitir saldo negativo em nenhuma hipótese

#### 3. Blocos ✅
**Campos implementados:**
- [x] `index`: Índice do bloco
- [x] `previousHash`: Hash do bloco anterior
- [x] `transactions`: Lista de transações
- [x] `nonce`: Valor do PoW
- [x] `timestamp`: Marca temporal
- [x] `hash`: Hash do bloco (SHA-256)

#### 4. Blockchain ✅
- [x] Bloco gênesis fixo
- [x] Referências corretas de hash
- [x] Validação de toda a cadeia

#### 5. Consenso (PoW) ✅
- [x] Hash deve começar com "000"
- [x] Dificuldade fixa
- [x] Propagação de blocos minerados
- [x] Validação antes de aceitar
- [x] Cadeia mais longa prevalece

#### 6. Protocolo de Comunicação ✅
- [x] NEW_TRANSACTION
- [x] NEW_BLOCK
- [x] REQUEST_CHAIN
- [x] RESPONSE_CHAIN
- [x] Sockets TCP/IP
- [x] JSON (UTF-8)

---

## 🔄 Principais Mudanças Realizadas

### 1. Transaction.java
**Antes:**
```java
private String sender;
private String recipient;
private double amount;
private long timestamp;
```

**Depois:**
```java
private String id;          // ✨ NOVO: ID único (UUID)
private String sender;
private String recipient;
private double amount;
private long timestamp;

// ✨ NOVO: Validação de valor positivo
if (amount <= 0) {
    throw new IllegalArgumentException("Valor deve ser positivo");
}
```

### 2. Blockchain.java
**Antes:**
```java
public Block minePendingTransactions(String minerAddress) {
    // Adiciona recompensa
    Transaction rewardTx = new Transaction("NETWORK", minerAddress, 10.0);
    pendingTransactions.add(rewardTx);
    // ...
}
```

**Depois:**
```java
public Block minePendingTransactions() {
    // ✨ SEM recompensa automática
    
    // ✨ NOVO: Validação de saldo antes de adicionar transação
    double currentBalance = getBalance(transaction.getSender());
    
    // Considera transações pendentes
    for (Transaction pending : pendingTransactions) {
        if (pending.getSender().equals(transaction.getSender())) {
            currentBalance -= pending.getAmount();
        }
    }
    
    // ✨ REGRA CRÍTICA: Não permitir saldo negativo
    if (currentBalance - transaction.getAmount() < 0) {
        throw new IllegalArgumentException("Saldo insuficiente!");
    }
}
```

### 3. Protocol.java
**Antes:**
```java
public static final double MINING_REWARD = 10.0;
```

**Depois:**
```java
// ❌ REMOVIDO: Recompensa não especificada no trabalho
// Foco nos fundamentos de sistemas distribuídos
```

### 4. Node.java
**Antes:**
```java
public void mineBlock(String minerAddress) {
    Block newBlock = blockchain.minePendingTransactions(minerAddress);
    // ...
}
```

**Depois:**
```java
public void mineBlock() {
    Block newBlock = blockchain.minePendingTransactions();
    // ✨ Sem minerAddress - foco em consenso distribuído
}

// ✨ NOVO: Método para funding inicial (testes)
public void fundAddress(String address, double amount) {
    blockchain.fundAddress(address, amount);
}
```

### 5. Main.java
**Mudanças no modo demonstração:**
```java
// ✨ NOVO: Funding inicial para testes
blockchain.fundAddress("Alice", 100.0);
blockchain.fundAddress("Bob", 100.0);

// ✨ NOVO: Teste de saldo negativo
try {
    blockchain.addTransaction(new Transaction("Alice", "Bob", 1000.0));
} catch (IllegalArgumentException e) {
    System.out.println("✓ Rejeitado: " + e.getMessage());
}

// ✨ NOVO: Teste de valor positivo
try {
    new Transaction("Alice", "Bob", -10.0);
} catch (IllegalArgumentException e) {
    System.out.println("✓ Rejeitado: " + e.getMessage());
}
```

---

## 🎯 Implementações Específicas para o Trabalho

### Validação de Saldo Negativo
```java
// blockchain.addTransaction() agora verifica:
// 1. Saldo atual na blockchain
// 2. Transações pendentes do mesmo sender
// 3. Se saldo final seria negativo → REJEITA

// Exemplo:
// Alice tem 100.0
blockchain.addTransaction(new Transaction("Alice", "Bob", 60.0));   // OK, pendente
blockchain.addTransaction(new Transaction("Alice", "Charlie", 50.0)); // ✗ FALHA!
// Motivo: 100 - 60 - 50 = -10 (saldo negativo não permitido)
```

### ID Único nas Transações
```java
// Cada transação tem UUID automático
Transaction tx1 = new Transaction("Alice", "Bob", 50.0);
Transaction tx2 = new Transaction("Alice", "Bob", 50.0);

tx1.getId(); // "c4f3a7d2-8e91-4b5c-9a12-3d4e5f6a7b8c"
tx2.getId(); // "7e8d9c0b-1a2f-3e4d-5c6b-7a8e9d0c1f2e"
// IDs sempre únicos!
```

### Bloco Gênesis Fixo
```java
// Sempre o mesmo bloco gênesis:
// - Índice: 0
// - Previous Hash: "0"
// - Transações: []
// - Minerado com PoW correto
```

### Proof of Work Consistente
```java
// Hash DEVE começar com "000"
"000abc123def456..." // ✅ Válido
"00abc123def456..."  // ✗ Inválido (apenas 2 zeros)
"abc123def456..."    // ✗ Inválido (sem zeros no início)
```

---

## 📊 Testes Automatizados

### Novos Testes Adicionados

#### TransactionTest.java
- `testUniqueIds()`: Verifica IDs únicos
- `testOnlyPositiveValues()`: Rejeita valores ≤ 0

#### BlockchainTest.java
- `testAddTransactionWithInsufficientBalance()`: Valida saldo
- `testNoNegativeBalance()`: Não permite saldo negativo
- `testOnlyPositiveValues()`: Valida valores positivos
- `testPendingTransactionsValidation()`: Considera pendentes

### Executar Testes
```bash
# Todos os testes
mvn test

# Valida regra de saldo negativo
mvn test -Dtest=BlockchainTest#testNoNegativeBalance

# Valida valores positivos
mvn test -Dtest=TransactionTest#testOnlyPositiveValues
```

---

## 📁 Arquivos Criados/Modificados

### Novos Arquivos
- ✅ `PROTOCOL.md` - Documentação completa do protocolo
- ✅ `ADAPTACOES.md` - Este arquivo
- ✅ `QUICK_START.md` - Já existia, mantido

### Arquivos Modificados
- ✅ `Transaction.java` - ID único + validações
- ✅ `Blockchain.java` - Validação de saldo + sem recompensa
- ✅ `Protocol.java` - Removida recompensa
- ✅ `Node.java` - Ajustes nos métodos
- ✅ `Main.java` - Demo atualizada + novo menu
- ✅ `README.md` - Documentação completa do trabalho
- ✅ `BlockchainTest.java` - Novos testes
- ✅ `TransactionTest.java` - Novos testes
- ✅ `BlockTest.java` - Mantido

---

## 🚀 Como Testar Conformidade

### 1. Regra: Somente valores positivos
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="--demo"

# Observe a saída:
# 9. Testando regra de valor positivo...
# ✓ Transação corretamente rejeitada: Valor da transação deve ser positivo
```

### 2. Regra: Não permitir saldo negativo
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="--demo"

# Observe a saída:
# 8. Testando regra de saldo negativo...
# ✓ Transação corretamente rejeitada: Saldo insuficiente! ...
```

### 3. Protocolo de Rede
```bash
# Terminal 1
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5000"
> 9. Criar funding
> Alice: 100

> 1. Criar transação
> Alice -> Bob: 50

> 2. Minerar bloco

# Terminal 2
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5001"
> 5. Conectar a peer
> localhost:5000

> 3. Mostrar blockchain
# Deve mostrar os mesmos blocos! (Sincronização funcionando)
```

### 4. Consenso (Cadeia Mais Longa)
```bash
# Terminal 1: Minera 2 blocos
> 2. Minerar bloco
> 2. Minerar bloco

# Terminal 2: Minera 1 bloco
> 2. Minerar bloco

# Terminal 2 conecta ao Terminal 1:
> 5. Conectar a peer
> localhost:5000

# Terminal 2 irá substituir sua chain pela do Terminal 1
# (chain com 2 blocos > chain com 1 bloco)
```

---

## ✅ Conclusão

O sistema está **100% conforme** às especificações do trabalho acadêmico:

1. ✅ Todas as estruturas de dados implementadas
2. ✅ Todas as regras de validação implementadas
3. ✅ Protocolo de comunicação completo
4. ✅ Proof of Work funcional
5. ✅ Consenso distribuído implementado
6. ✅ Arquitetura P2P sem servidor central
7. ✅ Comunicação via sockets + JSON
8. ✅ Testes automatizados
9. ✅ Documentação completa

**Pronto para apresentação e demonstração no laboratório!** 🎉
