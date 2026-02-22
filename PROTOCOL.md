# Documentação do Protocolo de Comunicação - Blockchain Java

## 📋 Visão Geral

Este documento descreve o protocolo de comunicação utilizado pelo sistema de blockchain distribuído, conforme especificações do trabalho acadêmico.

## 🌐 Arquitetura da Rede

- **Topologia:** P2P (Peer-to-Peer) descentralizada
- **Comunicação:** Sockets TCP/IP
- **Serialização:** JSON (UTF-8)
- **Porta:** Configurável por nó (ex: 5000, 5001, 5002...)
- **Bootstrap:** Cada nó conhece ao menos um nó inicial

## 📨 Tipos de Mensagens

### 1. NEW_TRANSACTION
**Descrição:** Propagação de uma nova transação pela rede

**Quando usar:**
- Um nó cria uma nova transação válida
- A transação deve ser propagada (broadcast) para todos os peers

**Formato JSON:**
```json
{
  "type": "NEW_TRANSACTION",
  "data": {
    "id": "c4f3a7d2-8e91-4b5c-9a12-3d4e5f6a7b8c",
    "sender": "Alice",
    "recipient": "Bob",
    "amount": 50.0,
    "timestamp": 1708123456789
  },
  "timestamp": 1708123456789,
  "senderHost": "192.168.1.10",
  "senderPort": 5000
}
```

**Ação do receptor:**
1. Validar transação (valor positivo, campos obrigatórios)
2. Verificar saldo do remetente (não permitir saldo negativo)
3. Adicionar ao pool de transações pendentes
4. Adicionar remetente aos peers conhecidos

---

### 2. NEW_BLOCK
**Descrição:** Propagação de um bloco recém-minerado

**Quando usar:**
- Um nó completa a mineração de um bloco (encontra nonce válido)
- O bloco minerado deve ser propagado para a rede

**Formato JSON:**
```json
{
  "type": "NEW_BLOCK",
  "data": {
    "index": 2,
    "timestamp": 1708123456789,
    "transactions": [
      {
        "id": "tx-uuid-1",
        "sender": "Alice",
        "recipient": "Bob",
        "amount": 50.0,
        "timestamp": 1708123450000
      }
    ],
    "previousHash": "000a1b2c3d4e5f...",
    "nonce": 42389,
    "hash": "000f1e2d3c4b5a..."
  },
  "timestamp": 1708123456789,
  "senderHost": "192.168.1.10",
  "senderPort": 5000
}
```

**Ação do receptor:**
1. Validar índice sequencial (index = chain.size())
2. Validar hash do bloco anterior (previousHash = lastBlock.hash)
3. Validar Proof of Work (hash começa com "000")
4. Recalcular hash para verificar integridade
5. Se válido: adicionar à blockchain local
6. Se inválido: descartar
7. Remover transações do bloco do pool de pendentes

---

### 3. REQUEST_CHAIN
**Descrição:** Solicitação da blockchain completa

**Quando usar:**
- Nó entra na rede pela primeira vez (bootstrap)
- Nó detecta que está dessincronizado
- Após receber bloco com índice não sequencial

**Formato JSON:**
```json
{
  "type": "REQUEST_CHAIN",
  "data": null,
  "timestamp": 1708123456789,
  "senderHost": "192.168.1.15",
  "senderPort": 5001
}
```

**Ação do receptor:**
1. Enviar resposta com RESPONSE_CHAIN
2. Adicionar solicitante aos peers conhecidos

---

### 4. RESPONSE_CHAIN
**Descrição:** Envio da blockchain completa para sincronização

**Quando usar:**
- Resposta a uma mensagem REQUEST_CHAIN
- Para sincronizar um nó que acabou de entrar na rede

**Formato JSON:**
```json
{
  "type": "RESPONSE_CHAIN",
  "data": [
    {
      "index": 0,
      "timestamp": 1708100000000,
      "transactions": [],
      "previousHash": "0",
      "nonce": 12345,
      "hash": "000abc123..."
    },
    {
      "index": 1,
      "timestamp": 1708100100000,
      "transactions": [
        {
          "id": "tx-uuid-1",
          "sender": "GENESIS_FUND",
          "recipient": "Alice",
          "amount": 100.0,
          "timestamp": 1708100100000
        }
      ],
      "previousHash": "000abc123...",
      "nonce": 67890,
      "hash": "000def456..."
    }
  ],
  "timestamp": 1708123456789,
  "senderHost": "192.168.1.10",
  "senderPort": 5000
}
```

**Ação do receptor:**
1. Comparar tamanho da chain recebida com a local
2. Se chain recebida for maior: validar toda a chain
3. Se válida: substituir chain local (consenso pela mais longa)
4. Se inválida ou menor: ignorar

---

## 🔒 Validações de Segurança

### Transações
- ✅ ID único (UUID) obrigatório
- ✅ Sender e recipient obrigatórios
- ✅ Amount deve ser positivo (> 0)
- ✅ Timestamp obrigatório
- ✅ Saldo do sender deve ser suficiente
- ✅ **Nunca permitir saldo negativo**

### Blocos
- ✅ Índice sequencial (sem gaps)
- ✅ Hash do bloco anterior correto
- ✅ Proof of Work válido (hash começa com "000")
- ✅ Hash calculado = hash armazenado
- ✅ Todas as transações válidas

### Blockchain
- ✅ Bloco gênesis fixo (índice 0)
- ✅ Cadeia ininterrupta de hashes
- ✅ Todos os blocos válidos
- ✅ Consenso: cadeia mais longa prevalece

---

## 🔄 Fluxo de Comunicação

### Cenário 1: Nova Transação
```
Nó A                    Nó B                    Nó C
  |                       |                       |
  |--NEW_TRANSACTION----->|                       |
  |                       |                       |
  |                       |--NEW_TRANSACTION----->|
  |                       |                       |
  | (Transação adicionada ao pool de pendentes)  |
```

### Cenário 2: Mineração de Bloco
```
Nó A                    Nó B                    Nó C
  |                       |                       |
  | [Minerando PoW...]    |                       |
  |                       |                       |
  |--NEW_BLOCK----------->|                       |
  |                       |                       |
  |                       |--NEW_BLOCK----------->|
  |                       |                       |
  | (Bloco validado e adicionado à chain local)  |
```

### Cenário 3: Sincronização (Bootstrap)
```
Nó Novo                 Nó Existente
  |                       |
  |--REQUEST_CHAIN------->|
  |                       |
  |<--RESPONSE_CHAIN------|
  |                       |
  | [Valida chain]        |
  | [Substitui local]     |
  | [Sincronizado!]       |
```

### Cenário 4: Conflito de Chain (Fork)
```
Nó A (4 blocos)         Nó B (5 blocos)
  |                       |
  |--REQUEST_CHAIN------->|
  |                       |
  |<--RESPONSE_CHAIN------|
  |  (5 blocos)           |
  |                       |
  | [5 > 4]               |
  | [Valida chain]        |
  | [Substitui: 5 blocos] |
  | [Consenso atingido]   |
```

---

## 🛠️ Configurações do Protocolo

```java
BUFFER_SIZE = 8192          // Tamanho do buffer do socket (bytes)
ENCODING = "UTF-8"           // Codificação das mensagens JSON
DIFFICULTY = "000"           // Proof of Work: 3 zeros iniciais
```

---

## 📊 Estrutura de Dados

### Transação
```java
{
  "id": String (UUID),           // Identificador único
  "sender": String,              // Origem
  "recipient": String,           // Destino  
  "amount": Double,              // Valor (> 0)
  "timestamp": Long              // Milissegundos desde epoch
}
```

### Bloco
```java
{
  "index": Integer,              // Índice do bloco
  "timestamp": Long,             // Timestamp da criação
  "transactions": Array,         // Lista de transações
  "previousHash": String,        // Hash do bloco anterior (SHA-256)
  "nonce": Integer,              // Nonce encontrado (PoW)
  "hash": String                 // Hash do bloco atual (SHA-256)
}
```

---

## 🔐 Proof of Work (PoW)

**Algoritmo:**
1. Inicializar nonce = 0
2. Calcular hash = SHA256(index + timestamp + transactions + previousHash + nonce)
3. Se hash começa com "000": bloco minerado!
4. Senão: incrementar nonce e voltar ao passo 2

**Exemplo de hash válido:**
```
000f1e2d3c4b5a6e7d8c9b0a1f2e3d4c5b6a7e8d9c0b1a2f3e4d5c6b7a8e9d0c
^^^
3 zeros no início = válido
```

**Exemplo de hash inválido:**
```
f1e2d3c4b5a6e7d8c9b0a1f2e3d4c5b6a7e8d9c0b1a2f3e4d5c6b7a8e9d0c1f2
Não começa com "000" = inválido, continue minerando
```

---

## 🚀 Implementação Prática

### Iniciando um Nó
```bash
# Nó 1 (seed/bootstrap)
java -jar blockchain.jar 5000

# Nó 2 (conecta ao Nó 1)
java -jar blockchain.jar 5001
> 5. Conectar a peer
> Host: localhost
> Porta: 5000
```

### Criando Transações
```bash
> 1. Criar transação
> Remetente: Alice
> Destinatário: Bob
> Valor: 50.0
✓ Transação criada e transmitida!
```

### Minerando Blocos
```bash
> 2. Minerar bloco
Minerando 3 transações...
Bloco minerado: 000abc123...
✓ Bloco minerado e transmitido!
```

---

## ⚠️ Tratamento de Erros

| Erro | Ação |
|------|------|
| Transação com valor ≤ 0 | Rejeitar com mensagem de erro |
| Saldo insuficiente | Rejeitar com mensagem de erro |
| Bloco com índice inválido | Descartar bloco |
| Bloco com PoW inválido | Descartar bloco |
| Chain recebida menor | Ignorar RESPONSE_CHAIN |
| Chain recebida inválida | Ignorar RESPONSE_CHAIN |
| Erro de socket | Logar e continuar |

---

## 📝 Exemplo Completo de Sessão

```
[Terminal 1 - Nó A:5000]
> 9. Criar funding
> Alice: 100.0
> 9. Criar funding  
> Bob: 100.0

> 1. Criar transação
> Alice -> Bob: 50.0
✓ Transação criada!

> 2. Minerar bloco
Minerando...
✓ Bloco minerado: 000abc...

[Terminal 2 - Nó B:5001]
> 5. Conectar a peer
> localhost:5000
✓ Chain sincronizada!

> 3. Mostrar blockchain
Blockchain (2 blocos):
  Block{index=0, ...}
  Block{index=1, txCount=1, ...}

> 4. Consultar saldo
> Alice
Saldo: 50.0

> 4. Consultar saldo
> Bob
Saldo: 150.0
```

---

## 🔍 Conformidade com Especificações

| Requisito | Status | Observação |
|-----------|--------|------------|
| Comunicação via sockets | ✅ | Implementado com Java Sockets |
| Mensagens JSON | ✅ | Gson 2.10.1 |
| NEW_TRANSACTION | ✅ | Broadcast de transações |
| NEW_BLOCK | ✅ | Broadcast de blocos minerados |
| REQUEST_CHAIN | ✅ | Sincronização de nós |
| RESPONSE_CHAIN | ✅ | Envio de blockchain completa |
| Proof of Work | ✅ | Dificuldade "000" fixa |
| Bloco Gênesis | ✅ | Índice 0, hash anterior "0" |
| Consenso | ✅ | Cadeia mais longa prevalece |
| Validação | ✅ | Todas as regras implementadas |
| Sem servidor central | ✅ | Arquitetura P2P pura |

---

**Versão:** 1.0  
**Data:** Fevereiro 2026  
**Linguagem:** Java 17  
**Framework:** Maven
