package com.mbench.mbenches.mtpcc.mTPCCbenchCases;



import com.mbench.api.SQLStmt;
import com.mbench.mbenches.mtpcc.mTPCCConstants;
import com.mbench.util.VaryPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class DeleteColumnBench extends VaryBase{
    public String changedInsertOrderLineSQL =
            "INSERT INTO " + mTPCCConstants.TABLENAME_ORDERLINE +
                    " (OL_O_ID, OL_D_ID, OL_W_ID, OL_NUMBER, OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DIST_INFO) " +
                    " VALUES (?,?,?,?,?,?,?,?,?)";

    public DeleteColumnBench() {
        super();
    }

    @Override
    public void applyChange() {
        this.stmtMap.put("InsertOrderLine",new SQLStmt(changedInsertOrderLineSQL));
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
        PStmt.setString(index++,dist_info);
        if (stage==1)
            PStmt.setString(index++,delivery_info);
    }
}
