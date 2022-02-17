package com.mbench.mbenches.mtpcc;




public class mTPCCStringBeforeChange {

    public static final String stmtGetCustSQL =
            "SELECT C_DISCOUNT, C_LAST, C_CREDIT" +
                    "  FROM " + mTPCCConstants.TABLENAME_CUSTOMER +
                    " WHERE C_W_ID = ? " +
                    "   AND C_D_ID = ? " +
                    "   AND C_ID = ?";

    public static final String stmtGetWhseSQL =
            "SELECT W_TAX " +
                    "  FROM " + mTPCCConstants.TABLENAME_WAREHOUSE +
                    " WHERE W_ID = ?";

    public static final String stmtGetDistSQL =
            "SELECT D_NEXT_O_ID, D_TAX " +
                    "  FROM " + mTPCCConstants.TABLENAME_DISTRICT +
                    " WHERE D_W_ID = ? AND D_ID = ? FOR UPDATE";

    public static final String stmtInsertNewOrderSQL =
            "INSERT INTO " + mTPCCConstants.TABLENAME_NEWORDER +
                    " (NO_O_ID, NO_D_ID, NO_W_ID) " +
                    " VALUES ( ?, ?, ?)";

    public static final String stmtUpdateDistSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_DISTRICT +
                    "   SET D_NEXT_O_ID = D_NEXT_O_ID + 1 " +
                    " WHERE D_W_ID = ? " +
                    "   AND D_ID = ?";

    public static final String stmtInsertOOrderSQL =
            "INSERT INTO " + mTPCCConstants.TABLENAME_OPENORDER +
                    " (O_ID, O_D_ID, O_W_ID, O_C_ID, O_ENTRY_D, O_OL_CNT, O_ALL_LOCAL)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String stmtGetItemSQL =
            "SELECT I_PRICE, I_NAME , I_DATA " +
                    "  FROM " + mTPCCConstants.TABLENAME_ITEM +
                    " WHERE I_ID = ?";

    public static final String stmtGetStockSQL =
            "SELECT S_QUANTITY, S_DATA, S_DIST_01, S_DIST_02, S_DIST_03, S_DIST_04, S_DIST_05, " +
                    "       S_DIST_06, S_DIST_07, S_DIST_08, S_DIST_09, S_DIST_10" +
                    "  FROM " + mTPCCConstants.TABLENAME_STOCK +
                    " WHERE S_I_ID = ? " +
                    "   AND S_W_ID = ? FOR UPDATE";

    public static final String stmtUpdateStockSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_STOCK +
                    "   SET S_QUANTITY = ? , " +
                    "       S_YTD = S_YTD + ?, " +
                    "       S_ORDER_CNT = S_ORDER_CNT + 1, " +
                    "       S_REMOTE_CNT = S_REMOTE_CNT + ? " +
                    " WHERE S_I_ID = ? " +
                    "   AND S_W_ID = ?";

    public static final String stmtInsertOrderLineSQL =
            "INSERT INTO " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " (OL_O_ID, OL_D_ID, OL_W_ID, OL_NUMBER, OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DIST_INFO, OL_DELIVERY_INFO) " +
                    " VALUES (?,?,?,?,?,?,?,?,?,?)";





    public static String delivGetOrderIdSQL =
            "SELECT NO_O_ID FROM " + mTPCCConstants.TABLENAME_NEWORDER +
                    " WHERE NO_D_ID = ? " +
                    "   AND NO_W_ID = ? " +
                    " ORDER BY NO_O_ID ASC " +
                    " LIMIT 1";

    public static String delivDeleteNewOrderSQL =
            "DELETE FROM " + mTPCCConstants.TABLENAME_NEWORDER +
                    " WHERE NO_O_ID = ? " +
                    "   AND NO_D_ID = ?" +
                    "   AND NO_W_ID = ?";

    public static String delivGetCustIdSQL =
            "SELECT O_C_ID FROM " + mTPCCConstants.TABLENAME_OPENORDER +
                    " WHERE O_ID = ? " +
                    "   AND O_D_ID = ? " +
                    "   AND O_W_ID = ?";

    public static String delivUpdateCarrierIdSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_OPENORDER +
                    "   SET O_CARRIER_ID = ? " +
                    " WHERE O_ID = ? " +
                    "   AND O_D_ID = ?" +
                    "   AND O_W_ID = ?";

    public static String delivUpdateDeliveryDateSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_ORDERLINE +
                    "   SET OL_DELIVERY_D = ? " +
                    " WHERE OL_O_ID = ? " +
                    "   AND OL_D_ID = ? " +
                    "   AND OL_W_ID = ? ";

    public static String delivSumOrderAmountSQL =
            "SELECT SUM(OL_AMOUNT) AS OL_TOTAL " +
                    "  FROM " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " WHERE OL_O_ID = ? " +
                    "   AND OL_D_ID = ? " +
                    "   AND OL_W_ID = ?";

    public static String delivUpdateCustBalDelivCntSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_CUSTOMER +
                    "   SET C_BALANCE = C_BALANCE + ?," +
                    "       C_DELIVERY_CNT = C_DELIVERY_CNT + 1 " +
                    " WHERE C_W_ID = ? " +
                    "   AND C_D_ID = ? " +
                    "   AND C_ID = ? ";


    public static String ordStatGetNewestOrdSQL =
            "SELECT O_ID, O_CARRIER_ID, O_ENTRY_D " +
                    "  FROM " + mTPCCConstants.TABLENAME_OPENORDER +
                    " WHERE O_W_ID = ? " +
                    "   AND O_D_ID = ? " +
                    "   AND O_C_ID = ? " +
                    " ORDER BY O_ID DESC LIMIT 1";

    public static String ordStatGetOrderLinesSQL =
            "SELECT OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DELIVERY_D, OL_DIST_INFO " +
                    "  FROM " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " WHERE OL_O_ID = ?" +
                    "   AND OL_D_ID = ?" +
                    "   AND OL_W_ID = ?";

    public static String payGetCustSQL =
            "SELECT C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, C_STREET_2, " +
                    "       C_CITY, C_STATE, C_ZIP, C_PHONE, C_CREDIT, C_CREDIT_LIM, " +
                    "       C_DISCOUNT, C_BALANCE, C_YTD_PAYMENT, C_PAYMENT_CNT, C_SINCE " +
                    "  FROM " + mTPCCConstants.TABLENAME_CUSTOMER +
                    " WHERE C_W_ID = ? " +
                    "   AND C_D_ID = ? " +
                    "   AND C_ID = ?";

    public static String customerByNameSQL =
            "SELECT C_FIRST, C_MIDDLE, C_ID, C_STREET_1, C_STREET_2, C_CITY, " +
                    "       C_STATE, C_ZIP, C_PHONE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, " +
                    "       C_BALANCE, C_YTD_PAYMENT, C_PAYMENT_CNT, C_SINCE " +
                    "  FROM " + mTPCCConstants.TABLENAME_CUSTOMER +
                    " WHERE C_W_ID = ? " +
                    "   AND C_D_ID = ? " +
                    "   AND C_LAST = ? " +
                    " ORDER BY C_FIRST";





    public static String payUpdateWhseSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_WAREHOUSE +
                    "   SET W_YTD = W_YTD + ? " +
                    " WHERE W_ID = ? ";

    public static String payGetWhseSQL =
            "SELECT W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP, W_NAME" +
                    "  FROM " + mTPCCConstants.TABLENAME_WAREHOUSE +
                    " WHERE W_ID = ?";

    public static String payUpdateDistSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_DISTRICT +
                    "   SET D_YTD = D_YTD + ? " +
                    " WHERE D_W_ID = ? " +
                    "   AND D_ID = ?";

    public static String payGetDistSQL =
            "SELECT D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, D_NAME" +
                    "  FROM " + mTPCCConstants.TABLENAME_DISTRICT +
                    " WHERE D_W_ID = ? " +
                    "   AND D_ID = ?";



    public static String payGetCustCdataSQL =
            "SELECT C_DATA " +
                    "  FROM " + mTPCCConstants.TABLENAME_CUSTOMER +
                    " WHERE C_W_ID = ? " +
                    "   AND C_D_ID = ? " +
                    "   AND C_ID = ?";

    public static String payUpdateCustBalCdataSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_CUSTOMER +
                    "   SET C_BALANCE = ?, " +
                    "       C_YTD_PAYMENT = ?, " +
                    "       C_PAYMENT_CNT = ?, " +
                    "       C_DATA = ? " +
                    " WHERE C_W_ID = ? " +
                    "   AND C_D_ID = ? " +
                    "   AND C_ID = ?";

    public static String payUpdateCustBalSQL =
            "UPDATE " + mTPCCConstants.TABLENAME_CUSTOMER +
                    "   SET C_BALANCE = ?, " +
                    "       C_YTD_PAYMENT = ?, " +
                    "       C_PAYMENT_CNT = ? " +
                    " WHERE C_W_ID = ? " +
                    "   AND C_D_ID = ? " +
                    "   AND C_ID = ?";

    public static String payInsertHistSQL =
            "INSERT INTO " + mTPCCConstants.TABLENAME_HISTORY +
                    " (H_C_D_ID, H_C_W_ID, H_C_ID, H_D_ID, H_W_ID, H_DATE, H_AMOUNT, H_DATA) " +
                    " VALUES (?,?,?,?,?,?,?,?)";

    public static String payGetHistSQL =
            "SELECT H_C_D_ID, H_C_W_ID, H_C_ID, H_D_ID, H_W_ID, H_DATE, H_AMOUNT, H_DATA" +
                    " FROM " + mTPCCConstants.TABLENAME_HISTORY +
                    " (H_C_D_ID, H_C_W_ID, H_C_ID, H_D_ID, H_W_ID, H_DATE, H_AMOUNT, H_DATA) " +
                    " VALUES (?,?,?,?,?,?,?,?)";










    public static String stockGetDistOrderIdSQL =
            "SELECT D_NEXT_O_ID " +
                    "  FROM " + mTPCCConstants.TABLENAME_DISTRICT +
                    " WHERE D_W_ID = ? " +
                    "   AND D_ID = ?";

    public static String stockGetCountStockSQL =
            "SELECT COUNT(DISTINCT (S_I_ID)) AS STOCK_COUNT " +
                    " FROM " + mTPCCConstants.TABLENAME_ORDERLINE + ", " + mTPCCConstants.TABLENAME_STOCK +
                    " WHERE OL_W_ID = ?" +
                    " AND OL_D_ID = ?" +
                    " AND OL_O_ID < ?" +
                    " AND OL_O_ID >= ?" +
                    " AND S_W_ID = ?" +
                    " AND S_I_ID = OL_I_ID" +
                    " AND S_QUANTITY < ?";

    public static String warehouseGetWarehouseInfoByID =
            "SELECT W_NAME " +
                    " FROM " + mTPCCConstants.TABLENAME_WAREHOUSE +
                    " WHERE W_ID = ?";
    public static String warehouseGetWarehouseInfoByName =
            "SELECT W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP, W_NAME " +
                    " FROM " + mTPCCConstants.TABLENAME_WAREHOUSE +
                    " WHERE W_NAME = ?";
    public  static String warehouseUpdateNameByID =
            "UPDATE " + mTPCCConstants.TABLENAME_WAREHOUSE +
                    "   SET W_NAME = ? " +
                    " WHERE W_ID = ? ";
}
