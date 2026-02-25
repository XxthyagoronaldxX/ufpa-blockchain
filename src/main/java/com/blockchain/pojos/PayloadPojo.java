package com.blockchain.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadPojo {
    private BlockchainPojo blockchain;

    private BlockPojo block;

    private TransactionPojo transaction;
}
