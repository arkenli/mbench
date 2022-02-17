package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class ChangeColTypeBench extends VaryBase{
    public ChangeColTypeBench() {
        super();

    }

    @Override
    public void applyChange() {
        stage++;
    }
    //number:int->bigint

    @Override
    public void setInsertOrderLineValue(PreparedStatement PStmt,int o_id, int d_id, int w_id, int number, int i_id, int supply_w_id, int quantity, double amount, String dist_info, String delivery_info) throws SQLException {
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        if (stage==1) {
            PStmt.setInt(index++,number);
        }
        else if (stage==2){
            PStmt.setBigDecimal(index++, BigDecimal.valueOf(number));
        }
        PStmt.setInt(index++,i_id);
        PStmt.setInt(index++,supply_w_id);
        PStmt.setInt(index++,quantity);
        PStmt.setDouble(index++,amount);
        PStmt.setString(index++,dist_info);
        PStmt.setString(index++,delivery_info);

    }


}
