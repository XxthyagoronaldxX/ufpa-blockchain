package com.blockchain.pojos;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionPojo {
    private String id; // Identificador único da transação

    private String origem; // Endereço do remetente (origem)

    private String destino; // Endereço do destinatário (destino)

    private double valor; // Valor da transação

    private double timestamp; // Timestamp da transação (segundos, compatível com Python)

    public TransactionPojo(String origem, String destino, double valor) {
        this.id = UUID.randomUUID().toString();
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.timestamp = System.currentTimeMillis() / 1000.0; // Converte ms para segundos

        // Validação: somente valores positivos
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }
    }

    @Override
    public String toString() {
        return String.format("Transaction{id='%s', %s -> %s: %.2f}",
                id.substring(0, 8), origem, destino, valor);
    }
}
