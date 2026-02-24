package com.blockchain.pojos;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TransactionPojo {
    private String id; // Identificador único da transação

    private String sender; // Endereço do remetente (origem)

    private String recipient; // Endereço do destinatário (destino)

    private double amount; // Valor da transação

    private long timestamp; // Timestamp da transação

    public TransactionPojo(String sender, String recipient, double amount) {
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

    @Override
    public String toString() {
        return String.format("Transaction{id='%s', %s -> %s: %.2f}",
                id.substring(0, 8), sender, recipient, amount);
    }
}
