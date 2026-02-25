package com.blockchain.factories;

import java.util.ArrayList;

import com.blockchain.pojos.BlockPojo;

public class BlockFactory {
    private BlockFactory() {
    }

    public static BlockPojo createGenesisBlock() {
        return BlockPojo.builder()
                .index(0)
                .timestamp(0)
                .transactions(new ArrayList<>())
                .previousHash("0000000000000000000000000000000000000000000000000000000000000000")
                .nonce(0)
                .hash("0567c32b97c36a70d3f4cb865710d329a0be5d713c8cb1b8c769fbaf89f1afb7")
                .build();
    }
}
