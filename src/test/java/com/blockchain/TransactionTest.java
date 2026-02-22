package com.blockchain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Testes de Transaction conforme especificações do trabalho
 */
class TransactionTest {
    
    @Test
    void testTransactionCreation() {
        Transaction tx = new Transaction("Alice", "Bob", 50.0);
        
        assertNotNull(tx);
        assertNotNull(tx.getId()); // ID único obrigatório
        assertEquals("Alice", tx.getSender());
        assertEquals("Bob", tx.getRecipient());
        assertEquals(50.0, tx.getAmount());
        assertTrue(tx.getTimestamp() > 0);
    }
    
    @Test
    void testUniqueIds() {
        Transaction tx1 = new Transaction("Alice", "Bob", 50.0);
        Transaction tx2 = new Transaction("Alice", "Bob", 50.0);
        
        // IDs devem ser diferentes (UUID)
        assertNotEquals(tx1.getId(), tx2.getId());
    }
    
    @Test
    void testOnlyPositiveValues() {
        // Valor negativo deve ser rejeitado
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction("Alice", "Bob", -10.0);
        });
        
        // Valor zero deve ser rejeitado
        assertThrows(IllegalArgumentException.class, () -> {
            new Transaction("Alice", "Bob", 0.0);
        });
        
        // Valor positivo deve ser aceito
        assertDoesNotThrow(() -> {
            new Transaction("Alice", "Bob", 0.01);
        });
    }
    
    @Test
    void testTransactionSerialization() {
        Transaction original = new Transaction("Alice", "Bob", 50.0);
        
        // Serializa e deserializa
        Transaction deserialized = Transaction.fromMap(original.toMap());
        
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getSender(), deserialized.getSender());
        assertEquals(original.getRecipient(), deserialized.getRecipient());
        assertEquals(original.getAmount(), deserialized.getAmount());
        assertEquals(original.getTimestamp(), deserialized.getTimestamp());
    }
    
    @Test
    void testTransactionEquality() {
        Transaction tx1 = new Transaction("id-123", "Alice", "Bob", 50.0, 1000L);
        Transaction tx2 = new Transaction("id-123", "Alice", "Bob", 50.0, 1000L);
        Transaction tx3 = new Transaction("id-456", "Alice", "Bob", 50.0, 1000L);
        
        // Mesma ID = iguais
        assertEquals(tx1, tx2);
        assertEquals(tx1.hashCode(), tx2.hashCode());
        
        // IDs diferentes = diferentes
        assertNotEquals(tx1, tx3);
    }
}
