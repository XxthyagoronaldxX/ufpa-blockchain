package com.blockchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Testes da Blockchain conforme especificações do trabalho
 */
class BlockchainTest {
    
    @Test
    void testBlockchainCreation() {
        Blockchain blockchain = new Blockchain();
        
        assertNotNull(blockchain);
        assertEquals(1, blockchain.getChainLength()); // Deve ter o bloco gênesis
        assertTrue(blockchain.isChainValid());
    }
    
    @Test
    void testAddTransactionWithSufficientBalance() {
        Blockchain blockchain = new Blockchain();
        
        // Cria funding inicial
        blockchain.fundAddress("Alice", 100.0);
        
        // Adiciona transação válida
        Transaction tx = new Transaction("Alice", "Bob", 50.0);
        blockchain.addTransaction(tx);
        
        assertEquals(1, blockchain.getPendingTransactions().size());
    }
    
    @Test
    void testAddTransactionWithInsufficientBalance() {
        Blockchain blockchain = new Blockchain();
        
        // Alice não tem saldo
        Transaction tx = new Transaction("Alice", "Bob", 50.0);
        
        // Deve lançar exceção por saldo insuficiente
        assertThrows(IllegalArgumentException.class, () -> {
            blockchain.addTransaction(tx);
        });
    }
    
    @Test
    void testNoNegativeBalance() {
        Blockchain blockchain = new Blockchain();
        blockchain.fundAddress("Alice", 100.0);
        
        // Tenta enviar mais do que tem
        Transaction invalidTx = new Transaction("Alice", "Bob", 150.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            blockchain.addTransaction(invalidTx);
        });
    }
    
    @Test
    void testOnlyPositiveValues() {
        // Tenta criar transação com valor negativo
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction("Alice", "Bob", -10.0);
        });
        
        // Tenta criar transação com valor zero
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction("Alice", "Bob", 0.0);
        });
    }
    
    @Test
    void testMinePendingTransactions() {
        Blockchain blockchain = new Blockchain();
        blockchain.fundAddress("Alice", 100.0);
        blockchain.addTransaction(new Transaction("Alice", "Bob", 50.0));
        
        blockchain.minePendingTransactions();
        
        // Deve ter: gênesis + funding + transações
        assertEquals(3, blockchain.getChainLength());
        assertEquals(0, blockchain.getPendingTransactions().size());
    }
    
    @Test
    void testChainValidation() {
        Blockchain blockchain = new Blockchain();
        blockchain.fundAddress("Alice", 100.0);
        blockchain.addTransaction(new Transaction("Alice", "Bob", 50.0));
        blockchain.minePendingTransactions();
        
        assertTrue(blockchain.isChainValid());
    }
    
    @Test
    void testGetBalance() {
        Blockchain blockchain = new Blockchain();
        
        // Funding inicial
        blockchain.fundAddress("Alice", 100.0);
        blockchain.fundAddress("Bob", 100.0);
        
        // Transações
        blockchain.addTransaction(new Transaction("Alice", "Bob", 50.0));
        blockchain.minePendingTransactions();
        
        blockchain.addTransaction(new Transaction("Bob", "Charlie", 25.0));
        blockchain.minePendingTransactions();
        
        // Alice: 100 - 50 = 50
        assertEquals(50.0, blockchain.getBalance("Alice"));
        // Bob: 100 + 50 - 25 = 125
        assertEquals(125.0, blockchain.getBalance("Bob"));
        // Charlie: 0 + 25 = 25
        assertEquals(25.0, blockchain.getBalance("Charlie"));
    }
    
    @Test
    void testPendingTransactionsValidation() {
        Blockchain blockchain = new Blockchain();
        blockchain.fundAddress("Alice", 100.0);
        
        // Adiciona transação pendente
        blockchain.addTransaction(new Transaction("Alice", "Bob", 60.0));
        
        // Tenta adicionar outra que excederia o saldo
        Transaction tx2 = new Transaction("Alice", "Charlie", 50.0);
        
        // Deve falhar pois Alice só tem 40 disponível (100 - 60)
        assertThrows(IllegalArgumentException.class, () -> {
            blockchain.addTransaction(tx2);
        });
    }
    
    @Test
    void testGenesisBlock() {
        Blockchain blockchain = new Blockchain();
        
        Block genesis = blockchain.getChain().get(0);
        
        assertEquals(0, genesis.getIndex());
        assertEquals("0", genesis.getPreviousHash());
        assertTrue(genesis.getHash().startsWith(Protocol.DIFFICULTY));
    }
}
