# 🚀 Guia Rápido - Blockchain Java

## Instalação e Primeiro Uso

### 1. Compilar o projeto
```bash
mvn clean compile
```

### 2. Modo DEMO (Recomendado para começar)

Veja a blockchain em ação com transações e mineração automáticas:

```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="--demo"
```

**O que acontece:**
- ✅ Cria blockchain com bloco gênesis
- ✅ Adiciona transações (Alice → Bob, Bob → Charlie)
- ✅ Minera dois blocos
- ✅ Mostra saldos de todos os endereços
- ✅ Valida a integridade da chain

---

## 3. Modo Rede P2P (Múltiplos Nós)

### Terminal 1 - Nó Principal (Porta 5000)
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5000"
```

**Operações no Nó 1:**
```
Escolha: 1 (Criar transação)
  Remetente: Alice
  Destinatário: Bob
  Valor: 100

Escolha: 2 (Minerar bloco)
```

---

### Terminal 2 - Nó Secundário (Porta 5001)
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5001"
```

**Conectar ao Nó 1:**
```
Escolha: 5 (Conectar a peer)
  Host do peer: localhost
  Porta do peer: 5000
```

**Verificar sincronização:**
```
Escolha: 3 (Mostrar blockchain)
```
✨ Você verá a mesma blockchain do Nó 1!

---

### Terminal 3 - Nó Minerador (Porta 5002)
```bash
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5002"
```

**Conectar à rede:**
```
Escolha: 5 (Conectar a peer)
  Host: localhost
  Porta: 5001
```

**Adicionar transação e minerar:**
```
Escolha: 1 (Criar transação)
  Remetente: Bob
  Destinatário: Charlie
  Valor: 50

Escolha: 2 (Minerar bloco)
```

**Verificar recompensa:**
```
Escolha: 4 (Consultar saldo)
  Endereço: Miner_5002
  
Resultado: 10.0 (recompensa de mineração!)
```

---

## 🎯 Cenário Completo Passo-a-Passo

### Passo 1: Iniciar 3 nós
```bash
# Terminal 1
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5000"

# Terminal 2
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5001"

# Terminal 3
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5002"
```

### Passo 2: Conectar nós em rede
```
Terminal 2 → Opção 5 → localhost:5000
Terminal 3 → Opção 5 → localhost:5001
```

### Passo 3: Criar transações (Terminal 1)
```
Opção 1:
  Alice → Bob: 100
  Bob → Charlie: 50
  Charlie → Alice: 25
```

### Passo 4: Minerar (Terminal 2)
```
Opção 2 (Minerar bloco)
```

### Passo 5: Verificar em todos os terminais
```
Opção 3 (Mostrar blockchain)
Opção 4 (Consultar saldos)
```

**Todos os nós terão a mesma blockchain! 🎉**

---

## 📝 Comandos Úteis

### Testes automatizados
```bash
mvn test
```

### Limpar e recompilar
```bash
mvn clean install
```

### Gerar JAR executável
```bash
mvn package

# Executar JAR
java -jar target/blockchain-java-1.0.0.jar 5000
```

---

## 🔍 Dicas

1. **Mineração demora?** 
   - Com dificuldade "000" (3 zeros), leva alguns segundos
   - Altere em `Protocol.java` para "00" se quiser mais rápido

2. **Nós não se conectam?**
   - Verifique se as portas estão livres
   - Use `localhost` ou `127.0.0.1` como host

3. **Ver peers conectados:**
   - Opção 6 no menu

4. **Validar integridade:**
   - Opção 7 no menu

---

## 🎓 Conceitos Demonstrados

- ✅ **Proof of Work**: Mineração com hash SHA-256
- ✅ **Consenso**: Cadeia mais longa prevalece
- ✅ **Gossip Protocol**: Propagação de transações/blocos
- ✅ **Persistência**: Validação de integridade
- ✅ **P2P**: Rede descentralizada sem servidor central

---

## 🆘 Problemas Comuns

### Erro: "Porta já em uso"
```bash
# Use outra porta
mvn exec:java -Dexec.mainClass="com.blockchain.Main" -Dexec.args="5003"
```

### Erro: "mvn: command not found"
```bash
# Verifique instalação do Maven
mvn --version

# Se não estiver instalado, baixe em: https://maven.apache.org/
```

### Erro ao compilar
```bash
# Verifique versão do Java (precisa ser 17+)
java --version

# Recompile do zero
mvn clean compile
```

---

**Pronto para começar! Execute o modo `--demo` primeiro e depois explore a rede P2P! 🚀**
