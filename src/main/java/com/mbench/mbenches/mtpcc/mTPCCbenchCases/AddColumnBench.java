package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import com.mbench.api.SQLStmt;
import com.mbench.mbenches.mtpcc.mTPCCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddColumnBench extends VaryBase {
    private static final Logger LOG = LoggerFactory.getLogger(AddColumnBench.class);
    public final String changedInsertOrderLineSQL =
            "INSERT INTO " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " (OL_O_ID, OL_D_ID, OL_W_ID, OL_NUMBER, OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_TAX, OL_DIST_INFO, OL_DELIVERY_INFO) " +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    //    SQLStmt changedDelivUpdateDeliveryDateSQL = new SQLStmt(
//            "UPDATE " + mTPCCConstants.TABLENAME_ORDERLINE +
//                    "   SET OL_DELIVERY_D = ? " +
//                    " WHERE OL_O_ID = ? " +
//                    "   AND OL_D_ID = ? " +
//                    "   AND OL_W_ID = ? ");
//    SQLStmt changedDelivSumOrderAmountSQL = new SQLStmt(
//            "SELECT SUM(OL_AMOUNT) AS OL_TOTAL " +
//                    "  FROM " + mTPCCConstants.TABLENAME_ORDERLINE +
//                    " WHERE OL_O_ID = ? " +
//                    "   AND OL_D_ID = ? " +
//                    "   AND OL_W_ID = ?");
    public final String changedOrdStatGetOrderLinesSQL =
            "SELECT OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DELIVERY_D, OL_DIST_INFO, OL_TAX " +
                    "  FROM " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " WHERE OL_O_ID = ?" +
                    "   AND OL_D_ID = ?" +
                    "   AND OL_W_ID = ?";

    public AddColumnBench(){
        super();
    }

    @Override
    public void applyChange() {

        this.stmtMap.put("InsertOrderLine",new SQLStmt(changedInsertOrderLineSQL));
        this.stmtMap.put("OrdStatGetOrderLines",new SQLStmt(changedOrdStatGetOrderLinesSQL));
        stage++;
    }


    @Override
    public void setInsertOrderLineValue(PreparedStatement PStmt, int o_id, int d_id, int w_id, int number, int i_id, int supply_w_id, int quantity, double amount, String dist_info, String delivery_info) throws SQLException {
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,number);
        PStmt.setInt(index++,i_id);
        PStmt.setInt(index++,supply_w_id);
        PStmt.setInt(index++,quantity);
        PStmt.setDouble(index++,amount);
        if (stage==2)
            PStmt.setFloat(index++,getWeight());
        PStmt.setString(index++,dist_info);
        PStmt.setString(index++,delivery_info);
    }

    @Override
    public void setOrdStatGetOrderLinesValue(PreparedStatement PStmt,int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
    }

}



