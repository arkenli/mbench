package com.mbench.mbenches.mtpcc;

import java.text.SimpleDateFormat;

public class mTPCCConfig {
    public enum TransactionType {
        INVALID, // Exists so the order is the same as the constants below
        NEW_ORDER, PAYMENT, ORDER_STATUS, DELIVERY, STOCK_LEVEL
    }

    public final static String[] nameTokens = {"BAR", "OUGHT", "ABLE", "PRI",
            "PRES", "ESE", "ANTI", "CALLY", "ATION", "EING"};

    public final static String terminalPrefix = "Term-";
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static int configWhseCount = 1;
    public final static int configItemCount = 100000; // tpc-c std = 100,000
    public final static int configDistPerWhse = 10; // tpc-c std = 10
    public final static int configCustPerDist = 3000; // tpc-c std = 3,000
    public static final int INVALID_ITEM_ID = -12345;
}
