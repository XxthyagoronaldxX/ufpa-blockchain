package com.blockchain.pojos;

import lombok.Data;

@Data
public class PayloadPojo {
    private BlockchainPojo blockchain;

    private BlockPojo block;
}
