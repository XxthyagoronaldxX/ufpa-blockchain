package com.blockchain.factories;

import java.util.Map;

import com.blockchain.pojos.TransactionPojo;

public class TransactionFactory {
    private TransactionFactory() {
    }

    public static TransactionPojo fromMap(Map<String, Object> map) {
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

        return new TransactionPojo(id, sender, recipient, amount, timestamp);
    }
}
