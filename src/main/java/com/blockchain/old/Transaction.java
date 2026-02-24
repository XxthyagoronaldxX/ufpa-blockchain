package com.blockchain.old;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma transação na blockchain conforme especificações do trabalho:
 * - identificador único
 * - origem (sender)
 * - destino (recipient)
 * - valor (amount)
 * - timestamp
 */
public class Transaction {
    private String id;          // Identificador único da transação
    private String sender;      // Endereço do remetente (origem)
    private String recipient;   // Endereço do destinatário (destino)
    private double amount;      // Valor da transação
    private long timestamp;     // Timestamp da transação
    
    /**
     * Cria uma nova transação com ID gerado automaticamente
     */
    public Transaction(String sender, String recipient, double amount) {
        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        
        // Validação: somente valores positivos
        if (amount <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }
    }
    
    /**
     * Construtor completo para deserialização
     */
    public Transaction(String id, String sender, String recipient, double amount, long timestamp) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = timestamp;
        
        // Validação: somente valores positivos
        if (amount <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }
    }
    
    /**
     * Converte a transação para Map para serialização JSON
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("sender", sender);
        map.put("recipient", recipient);
        map.put("amount", amount);
        map.put("timestamp", timestamp);
        return map;
    }
    
    /**
     * Cria uma Transaction a partir de um Map (deserialização)
     */
    public static Transaction fromMap(Map<String, Object> map) {
        String id = (String) map.get("id");
        String sender = (String) map.get("sender");
        String recipient = (String) map.get("recipient");
        double amount;
        
        Object amountObj = map.get("amount");
        if (amountObj instanceof Integer) {
            amount = ((Integer) amountObj).doubleValue();
        } else if (amountObj instanceof Double) {
            amount = (Double) amountObj;
        } else {
            amount = Double.parseDouble(amountObj.toString());
        }
        
        long timestamp;
        Object timestampObj = map.get("timestamp");
        if (timestampObj instanceof Integer) {
            timestamp = ((Integer) timestampObj).longValue();
        } else if (timestampObj instanceof Long) {
            timestamp = (Long) timestampObj;
        } else {
            timestamp = Long.parseLong(timestampObj.toString());
        }
        
        return new Transaction(id, sender, recipient, amount, timestamp);
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id='%s', %s -> %s: %.2f}", 
            id.substring(0, 8), sender, recipient, amount);
    }
}
