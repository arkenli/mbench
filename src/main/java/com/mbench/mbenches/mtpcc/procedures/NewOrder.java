/*
 * Copyright 2020 by OLTPBenchmark Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.mbench.mbenches.mtpcc.procedures;



import com.mbench.mbenches.mtpcc.mTPCCConfig;
import com.mbench.mbenches.mtpcc.mTPCCWorker;
import com.mbench.mbenches.mtpcc.mTPCCUtil;
import com.mbench.mbenches.mtpcc.pojos.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Random;

public class NewOrder extends VaryProcedure {
//    private PreparedStatement stmtGetCust = null;
//    private PreparedStatement stmtGetWhse = null;
//    private PreparedStatement stmtGetDist = null;
//    private PreparedStatement stmtInsertNewOrder = null;
//    private PreparedStatement stmtUpdateDist = null;
//    private PreparedStatement stmtInsertOOrder = null;
//    private PreparedStatement stmtGetItem = null;
//    private PreparedStatement stmtGetStock = null;
//    private PreparedStatement stmtUpdateStock = null;
//    private PreparedStatement stmtInsertOrderLine = null;
    private static final Logger LOG = LoggerFactory.getLogger(NewOrder.class);


    public void run(Connection conn, Random gen, int terminalWarehouseID, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {

        int districtID = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);
        int customerID = mTPCCUtil.getCustomerID(gen);

        int numItems = mTPCCUtil.randomNumber(5, 15, gen);
        int[] itemIDs = new int[numItems];
        int[] supplierWarehouseIDs = new int[numItems];
        int[] orderQuantities = new int[numItems];
        int allLocal = 1;

        for (int i = 0; i < numItems; i++) {
            itemIDs[i] = mTPCCUtil.getItemID(gen);
            if (mTPCCUtil.randomNumber(1, 100, gen) > 1) {
                supplierWarehouseIDs[i] = terminalWarehouseID;
            } else {
                do {
                    supplierWarehouseIDs[i] = mTPCCUtil.randomNumber(1, numWarehouses, gen);
                }
                while (supplierWarehouseIDs[i] == terminalWarehouseID && numWarehouses > 1);
                allLocal = 0;
            }
            orderQuantities[i] = mTPCCUtil.randomNumber(1, 10, gen);
        }

        // we need to cause 1% of the new orders to be rolled back.
        if (mTPCCUtil.randomNumber(1, 100, gen) == 1) {
            itemIDs[numItems - 1] = mTPCCConfig.INVALID_ITEM_ID;
        }

        newOrderTransaction(terminalWarehouseID, districtID, customerID, numItems, allLocal, itemIDs, supplierWarehouseIDs, orderQuantities, conn,w);

    }


    private void newOrderTransaction(int w_id, int d_id, int c_id,
                                     int o_ol_cnt, int o_all_local, int[] itemIDs,
                                     int[] supplierWarehouseIDs, int[] orderQuantities, Connection conn,mTPCCWorker w) throws SQLException {


        getCustomer(conn, w_id, d_id, c_id,w);

        getWarehouse(conn, w_id,w);

        int d_next_o_id = getDistrict(conn, w_id, d_id,w);

        updateDistrict(conn, w_id, d_id,w);

        insertOpenOrder(conn, w_id, d_id, c_id, o_ol_cnt, o_all_local, d_next_o_id,w);

        insertNewOrder(conn, w_id, d_id, d_next_o_id,w);

        try (PreparedStatement stmtUpdateStock = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("UpdateStock"));
             PreparedStatement stmtInsertOrderLine = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("InsertOrderLine"))) {

            for (int ol_number = 1; ol_number <= o_ol_cnt; ol_number++) {
                int ol_supply_w_id = supplierWarehouseIDs[ol_number - 1];
                int ol_i_id = itemIDs[ol_number - 1];
                int ol_quantity = orderQuantities[ol_number - 1];

                // this may occasionally error and that's ok!
                float i_price = getItemPrice(conn, ol_i_id,w);

                float ol_amount = ol_quantity * i_price;

                Stock s = getStock(conn, ol_supply_w_id, ol_i_id, ol_quantity,w);

                String ol_dist_info = getDistInfo(d_id, s);
                String delivery_info=mTPCCUtil.randomStr(24);
                w.getVaryBase().setInsertOrderLineValue(stmtInsertOrderLine,d_next_o_id,d_id,w_id,ol_number,ol_i_id,ol_supply_w_id,ol_quantity,ol_amount, ol_dist_info,delivery_info);
                stmtInsertOrderLine.addBatch();

                int s_remote_cnt_increment;

                if (ol_supply_w_id == w_id) {
                    s_remote_cnt_increment = 0;
                } else {
                    s_remote_cnt_increment = 1;
                }
                w.getVaryBase().setUpdateStockValue(stmtUpdateStock,s.s_quantity,ol_quantity,s_remote_cnt_increment,ol_i_id,ol_supply_w_id);
                stmtUpdateStock.addBatch();

            }

            stmtInsertOrderLine.executeBatch();
            stmtInsertOrderLine.clearBatch();

            stmtUpdateStock.executeBatch();
            stmtUpdateStock.clearBatch();

        }

    }

    private String getDistInfo(int d_id, Stock s) {
        return switch (d_id) {
            case 1 -> s.s_dist_01;
            case 2 -> s.s_dist_02;
            case 3 -> s.s_dist_03;
            case 4 -> s.s_dist_04;
            case 5 -> s.s_dist_05;
            case 6 -> s.s_dist_06;
            case 7 -> s.s_dist_07;
            case 8 -> s.s_dist_08;
            case 9 -> s.s_dist_09;
            case 10 -> s.s_dist_10;
            default -> null;
        };
    }

    private Stock getStock(Connection conn, int ol_supply_w_id, int ol_i_id, int ol_quantity,mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtGetStock = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("GetStock"))) {
            w.getVaryBase().setGetStockValue(stmtGetStock,ol_i_id,ol_supply_w_id);
            try (ResultSet rs = stmtGetStock.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("S_I_ID=" + ol_i_id + " not found!");
                }
                Stock s = new Stock();
                s.s_quantity = rs.getInt("S_QUANTITY");
                s.s_dist_01 = rs.getString("S_DIST_01");
                s.s_dist_02 = rs.getString("S_DIST_02");
                s.s_dist_03 = rs.getString("S_DIST_03");
                s.s_dist_04 = rs.getString("S_DIST_04");
                s.s_dist_05 = rs.getString("S_DIST_05");
                s.s_dist_06 = rs.getString("S_DIST_06");
                s.s_dist_07 = rs.getString("S_DIST_07");
                s.s_dist_08 = rs.getString("S_DIST_08");
                s.s_dist_09 = rs.getString("S_DIST_09");
                s.s_dist_10 = rs.getString("S_DIST_10");

                if (s.s_quantity - ol_quantity >= 10) {
                    s.s_quantity -= ol_quantity;
                } else {
                    s.s_quantity += -ol_quantity + 91;
                }

                return s;
            }
        }
    }

    private float getItemPrice(Connection conn, int ol_i_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtGetItem = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("GetItem"))) {
            w.getVaryBase().setGetItemValue(stmtGetItem,ol_i_id);
            try (ResultSet rs = stmtGetItem.executeQuery()) {
                if (!rs.next()) {
                    // This is (hopefully) an expected error: this is an expected new order rollback
                    throw new UserAbortException("EXPECTED new order rollback: I_ID=" + ol_i_id + " not found!");
                }

                return rs.getFloat("I_PRICE");
            }
        }
    }

    private void insertNewOrder(Connection conn, int w_id, int d_id, int o_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtInsertNewOrder = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("InsertNewOrder"));) {
            w.getVaryBase().setInsertNewOrderValue(stmtInsertNewOrder,o_id,d_id,w_id);
            int result = stmtInsertNewOrder.executeUpdate();

            if (result == 0) {
                LOG.warn("new order not inserted");
            }
        }
    }

    private void insertOpenOrder(Connection conn, int w_id, int d_id, int c_id, int o_ol_cnt, int o_all_local, int o_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtInsertOOrder = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("InsertOOrder"));) {
            w.getVaryBase().setInsertOOrderValue(stmtInsertOOrder,o_id,d_id,w_id,c_id,new Timestamp(System.currentTimeMillis()),o_ol_cnt,o_all_local);

            int result = stmtInsertOOrder.executeUpdate();

            if (result == 0) {
                LOG.warn("open order not inserted");
            }
        }
    }

    private void updateDistrict(Connection conn, int w_id, int d_id, mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtUpdateDist = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("UpdateDist"))) {
            w.getVaryBase().setUpdateDistValue(stmtUpdateDist,w_id,d_id);
            int result = stmtUpdateDist.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Error!! Cannot update next_order_id on district for D_ID=" + d_id + " D_W_ID=" + w_id);
            }
        }
    }

    private int getDistrict(Connection conn, int w_id, int d_id, mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtGetDist = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("GetDist"))) {
            w.getVaryBase().setGetDistValue(stmtGetDist,w_id,d_id);
            try (ResultSet rs = stmtGetDist.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("D_ID=" + d_id + " D_W_ID=" + w_id + " not found!");
                }
                return rs.getInt("D_NEXT_O_ID");
            }
        }
    }

    private void getWarehouse(Connection conn, int w_id, mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtGetWhse = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("GetWhse"))) {
            w.getVaryBase().setGetWhseValue(stmtGetWhse,w_id);
            try (ResultSet rs = stmtGetWhse.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("W_ID=" + w_id + " not found!");
                }
            }
        }
    }

    private void getCustomer(Connection conn, int w_id, int d_id, int c_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement stmtGetCust = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("GetCust"))) {
            w.getVaryBase().setGetCustValue(stmtGetCust,w_id,d_id,c_id);
            try (ResultSet rs = stmtGetCust.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("C_D_ID=" + d_id + " C_ID=" + c_id + " not found!");
                }
            }
        }
    }







//    public void run(Connection conn, Random gen, int terminalWarehouseID, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {
//
//
//        //initializing all prepared statements
//        stmtGetCust=this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("GetCust")));
//        stmtGetWhse=this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("GetWhse")));
//        stmtGetDist=this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("GetDist")));
//        stmtInsertNewOrder=this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("InsertNewOrder")));
//        stmtUpdateDist =this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("UpdateDist")));
//        stmtInsertOOrder =this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("InsertOOrder")));
//        stmtGetItem =this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("GetItem")));
//        stmtGetStock =this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("GetStock")));
//        stmtUpdateStock =this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("UpdateStock")));
//        stmtInsertOrderLine =this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("InsertOrderLine")));
//
//        int districtID = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);
//        int customerID = mTPCCUtil.getCustomerID(gen);
//
//        int numItems = mTPCCUtil.randomNumber(5, 15, gen);
//        int[] itemIDs = new int[numItems];
//        int[] supplierWarehouseIDs = new int[numItems];
//        int[] orderQuantities = new int[numItems];
//        int allLocal = 1;
//        for (int i = 0; i < numItems; i++) {
//            itemIDs[i] = mTPCCUtil.getItemID(gen);
//            if (mTPCCUtil.randomNumber(1, 100, gen) > 1) {
//                supplierWarehouseIDs[i] = terminalWarehouseID;
//            } else {
//                do {
//                    supplierWarehouseIDs[i] = mTPCCUtil.randomNumber(1,
//                            numWarehouses, gen);
//                }
//                while (supplierWarehouseIDs[i] == terminalWarehouseID
//                        && numWarehouses > 1);
//                allLocal = 0;
//            }
//            orderQuantities[i] = mTPCCUtil.randomNumber(1, 10, gen);
//        }
//
//        // we need to cause 1% of the new orders to be rolled back.
//        if (mTPCCUtil.randomNumber(1, 100, gen) == 1) {
//            itemIDs[numItems - 1] = mTPCCConfig.INVALID_ITEM_ID;
//        }
//
//
//        newOrderTransaction(terminalWarehouseID, districtID,
//                customerID, numItems, allLocal, itemIDs,
//                supplierWarehouseIDs, orderQuantities, conn, w);
//
//    }
//
//
//    private void newOrderTransaction(int w_id, int d_id, int c_id,
//                                     int o_ol_cnt, int o_all_local, int[] itemIDs,
//                                     int[] supplierWarehouseIDs, int[] orderQuantities, Connection conn, mTPCCWorker w)
//            throws SQLException {
//        float i_price;
//        int d_next_o_id;
//        int o_id;
//        int s_quantity;
//        String s_dist_01;
//        String s_dist_02;
//        String s_dist_03;
//        String s_dist_04;
//        String s_dist_05;
//        String s_dist_06;
//        String s_dist_07;
//        String s_dist_08;
//        String s_dist_09;
//        String s_dist_10;
//        String ol_dist_info = null;
//
//        int ol_supply_w_id;
//        int ol_i_id;
//        int ol_quantity;
//        int s_remote_cnt_increment;
//        float ol_amount;
//
//        w.getVaryBase().setGetCustValue(stmtGetCust,w_id,d_id,c_id);
//        try (ResultSet rs = stmtGetCust.executeQuery()) {
//            if (!rs.next()) {
//                throw new RuntimeException("C_D_ID=" + d_id
//                        + " C_ID=" + c_id + " not found!");
//            }
//        }
//
//        w.getVaryBase().setGetWhseValue(stmtGetWhse,w_id);
//        try (ResultSet rs = stmtGetWhse.executeQuery()) {
//            if (!rs.next()) {
//                throw new RuntimeException("W_ID=" + w_id + " not found!");
//            }
//        }
//
//        w.getVaryBase().setGetDistValue(stmtGetDist,w_id,d_id);
//        try (ResultSet rs = stmtGetDist.executeQuery()) {
//            if (!rs.next()) {
//                throw new RuntimeException("D_ID=" + d_id + " D_W_ID=" + w_id
//                        + " not found!");
//            }
//            d_next_o_id = rs.getInt("D_NEXT_O_ID");
//        }
//
//        //woonhak, need to change order because of foreign key constraints
//        //update next_order_id first, but it might doesn't matter
//        w.getVaryBase().setUpdateDistValue(stmtUpdateDist,w_id,d_id);
//        int result = stmtUpdateDist.executeUpdate();
//        if (result == 0) {
//            throw new RuntimeException(
//                    "Error!! Cannot update next_order_id on district for D_ID="
//                            + d_id + " D_W_ID=" + w_id);
//        }
//
//        o_id = d_next_o_id;
//
//        // woonhak, need to change order, because of foreign key constraints
//        //[[insert ooder first
//
//        w.getVaryBase().setInsertOOrderValue(stmtInsertOOrder,o_id,d_id,w_id,c_id,new Timestamp(System.currentTimeMillis()),o_ol_cnt,o_all_local);
//        stmtInsertOOrder.executeUpdate();
//
//        //insert ooder first]]
//        /*TODO: add error checking */
//
//
//        w.getVaryBase().setInsertNewOrderValue(stmtInsertNewOrder,o_id,d_id,w_id);
//        stmtInsertNewOrder.executeUpdate();
//
//        /*TODO: add error checking */
//
//
//        for (int ol_number = 1; ol_number <= o_ol_cnt; ol_number++) {
//            ol_supply_w_id = supplierWarehouseIDs[ol_number - 1];
//            ol_i_id = itemIDs[ol_number - 1];
//            ol_quantity = orderQuantities[ol_number - 1];
//            w.getVaryBase().setGetItemValue(stmtGetItem,ol_i_id);
//            try (ResultSet rs = stmtGetItem.executeQuery()) {
//                if (!rs.next()) {
//                    // This is (hopefully) an expected error: this is an
//                    // expected new order rollback
//                    throw new UserAbortException(
//                            "EXPECTED new order rollback: I_ID=" + ol_i_id
//                                    + " not found!");
//                }
//
//                i_price = rs.getFloat("I_PRICE");
//            }
//
//
//
//            w.getVaryBase().setGetStockValue(stmtGetStock,ol_i_id,ol_supply_w_id);
//            try (ResultSet rs = stmtGetStock.executeQuery()) {
//                if (!rs.next()) {
//                    throw new RuntimeException("I_ID=" + ol_i_id
//                            + " not found!");
//                }
//                s_quantity = rs.getInt("S_QUANTITY");
//                s_dist_01 = rs.getString("S_DIST_01");
//                s_dist_02 = rs.getString("S_DIST_02");
//                s_dist_03 = rs.getString("S_DIST_03");
//                s_dist_04 = rs.getString("S_DIST_04");
//                s_dist_05 = rs.getString("S_DIST_05");
//                s_dist_06 = rs.getString("S_DIST_06");
//                s_dist_07 = rs.getString("S_DIST_07");
//                s_dist_08 = rs.getString("S_DIST_08");
//                s_dist_09 = rs.getString("S_DIST_09");
//                s_dist_10 = rs.getString("S_DIST_10");
//            }
//
//            s_quantity=w.getVaryBase().get_s_quantity(s_quantity,ol_quantity);
//
//            s_remote_cnt_increment=w.getVaryBase().if_remote_increment(w_id,ol_supply_w_id);
//
//
//            w.getVaryBase().setUpdateStockValue(stmtUpdateStock,s_quantity,ol_quantity,s_remote_cnt_increment,ol_i_id,ol_supply_w_id);
//            stmtUpdateStock.addBatch();
//
//
//            ol_amount = ol_quantity * i_price;
//
//
//            switch (d_id) {
//                case 1:
//                    ol_dist_info = s_dist_01;
//                    break;
//                case 2:
//                    ol_dist_info = s_dist_02;
//                    break;
//                case 3:
//                    ol_dist_info = s_dist_03;
//                    break;
//                case 4:
//                    ol_dist_info = s_dist_04;
//                    break;
//                case 5:
//                    ol_dist_info = s_dist_05;
//                    break;
//                case 6:
//                    ol_dist_info = s_dist_06;
//                    break;
//                case 7:
//                    ol_dist_info = s_dist_07;
//                    break;
//                case 8:
//                    ol_dist_info = s_dist_08;
//                    break;
//                case 9:
//                    ol_dist_info = s_dist_09;
//                    break;
//                case 10:
//                    ol_dist_info = s_dist_10;
//                    break;
//            }
//            String delivery_info=mTPCCUtil.randomStr(24);
//            w.getVaryBase().setInsertOrderLineValue(stmtInsertOrderLine,o_id,d_id,w_id,ol_number,ol_i_id,ol_supply_w_id,ol_quantity,ol_amount, ol_dist_info,delivery_info);
//            stmtInsertOrderLine.addBatch();
//
//        }
//        stmtUpdateStock.executeBatch();
//        stmtUpdateStock.clearBatch();
//        stmtInsertOrderLine.executeBatch();
//        stmtInsertOrderLine.executeBatch();
//    }
}



