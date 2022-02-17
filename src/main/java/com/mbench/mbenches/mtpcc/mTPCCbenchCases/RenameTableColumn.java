package com.mbench.mbenches.mtpcc.mTPCCbenchCases;


import com.mbench.api.SQLStmt;
import com.mbench.mbenches.mtpcc.mTPCCConstants;

import java.sql.Connection;
import java.util.Random;

public class RenameTableColumn extends VaryBase{
    public static final String newTabName="ORDER_ITEMS";
    public final String stmtInsertOrderLineSQL =
            "INSERT INTO " + newTabName +
                    " (OL_O_ID, OL_D_ID, OL_W_ID, OL_NUMBER, OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DIST_INFO, OL_DELIVERY_INFO) " +
                    " VALUES (?,?,?,?,?,?,?,?,?,?)";
    public String delivUpdateDeliveryDateSQL =
            "UPDATE " + newTabName +
                    "   SET OL_DELIVERY_D = ? " +
                    " WHERE OL_O_ID = ? " +
                    "   AND OL_D_ID = ? " +
                    "   AND OL_W_ID = ? ";
    public String delivSumOrderAmountSQL =
            "SELECT SUM(OL_AMOUNT) AS OL_TOTAL " +
                    "  FROM " + newTabName +
                    " WHERE OL_O_ID = ? " +
                    "   AND OL_D_ID = ? " +
                    "   AND OL_W_ID = ?";
    public String ordStatGetOrderLinesSQL =
            "SELECT OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DELIVERY_D, OL_DIST_INFO " +
                    "  FROM " + newTabName +
                    " WHERE OL_O_ID = ?" +
                    "   AND OL_D_ID = ?" +
                    "   AND OL_W_ID = ?";
    public String stockGetCountStockSQL =
            "SELECT COUNT(DISTINCT (S_I_ID)) AS STOCK_COUNT " +
                    " FROM " + newTabName + ", " + mTPCCConstants.TABLENAME_STOCK +
                    " WHERE OL_W_ID = ?" +
                    " AND OL_D_ID = ?" +
                    " AND OL_O_ID < ?" +
                    " AND OL_O_ID >= ?" +
                    " AND S_W_ID = ?" +
                    " AND S_I_ID = OL_I_ID" +
                    " AND S_QUANTITY < ?";

    public RenameTableColumn() {
        super();

    }

    @Override
    public void applyChange() {
        this.stmtMap.put("InsertOrderLine",new SQLStmt(stmtInsertOrderLineSQL));
        this.stmtMap.put("delivUpdateDeliveryDate",new SQLStmt(delivUpdateDeliveryDateSQL));
        this.stmtMap.put("delivSumOrderAmount",new SQLStmt(delivSumOrderAmountSQL));
        this.stmtMap.put("ordStatGetOrderLines",new SQLStmt(ordStatGetOrderLinesSQL));
        this.stmtMap.put("stockGetCountStock",new SQLStmt(stockGetCountStockSQL));
        stage++;
    }


}
