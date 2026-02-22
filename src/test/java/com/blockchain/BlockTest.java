package com.blockchain;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BlockTest {
    
    @Test
    void testBlockCreation() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Alice", "Bob", 50.0));
        
        Block block = new Block(0, transactions, "0");
        
        assertNotNull(block);
        assertEquals(0, block.getIndex());
        assertEquals(1, block.getTransactions().size());
        assertEquals("0", block.getPreviousHash());
        assertEquals(0, block.getNonce());
    }
    
    @Test
    void testHashCalculation() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Alice", "Bob", 50.0));
        
        Block block = new Block(0, transactions, "0");
        String hash = block.calculateHash();
        
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 produz 64 caracteres hexadecimais
    }
    
    @Test
    void testMineBlock() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Alice", "Bob", 50.0));
        
        Block block = new Block(0, transactions, "0");
        block.mineBlock("00"); // Dificuldade menor para teste rápido
        
        assertTrue(block.getHash().startsWith("00"));
        assertTrue(block.getNonce() > 0);
    }
    
    @Test
    void testBlockValidation() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Alice", "Bob", 50.0));
        
        Block block = new Block(0, transactions, "0");
        block.mineBlock("00");
        
        assertTrue(block.isValid("00"));
    }
    
    @Test
    void testBlockSerialization() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Alice", "Bob", 50.0));
        
        Block originalBlock = new Block(0, transactions, "0");
        originalBlock.mineBlock("00");
        
        // Serializa para Map e deserializa
        Block deserializedBlock = Block.fromMap(originalBlock.toMap());
        
        assertEquals(originalBlock.getIndex(), deserializedBlock.getIndex());
        assertEquals(originalBlock.getHash(), deserializedBlock.getHash());
        assertEquals(originalBlock.getNonce(), deserializedBlock.getNonce());
        assertEquals(originalBlock.getPreviousHash(), deserializedBlock.getPreviousHash());
    }
}
