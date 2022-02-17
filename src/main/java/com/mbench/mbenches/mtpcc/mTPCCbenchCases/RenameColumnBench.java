package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import com.mbench.api.SQLStmt;
import com.mbench.mbenches.mtpcc.mTPCCConstants;

import java.sql.Connection;
import java.util.Random;


public class RenameColumnBench extends VaryBase {
    //rename the amount column to quantity column
    public String stmtInsertOrderLineSQL =
            "INSERT INTO " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " (OL_O_ID, OL_D_ID, OL_W_ID, OL_NUMBER, OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_SIZE, OL_DIST_INFO, OL_DELIVERY_INFO) " +
                    " VALUES (?,?,?,?,?,?,?,?,?,?)";
    public String delivSumOrderAmountSQL =
            "SELECT SUM(OL_SIZE) AS OL_TOTAL " +
                    "  FROM " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " WHERE OL_O_ID = ? " +
                    "   AND OL_D_ID = ? " +
                    "   AND OL_W_ID = ?";
    public String ordStatGetOrderLinesSQL =
            "SELECT OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_SIZE, OL_DELIVERY_D, OL_DIST_INFO " +
                    "  FROM " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " WHERE OL_O_ID = ?" +
                    "   AND OL_D_ID = ?" +
                    "   AND OL_W_ID = ?";

    public RenameColumnBench() {
        super();

    }

    @Override
    public void applyChange() {
        this.stmtMap.put("InsertOrderLine",new SQLStmt(stmtInsertOrderLineSQL));
        this.stmtMap.put("DelivSumOrderAmount",new SQLStmt(delivSumOrderAmountSQL));
        this.stmtMap.put("OrdStatGetOrderLines",new SQLStmt(ordStatGetOrderLinesSQL));
        stage++;
    }


}
