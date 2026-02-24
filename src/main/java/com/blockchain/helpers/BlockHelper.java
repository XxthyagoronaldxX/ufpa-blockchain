package com.blockchain.helpers;

import com.blockchain.pojos.BlockPojo;
import com.blockchain.utils.CryptoUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlockHelper {
    private BlockHelper() {
    }

    public static String calculateHash(BlockPojo block) {
        String data = block.getIndex() + block.getTimestamp() + block.getTransactionsToString()
                + block.getPreviousHash() + block.getNonce();

        return CryptoUtil.applySHA256(data);
    }

    public static void mineBlock(BlockPojo block, String difficulty) {
        String target = new String(new char[difficulty.length()]).replace('\0', '0');

        // Calculate initial hash before checking
        String hash = calculateHash(block);

        while (!hash.substring(0, difficulty.length()).equals(target)) {
            block.setNonce(block.getNonce() + 1);
            hash = calculateHash(block);
        }

        block.setHash(hash);
        log.debug("Bloco minerado: {}", hash);
    }
}
