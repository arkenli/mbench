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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Random;

public class Delivery extends VaryProcedure {

    private static final Logger LOG = LoggerFactory.getLogger(Delivery.class);
//    private PreparedStatement delivGetOrderId = null;
//    private PreparedStatement delivDeleteNewOrder = null;
//    private PreparedStatement delivGetCustId = null;
//    private PreparedStatement delivUpdateCarrierId = null;
//    private PreparedStatement delivUpdateDeliveryDate = null;
//    private PreparedStatement delivSumOrderAmount = null;
//    private PreparedStatement delivUpdateCustBalDelivCnt = null;


    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {

        int o_carrier_id = mTPCCUtil.randomNumber(1, 10, gen);

        int d_id;

        int[] orderIDs = new int[10];

        for (d_id = 1; d_id <= terminalDistrictUpperID; d_id++) {
            Integer no_o_id = getOrderId(conn, w_id, d_id,w);

            if (no_o_id == null) {
                continue;
            }

            orderIDs[d_id - 1] = no_o_id;

            deleteOrder(conn, w_id, d_id, no_o_id,w);

            int customerId = getCustomerId(conn, w_id, d_id, no_o_id,w);

            updateCarrierId(conn, w_id, o_carrier_id, d_id, no_o_id,w);

            updateDeliveryDate(conn, w_id, d_id, no_o_id,w);

            float orderLineTotal = getOrderLineTotal(conn, w_id, d_id, no_o_id,w);

            updateBalanceAndDelivery(conn, w_id, d_id, customerId, orderLineTotal,w);
        }

        if (LOG.isTraceEnabled()) {
            StringBuilder terminalMessage = new StringBuilder();
            terminalMessage.append("\n+---------------------------- DELIVERY ---------------------------+\n");
            terminalMessage.append(" Date: ");
            terminalMessage.append(mTPCCUtil.getCurrentTime());
            terminalMessage.append("\n\n Warehouse: ");
            terminalMessage.append(w_id);
            terminalMessage.append("\n Carrier:   ");
            terminalMessage.append(o_carrier_id);
            terminalMessage.append("\n\n Delivered Orders\n");
            for (int i = 1; i <= mTPCCConfig.configDistPerWhse; i++) {
                if (orderIDs[i - 1] >= 0) {
                    terminalMessage.append("  District ");
                    terminalMessage.append(i < 10 ? " " : "");
                    terminalMessage.append(i);
                    terminalMessage.append(": Order number ");
                    terminalMessage.append(orderIDs[i - 1]);
                    terminalMessage.append(" was delivered.\n");
                }
            }
            terminalMessage.append("+-----------------------------------------------------------------+\n\n");
            LOG.trace(terminalMessage.toString());
        }

    }

    private Integer getOrderId(Connection conn, int w_id, int d_id,mTPCCWorker w) throws SQLException {

        try (PreparedStatement delivGetOrderId = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("DelivGetOrderId"))) {
            w.getVaryBase().setDelivGetOrderIdValue(delivGetOrderId,d_id,w_id);

            try (ResultSet rs = delivGetOrderId.executeQuery()) {

                if (!rs.next()) {
                    // This district has no new orders.  This can happen but should be rare

                    LOG.warn(String.format("District has no new orders [W_ID=%d, D_ID=%d]", w_id, d_id));

                    return null;
                }

                return rs.getInt("NO_O_ID");

            }
        }
    }

    private void deleteOrder(Connection conn, int w_id, int d_id, int no_o_id, mTPCCWorker w) throws SQLException {
        try (PreparedStatement delivDeleteNewOrder = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("DelivDeleteNewOrder"))) {
            w.getVaryBase().setDeleteNewOrderValue(delivDeleteNewOrder,no_o_id,d_id,w_id);


            int result = delivDeleteNewOrder.executeUpdate();

            if (result != 1) {
                // This code used to run in a loop in an attempt to make this work
                // with MySQL's default weird consistency level. We just always run
                // this as SERIALIZABLE instead. I don't *think* that fixing this one
                // error makes this work with MySQL's default consistency.
                // Careful auditing would be required.
                String msg = String.format("NewOrder delete failed. Not running with SERIALIZABLE isolation? [w_id=%d, d_id=%d, no_o_id=%d]", w_id, d_id, no_o_id);
                throw new UserAbortException(msg);
            }
        }
    }

    private int getCustomerId(Connection conn, int w_id, int d_id, int no_o_id, mTPCCWorker w) throws SQLException {

        try (PreparedStatement delivGetCustId = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("DelivGetCustId"))) {
            w.getVaryBase().setDelivGetCustIdValue(delivGetCustId,no_o_id,d_id,w_id);
            try (ResultSet rs = delivGetCustId.executeQuery()) {

                if (!rs.next()) {
                    String msg = String.format("Failed to retrieve ORDER record [W_ID=%d, D_ID=%d, O_ID=%d]", w_id, d_id, no_o_id);
                    throw new RuntimeException(msg);
                }

                return rs.getInt("O_C_ID");
            }
        }
    }

    private void updateCarrierId(Connection conn, int w_id, int o_carrier_id, int d_id, int no_o_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement delivUpdateCarrierId = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("DelivUpdateCarrierId"))) {
            w.getVaryBase().setDelivUpdateCarrierValue(delivUpdateCarrierId,o_carrier_id,no_o_id,d_id,w_id);

            int result = delivUpdateCarrierId.executeUpdate();

            if (result != 1) {
                String msg = String.format("Failed to update ORDER record [W_ID=%d, D_ID=%d, O_ID=%d]", w_id, d_id, no_o_id);
                throw new RuntimeException(msg);
            }
        }
    }

    private void updateDeliveryDate(Connection conn, int w_id, int d_id, int no_o_id,mTPCCWorker w) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try (PreparedStatement delivUpdateDeliveryDate = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("DelivUpdateDeliveryDate"))) {
            w.getVaryBase().setDelivUpdateDeleiveryDatValue(delivUpdateDeliveryDate,timestamp,no_o_id,d_id,w_id);

            int result = delivUpdateDeliveryDate.executeUpdate();

            if (result == 0) {
                String msg = String.format("Failed to update ORDER_LINE records [W_ID=%d, D_ID=%d, O_ID=%d]", w_id, d_id, no_o_id);
                throw new RuntimeException(msg);
            }
        }
    }

    private float getOrderLineTotal(Connection conn, int w_id, int d_id, int no_o_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement delivSumOrderAmount = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("DelivSumOrderAmount"))) {
            w.getVaryBase().setDelivSumOrderAmountValue(delivSumOrderAmount,no_o_id,d_id,w_id);

            try (ResultSet rs = delivSumOrderAmount.executeQuery()) {
                if (!rs.next()) {
                    String msg = String.format("Failed to retrieve ORDER_LINE records [W_ID=%d, D_ID=%d, O_ID=%d]", w_id, d_id, no_o_id);
                    throw new RuntimeException(msg);
                }

                return rs.getFloat("OL_TOTAL");
            }
        }
    }

    private void updateBalanceAndDelivery(Connection conn, int w_id, int d_id, int c_id, float orderLineTotal,mTPCCWorker w) throws SQLException {

        try (PreparedStatement delivUpdateCustBalDelivCnt = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("DelivUpdateCustBalDelivCnt"))) {
            w.getVaryBase().setDelivUpdateCustBalDelivCntValue(delivUpdateCustBalDelivCnt,BigDecimal.valueOf(orderLineTotal),w_id,d_id,c_id);

            int result = delivUpdateCustBalDelivCnt.executeUpdate();

            if (result == 0) {
                String msg = String.format("Failed to update CUSTOMER record [W_ID=%d, D_ID=%d, C_ID=%d]", w_id, d_id, c_id);
                throw new RuntimeException(msg);
            }
        }
    }







//
//    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {
//        boolean trace = LOG.isDebugEnabled();
//        int o_carrier_id = mTPCCUtil.randomNumber(1, 10, gen);
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//
//        // Delivery Txn
//        delivGetOrderId = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("DelivGetOrderId")));
//        delivDeleteNewOrder =  this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("DelivDeleteNewOrder")));
//        delivGetCustId = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("DelivGetCustId")));
//        delivUpdateCarrierId = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("DelivUpdateCarrierId")));
//        delivUpdateDeliveryDate = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("DelivUpdateDeliveryDate")));
//        delivSumOrderAmount = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("DelivSumOrderAmount")));
//        delivUpdateCustBalDelivCnt = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("DelivUpdateCustBalDelivCnt")));
//
//            int d_id, c_id;
//            float ol_total;
//            int[] orderIDs;
//
//            orderIDs = new int[10];
//            for (d_id = 1; d_id <= terminalDistrictUpperID; d_id++) {
//
//                if (trace) {
//                    LOG.trace("delivGetOrderId START");
//                }
//
//                int no_o_id;
//                w.getVaryBase().setDelivGetOrderIdValue(delivGetOrderId,d_id,w_id);
//                try (ResultSet rs = delivGetOrderId.executeQuery()) {
//                    if (trace) {
//                        LOG.trace("delivGetOrderId END");
//                    }
//                    if (!rs.next()) {
//                        // This district has no new orders
//                        // This can happen but should be rare
//                        if (trace) {
//                            LOG.warn(String.format("District has no new orders [W_ID=%d, D_ID=%d]", w_id, d_id));
//                        }
//                        continue;
//                    }
//
//                    no_o_id = rs.getInt("NO_O_ID");
//                    orderIDs[d_id - 1] = no_o_id;
//                }
//
//
//                if (trace) {
//                    LOG.trace("delivDeleteNewOrder START");
//                }
//                w.getVaryBase().setDeleteNewOrderValue(delivDeleteNewOrder,no_o_id,d_id,w_id);
//                int result =delivDeleteNewOrder.executeUpdate();
//                if (trace) {
//                    LOG.trace("delivDeleteNewOrder END");
//                }
//                if (result != 1) {
//                    // This code used to run in a loop in an attempt to make this work
//                    // with MySQL's default weird consistency level. We just always run
//                    // this as SERIALIZABLE instead. I don't *think* that fixing this one
//                    // error makes this work with MySQL's default consistency.
//                    // Careful auditing would be required.
//                    String msg = String.format("NewOrder delete failed. Not running with SERIALIZABLE isolation? " +
//                            "[w_id=%d, d_id=%d, no_o_id=%d]", w_id, d_id, no_o_id);
//                    throw new UserAbortException(msg);
//                }
//
//
//
//                if (trace) {
//                    LOG.trace("delivGetCustId START");
//                }
//                w.getVaryBase().setDelivGetCustIdValue(delivGetCustId,no_o_id,d_id,w_id);
//                try (ResultSet rs = delivGetCustId.executeQuery()) {
//                    if (trace) {
//                        LOG.trace("delivGetCustId END");
//                    }
//
//                    if (!rs.next()) {
//                        String msg = String.format("Failed to retrieve ORDER record [W_ID=%d, D_ID=%d, O_ID=%d]",
//                                w_id, d_id, no_o_id);
//                        if (trace) {
//                            LOG.warn(msg);
//                        }
//                        throw new RuntimeException(msg);
//                    }
//                    c_id = rs.getInt("O_C_ID");
//                }
//
//
//                if (trace) {
//                    LOG.trace("delivUpdateCarrierId START");
//                }
//                w.getVaryBase().setDelivUpdateCarrierValue(delivUpdateCarrierId,o_carrier_id,no_o_id,d_id,w_id);
//                result = delivUpdateCarrierId.executeUpdate();
//                if (trace) {
//                    LOG.trace("delivUpdateCarrierId END");
//                }
//
//                if (result != 1) {
//                    String msg = String.format("Failed to update ORDER record [W_ID=%d, D_ID=%d, O_ID=%d]",
//                            w_id, d_id, no_o_id);
//                    if (trace) {
//                        LOG.warn(msg);
//                    }
//                    throw new RuntimeException(msg);
//                }
//
//
//                if (trace) {
//                    LOG.trace("delivUpdateDeliveryDate START");
//                }
//                w.getVaryBase().setDelivUpdateDeleiveryDatValue(delivUpdateDeliveryDate,timestamp,no_o_id,d_id,w_id);
//                result = delivUpdateDeliveryDate.executeUpdate();
//                if (trace) {
//                    LOG.trace("delivUpdateDeliveryDate END");
//                }
//
//                if (result == 0) {
//                    String msg = String.format("Failed to update ORDER_LINE records [W_ID=%d, D_ID=%d, O_ID=%d]",
//                            w_id, d_id, no_o_id);
//                    if (trace) {
//                        LOG.warn(msg);
//                    }
//                    throw new RuntimeException(msg);
//                }
//
//
//                if (trace) {
//                    LOG.trace("delivSumOrderAmount START");
//                }
//                w.getVaryBase().setDelivSumOrderAmountValue(delivSumOrderAmount,no_o_id,d_id,w_id);
//                try (ResultSet rs = delivSumOrderAmount.executeQuery()) {
//                    if (trace) {
//                        LOG.trace("delivSumOrderAmount END");
//                    }
//
//                    if (!rs.next()) {
//                        String msg = String.format("Failed to retrieve ORDER_LINE records [W_ID=%d, D_ID=%d, O_ID=%d]",
//                                w_id, d_id, no_o_id);
//                        if (trace) {
//                            LOG.warn(msg);
//                        }
//                        throw new RuntimeException(msg);
//                    }
//                    ol_total = rs.getFloat("OL_TOTAL");
//                }
//
//                int idx = 1; // HACK: So that we can debug this query
//
//                if (trace) {
//                    LOG.trace("delivUpdateCustBalDelivCnt START");
//                }
//                w.getVaryBase().setDelivUpdateCustBalDelivCntValue(delivUpdateCustBalDelivCnt,BigDecimal.valueOf(ol_total),w_id,d_id,c_id);
//                result = delivUpdateCustBalDelivCnt.executeUpdate();
//                if (trace) {
//                    LOG.trace("delivUpdateCustBalDelivCnt END");
//                }
//
//                if (result == 0) {
//                    String msg = String.format("Failed to update CUSTOMER record [W_ID=%d, D_ID=%d, C_ID=%d]",
//                            w_id, d_id, c_id);
//                    if (trace) {
//                        LOG.warn(msg);
//                    }
//                    throw new RuntimeException(msg);
//                }
//            }
//
//            if (trace) {
//                StringBuilder terminalMessage = new StringBuilder();
//                terminalMessage
//                        .append("\n+---------------------------- DELIVERY ---------------------------+\n");
//                terminalMessage.append(" Date: ");
//                terminalMessage.append(mTPCCUtil.getCurrentTime());
//                terminalMessage.append("\n\n Warehouse: ");
//                terminalMessage.append(w_id);
//                terminalMessage.append("\n Carrier:   ");
//                terminalMessage.append(o_carrier_id);
//                terminalMessage.append("\n\n Delivered Orders\n");
//                for (int i = 1; i <= mTPCCConfig.configDistPerWhse; i++) {
//                    if (orderIDs[i - 1] >= 0) {
//                        terminalMessage.append("  District ");
//                        terminalMessage.append(i < 10 ? " " : "");
//                        terminalMessage.append(i);
//                        terminalMessage.append(": Order number ");
//                        terminalMessage.append(orderIDs[i - 1]);
//                        terminalMessage.append(" was delivered.\n");
//                    }
//                }
//                terminalMessage.append("+-----------------------------------------------------------------+\n\n");
//                LOG.trace(terminalMessage.toString());
//            }
//        }


}
