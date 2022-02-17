package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import com.mbench.api.SQLStmt;
import com.mbench.jdbc.AutoIncrementPreparedStatement;

import com.mbench.mbenches.mtpcc.mTPCCStringBeforeChange;
import com.mbench.types.DatabaseType;
import java.sql.PreparedStatement;


import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class VaryBase {
    public Map<String, SQLStmt> stmtMap=new HashMap<>();
    public Random gen=new Random();
    public int stage=1;
    public VaryBase(){
        initialStmtMap();
    }
    public void initialStmtMap(){
        stmtMap.put("GetCust", new SQLStmt(mTPCCStringBeforeChange.stmtGetCustSQL));
        stmtMap.put("GetWhse", new SQLStmt(mTPCCStringBeforeChange.stmtGetWhseSQL));
        stmtMap.put("GetDist", new SQLStmt(mTPCCStringBeforeChange.stmtGetDistSQL));
        stmtMap.put("InsertNewOrder", new SQLStmt(mTPCCStringBeforeChange.stmtInsertNewOrderSQL));
        stmtMap.put("UpdateDist", new SQLStmt(mTPCCStringBeforeChange.stmtUpdateDistSQL));
        stmtMap.put("InsertOOrder", new SQLStmt(mTPCCStringBeforeChange.stmtInsertOOrderSQL));
        stmtMap.put("GetItem", new SQLStmt(mTPCCStringBeforeChange.stmtGetItemSQL));
        stmtMap.put("GetStock", new SQLStmt(mTPCCStringBeforeChange.stmtGetStockSQL));
        stmtMap.put("UpdateStock", new SQLStmt(mTPCCStringBeforeChange.stmtUpdateStockSQL));
        stmtMap.put("InsertOrderLine", new SQLStmt(mTPCCStringBeforeChange.stmtInsertOrderLineSQL));
        stmtMap.put("DelivDeleteNewOrder", new SQLStmt(mTPCCStringBeforeChange.delivDeleteNewOrderSQL));
        stmtMap.put("DelivGetCustId", new SQLStmt(mTPCCStringBeforeChange.delivGetCustIdSQL));
        stmtMap.put("DelivUpdateCarrierId", new SQLStmt(mTPCCStringBeforeChange.delivUpdateCarrierIdSQL));
        stmtMap.put("DelivUpdateDeliveryDate", new SQLStmt(mTPCCStringBeforeChange.delivUpdateDeliveryDateSQL));
        stmtMap.put("DelivSumOrderAmount", new SQLStmt(mTPCCStringBeforeChange.delivSumOrderAmountSQL));
        stmtMap.put("DelivGetOrderId", new SQLStmt(mTPCCStringBeforeChange.delivGetOrderIdSQL));
        stmtMap.put("DelivUpdateCustBalDelivCnt", new SQLStmt(mTPCCStringBeforeChange.delivUpdateCustBalDelivCntSQL));
        stmtMap.put("OrdStatGetNewestOrd", new SQLStmt(mTPCCStringBeforeChange.ordStatGetNewestOrdSQL));
        stmtMap.put("OrdStatGetOrderLines", new SQLStmt(mTPCCStringBeforeChange.ordStatGetOrderLinesSQL));
        stmtMap.put("PayGetCust", new SQLStmt(mTPCCStringBeforeChange.payGetCustSQL));
        stmtMap.put("CustomerByName", new SQLStmt(mTPCCStringBeforeChange.customerByNameSQL));
        stmtMap.put("PayUpdateWhse", new SQLStmt(mTPCCStringBeforeChange.payUpdateWhseSQL));
        stmtMap.put("PayGetWhse", new SQLStmt(mTPCCStringBeforeChange.payGetWhseSQL));
        stmtMap.put("PayUpdateDist", new SQLStmt(mTPCCStringBeforeChange.payUpdateDistSQL));
        stmtMap.put("PayGetDist", new SQLStmt(mTPCCStringBeforeChange.payGetDistSQL));
        stmtMap.put("PayGetCustCdata", new SQLStmt(mTPCCStringBeforeChange.payGetCustCdataSQL));
        stmtMap.put("PayUpdateCustBalCdata", new SQLStmt(mTPCCStringBeforeChange.payUpdateCustBalCdataSQL));
        stmtMap.put("PayUpdateCustBal", new SQLStmt(mTPCCStringBeforeChange.payUpdateCustBalSQL));
        stmtMap.put("PayInsertHist", new SQLStmt(mTPCCStringBeforeChange.payInsertHistSQL));
        stmtMap.put("StockGetDistOrderId", new SQLStmt(mTPCCStringBeforeChange.stockGetDistOrderIdSQL));
        stmtMap.put("StockGetCountStock", new SQLStmt(mTPCCStringBeforeChange.stockGetCountStockSQL));
        stmtMap.put("WarehouseGetWarehouseInfoByID", new SQLStmt(mTPCCStringBeforeChange.warehouseGetWarehouseInfoByID));
        stmtMap.put("WarehouseGetWarehouseInfoByName", new SQLStmt(mTPCCStringBeforeChange.warehouseGetWarehouseInfoByName));
        stmtMap.put("WarehouseUpdateNameByID", new SQLStmt(mTPCCStringBeforeChange.warehouseUpdateNameByID));
    }
    public abstract void applyChange();

    public SQLStmt getSQLStmt(String key) throws SQLException {
        return this.stmtMap.get(key);
    }

    public void setGetCustValue(PreparedStatement PStmt,int w_id,int d_id,int c_id) throws SQLException {
        int index=1;
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,c_id);
        ;
    }
    public void setGetWhseValue(PreparedStatement PStmt, int w_id) throws SQLException {
        int index=1;
        PStmt.setInt(1,w_id);
        ;
    }
    public void setGetDistValue(PreparedStatement PStmt,int w_id,int d_id) throws SQLException {
        int index=1;
        PStmt.setInt(1,w_id);
        PStmt.setInt(2,d_id);
        ;
    }
    public void  setInsertNewOrderValue(PreparedStatement PStmt,int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        ;

    }
    public void  setUpdateDistValue(PreparedStatement PStmt,int w_id, int d_id) throws SQLException {
        int index=1;
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        ;

    }
    public void  setInsertOOrderValue(PreparedStatement PStmt,int o_id, int d_id, int w_id, int c_id, Timestamp entry_d, int ol_cnt, int all_local) throws SQLException{
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,c_id);
        PStmt.setTimestamp(index++,entry_d);
        PStmt.setInt(index++,ol_cnt);
        PStmt.setInt(index++,all_local);
        ;
    }
    public void setGetItemValue(PreparedStatement PStmt,int i_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,i_id);
        ;
    }
    public void setGetStockValue(PreparedStatement PStmt,int i_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,i_id);
        PStmt.setInt(index++,w_id);
        ;
    }
    public void setUpdateStockValue(PreparedStatement PStmt,int s_quantity, int ol_quantity, int s_remote_count_increment, int i_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,s_quantity);
        PStmt.setInt(index++,ol_quantity);
        PStmt.setInt(index++,s_remote_count_increment);
        PStmt.setInt(index++,i_id);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setInsertOrderLineValue(PreparedStatement PStmt,int o_id, int d_id, int w_id, int number, int i_id, int supply_w_id, int quantity, double amount, String dist_info,String delivery_info) throws SQLException {
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
        PStmt.setString(index++,delivery_info);
        ;
    }

    public void setDeleteNewOrderValue(PreparedStatement PStmt,int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
    }

    public void setDelivGetCustIdValue(PreparedStatement PStmt,int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setDelivUpdateCarrierValue(PreparedStatement PStmt,int carrier_id, int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,carrier_id);
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setDelivUpdateDeleiveryDatValue(PreparedStatement PStmt,Timestamp timestamp, int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setTimestamp(index++,timestamp);
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setDelivSumOrderAmountValue(PreparedStatement PStmt,int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setDelivGetOrderIdValue(PreparedStatement PStmt,int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setDelivUpdateCustBalDelivCntValue(PreparedStatement PStmt,BigDecimal total, int w_id, int d_id, int c_id) throws SQLException{
        int index=1;
        PStmt.setBigDecimal(index++,total);
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,c_id);
        ;
    }

    public void setOrdStatGetNewestOrdValue(PreparedStatement PStmt,int w_id, int d_id, int c_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,c_id);
        ;
    }

    public void setOrdStatGetOrderLinesValue(PreparedStatement PStmt,int o_id, int d_id, int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setPayGetCustValue(PreparedStatement PStmt,int c_w_id, int c_d_id, int c_id) throws SQLException {
        int index=1;
        PStmt.setInt(index++,c_w_id);
        PStmt.setInt(index++,c_d_id);
        PStmt.setInt(index++,c_id);
        ;
    }

    public void setCustomerByNameValue(PreparedStatement PStmt,int c_w_id, int c_d_id, String customerLastName) throws SQLException {
        int index=1;
        PStmt.setInt(index++,c_w_id);
        PStmt.setInt(index++,c_d_id);
        PStmt.setString(index++,customerLastName);
        ;
    }

    public void setPayUpdateWhseValue(PreparedStatement PStmt,BigDecimal amount, int w_id) throws SQLException{
        int index=1;
        PStmt.setBigDecimal(index++,amount);
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setPayGetWhseValue(PreparedStatement PStmt,int w_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setPayUpdateDistValue(PreparedStatement PStmt,BigDecimal amount, int w_id, int d_id) throws SQLException{
        int index=1;
        PStmt.setBigDecimal(index++,amount);
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        ;
    }

    public void setPayGetDistValue(PreparedStatement PStmt,int w_id, int d_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        ;
    }

    public void setPayGetCustCdataValue(PreparedStatement PStmt,int c_w_id, int c_d_id, int c_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,c_w_id);
        PStmt.setInt(index++,c_d_id);
        PStmt.setInt(index++,c_id);
        ;
    }

    public void setPayUpdateCustBalCdataValue(PreparedStatement PStmt,double balance, double ytd_payment, int payment_cnt, String data, int c_w_id, int c_d_id, int c_id) throws SQLException{
        int index=1;
        PStmt.setDouble(index++,balance);
        PStmt.setDouble(index++,ytd_payment);
        PStmt.setInt(index++,payment_cnt);
        PStmt.setString(index++,data);
        PStmt.setInt(index++,c_w_id);
        PStmt.setInt(index++,c_d_id);
        PStmt.setInt(index++,c_id);
        ;
    }

    public void setPayUpdateCustBalValue(PreparedStatement PStmt,double balance, double ytd_payment, int payment_cnt, int c_w_id, int c_d_id, int c_id) throws SQLException{
        int index=1;
        PStmt.setDouble(index++,balance);
        PStmt.setDouble(index++,ytd_payment);
        PStmt.setInt(index++,payment_cnt);
        PStmt.setInt(index++,c_w_id);
        PStmt.setInt(index++,c_d_id);
        PStmt.setInt(index++,c_id);
        ;
    }

    public void setPayInsertHistValue(PreparedStatement PStmt,int c_d_id, int c_w_id, int c_id, int d_id, int w_id, Timestamp timestamp, double paymentAmount, String h_data) throws SQLException{
        int index=1;
        PStmt.setInt(index++,c_d_id);
        PStmt.setInt(index++,c_w_id);
        PStmt.setInt(index++,c_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,w_id);
        PStmt.setTimestamp(index++,timestamp);
        PStmt.setDouble(index++,paymentAmount);
        PStmt.setString(index++,h_data);
        ;
    }

    public void setStockGetDistOrderIdValue(PreparedStatement PStmt,int w_id, int d_id) throws SQLException{
        int index=1;
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        ;
    }

    public void setStockGetCountStockValue(PreparedStatement PStmt,int w_id, int d_id, int o_id, int o_minus_id, int w_id_2, int threshold) throws SQLException{
        int index=1;
        PStmt.setInt(index++,w_id);
        PStmt.setInt(index++,d_id);
        PStmt.setInt(index++,o_id);
        PStmt.setInt(index++,o_minus_id);
        PStmt.setInt(index++,w_id_2);
        PStmt.setInt(index++,threshold);
        ;
    }

    public void setWarehouseGetWarehouseInfoByIDValue(PreparedStatement PStmt,int w_id) throws SQLException {
        int index=1;
        PStmt.setInt(index++,w_id);
        ;
    }

    public void setWarehouseGetWarehouseInfoByNameValue(PreparedStatement PStmt,String w_name) throws SQLException {
        int index=1;
        PStmt.setString(index++,w_name);
        ;
    }

    public void setWarehouseUpdateNameByIDValue(PreparedStatement PStmt,int w_id,String w_name) throws SQLException{
        int index=1;
        PStmt.setInt(index++,w_id);
        PStmt.setString(index++,w_name);
        ;
    }




    public int get_s_quantity(int s_quantity, int ol_quantity){
        if (s_quantity - ol_quantity >= 10) {
            s_quantity -= ol_quantity;
        } else {
            s_quantity += -ol_quantity + 91;
        }
        return s_quantity;
    }

    public int if_remote_increment(int w_id,int ol_supply_w_id){
        int s_remote_cnt_increment;
        if (ol_supply_w_id == w_id) {
            s_remote_cnt_increment = 0;
        } else {
            s_remote_cnt_increment = 1;
        }
        return s_remote_cnt_increment;
    }
    public float getWeight(){
        int prob=gen.nextInt(100);
        if (prob>90)
            return 0.12f;
        else
            return 0.21f;
    }


}
