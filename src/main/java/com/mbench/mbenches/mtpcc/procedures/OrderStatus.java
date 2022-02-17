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



import com.mbench.mbenches.mtpcc.mTPCCWorker;
import com.mbench.mbenches.mtpcc.mTPCCUtil;
import com.mbench.mbenches.mtpcc.pojos.Customer;
import com.mbench.mbenches.mtpcc.pojos.Oorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderStatus extends VaryProcedure {

    private static final Logger LOG = LoggerFactory.getLogger(OrderStatus.class);
//    private PreparedStatement ordStatGetNewestOrd = null;
//    private PreparedStatement ordStatGetOrderLines = null;
//    private PreparedStatement payGetCust = null;
//    private PreparedStatement customerByName = null;

    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {

        int d_id = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);
        int y = mTPCCUtil.randomNumber(1, 100, gen);

        boolean c_by_name;
        String c_last = null;
        int c_id = -1;

        if (y <= 60) {
            c_by_name = true;
            c_last = mTPCCUtil.getNonUniformRandomLastNameForRun(gen);
        } else {
            c_by_name = false;
            c_id = mTPCCUtil.getCustomerID(gen);
        }


        Customer c;

        if (c_by_name) {
            c = getCustomerByName(w_id, d_id, c_last, conn, w);
        } else {
            c = getCustomerById(w_id, d_id, c_id, conn, w);
        }


        Oorder o = getOrderDetails(conn, w_id, d_id, c, w);

        // retrieve the order lines for the most recent order
        List<String> orderLines = getOrderLines(conn, w_id, d_id, o.o_id, c, w);

        if (LOG.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append("+-------------------------- ORDER-STATUS -------------------------+\n");
            sb.append(" Date: ");
            sb.append(mTPCCUtil.getCurrentTime());
            sb.append("\n\n Warehouse: ");
            sb.append(w_id);
            sb.append("\n District:  ");
            sb.append(d_id);
            sb.append("\n\n Customer:  ");
            sb.append(c.c_id);
            sb.append("\n   Name:    ");
            sb.append(c.c_first);
            sb.append(" ");
            sb.append(c.c_middle);
            sb.append(" ");
            sb.append(c.c_last);
            sb.append("\n   Balance: ");
            sb.append(c.c_balance);
            sb.append("\n\n");
            if (o.o_id == -1) {
                sb.append(" Customer has no orders placed.\n");
            } else {
                sb.append(" Order-Number: ");
                sb.append(o.o_id);
                sb.append("\n    Entry-Date: ");
                sb.append(o.o_entry_d);
                sb.append("\n    Carrier-Number: ");
                sb.append(o.o_carrier_id);
                sb.append("\n\n");
                if (orderLines.size() != 0) {
                    sb.append(" [Supply_W - Item_ID - Qty - Amount - Delivery-Date]\n");
                    for (String orderLine : orderLines) {
                        sb.append(" ");
                        sb.append(orderLine);
                        sb.append("\n");
                    }
                } else {
                    LOG.trace(" This Order has no Order-Lines.\n");
                }
            }
            sb.append("+-----------------------------------------------------------------+\n\n");
            LOG.trace(sb.toString());
        }


    }

    private Oorder getOrderDetails(Connection conn, int w_id, int d_id, Customer c,mTPCCWorker w) throws SQLException {
        try (PreparedStatement ordStatGetNewestOrd = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("OrdStatGetNewestOrd"))) {


            // find the newest order for the customer
            // retrieve the carrier & order date for the most recent order.
            w.getVaryBase().setOrdStatGetNewestOrdValue(ordStatGetNewestOrd,w_id,d_id,c.c_id);

            try (ResultSet rs = ordStatGetNewestOrd.executeQuery()) {

                if (!rs.next()) {
                    String msg = String.format("No order records for CUSTOMER [C_W_ID=%d, C_D_ID=%d, C_ID=%d]", w_id, d_id, c.c_id);

                    throw new RuntimeException(msg);
                }
                Oorder o = new Oorder();
                o.o_id=rs.getInt("O_ID");
                o.o_carrier_id = rs.getInt("O_CARRIER_ID");
                o.o_entry_d = rs.getTimestamp("O_ENTRY_D");
                return o;
            }
        }
    }

    private List<String> getOrderLines(Connection conn, int w_id, int d_id, int o_id, Customer c,mTPCCWorker w) throws SQLException {
        List<String> orderLines = new ArrayList<>();

        try (PreparedStatement ordStatGetOrderLines = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("OrdStatGetOrderLines"))) {
            w.getVaryBase().setOrdStatGetOrderLinesValue(ordStatGetOrderLines,o_id,d_id,w_id);

            try (ResultSet rs = ordStatGetOrderLines.executeQuery()) {

                while (rs.next()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    sb.append(rs.getLong("OL_SUPPLY_W_ID"));
                    sb.append(" - ");
                    sb.append(rs.getLong("OL_I_ID"));
                    sb.append(" - ");
                    sb.append(rs.getLong("OL_QUANTITY"));
                    sb.append(" - ");
                    if (w.getWorkloadConfiguration().getBenchCase().equals("renameColumn")&&w.isChanged())
                        sb.append(mTPCCUtil.formattedDouble(rs.getDouble("OL_SIZE")));
                    else
                        sb.append(mTPCCUtil.formattedDouble(rs.getDouble("OL_AMOUNT")));
                    sb.append(" - ");
                    if (rs.getTimestamp("OL_DELIVERY_D") != null) {
                        sb.append(rs.getTimestamp("OL_DELIVERY_D"));
                    } else {
                        sb.append("99-99-9999");
                    }
                    sb.append("]");
                    orderLines.add(sb.toString());
                }
            }


            if (orderLines.isEmpty()) {
                String msg = String.format("Order record had no order line items [C_W_ID=%d, C_D_ID=%d, C_ID=%d, O_ID=%d]", w_id, d_id, c.c_id, o_id);
                LOG.trace(msg);
            }
        }

        return orderLines;
    }

    // attention duplicated code across trans... ok for now to maintain separate
    // prepared statements
    public Customer getCustomerById(int c_w_id, int c_d_id, int c_id, Connection conn,mTPCCWorker w) throws SQLException {

        try (PreparedStatement payGetCust = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayGetCust"))) {
            w.getVaryBase().setPayGetCustValue(payGetCust,c_w_id,c_d_id,c_id);

            try (ResultSet rs = payGetCust.executeQuery()) {

                if (!rs.next()) {
                    String msg = String.format("Failed to get CUSTOMER [C_W_ID=%d, C_D_ID=%d, C_ID=%d]", c_w_id, c_d_id, c_id);

                    throw new RuntimeException(msg);
                }

                Customer c = mTPCCUtil.newCustomerFromResults(rs);
                c.c_id = c_id;
                c.c_last = rs.getString("C_LAST");
                return c;
            }
        }
    }

    // attention this code is repeated in other transacitons... ok for now to
    // allow for separate statements.
    public Customer getCustomerByName(int c_w_id, int c_d_id, String c_last, Connection conn,mTPCCWorker w) throws SQLException {
        ArrayList<Customer> customers = new ArrayList<>();

        try (PreparedStatement customerByName = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("CustomerByName"))) {
            w.getVaryBase().setCustomerByNameValue(customerByName,c_w_id,c_d_id,c_last);

            try (ResultSet rs = customerByName.executeQuery()) {
                while (rs.next()) {
                    Customer c = mTPCCUtil.newCustomerFromResults(rs);
                    c.c_id = rs.getInt("C_ID");
                    c.c_last = c_last;
                    customers.add(c);
                }
            }
        }

        if (customers.size() == 0) {
            String msg = String.format("Failed to get CUSTOMER [C_W_ID=%d, C_D_ID=%d, C_LAST=%s]", c_w_id, c_d_id, c_last);

            throw new RuntimeException(msg);
        }

        // TPC-C 2.5.2.2: Position n / 2 rounded up to the next integer, but
        // that counts starting from 1.
        int index = customers.size() / 2;
        if (customers.size() % 2 == 0) {
            index -= 1;
        }
        return customers.get(index);
    }



//
//
//    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {
//        boolean trace = LOG.isTraceEnabled();
//        // initializing all prepared statements
//        payGetCust = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayGetCust")));
//        customerByName = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("CustomerByName")));
//        ordStatGetNewestOrd = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("OrdStatGetNewestOrd")));
//        ordStatGetOrderLines = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("OrdStatGetOrderLines")));
//            int d_id = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);
//            boolean c_by_name;
//            int y = mTPCCUtil.randomNumber(1, 100, gen);
//            String c_last = null;
//            int c_id = -1;
//            if (y <= 60) {
//                c_by_name = true;
//                c_last = mTPCCUtil.getNonUniformRandomLastNameForRun(gen);
//            } else {
//                c_by_name = false;
//                c_id = mTPCCUtil.getCustomerID(gen);
//            }
//
//            int o_id;
//            int o_carrier_id;
//            Timestamp o_entry_d;
//            ArrayList<String> orderLines = new ArrayList<>();
//
//            Customer c;
//            if (c_by_name) {
//
//                // TODO: This only needs c_balance, c_first, c_middle, c_id
//                // only fetch those columns?
//                c = getCustomerByName(w_id, d_id, c_last, conn,w);
//            } else {
//
//                c = getCustomerById(w_id, d_id, c_id, conn,w);
//            }
//
//            // find the newest order for the customer
//            // retrieve the carrier & order date for the most recent order.
//
//
//            if (trace) {
//                LOG.trace("ordStatGetNewestOrd START");
//            }
//            w.getVaryBase().setOrdStatGetNewestOrdValue(ordStatGetNewestOrd,w_id,d_id,c.c_id);
//            try (ResultSet rs = ordStatGetNewestOrd.executeQuery()) {
//                if (trace) {
//                    LOG.trace("ordStatGetNewestOrd END");
//                }
//
//                if (!rs.next()) {
//                    String msg = String.format("No order records for CUSTOMER [C_W_ID=%d, C_D_ID=%d, C_ID=%d]",
//                            w_id, d_id, c.c_id);
//                    if (trace) {
//                        LOG.warn(msg);
//                    }
//                    throw new RuntimeException(msg);
//                }
//
//                o_id = rs.getInt("O_ID");
//                o_carrier_id = rs.getInt("O_CARRIER_ID");
//                o_entry_d = rs.getTimestamp("O_ENTRY_D");
//            }
//
//            // retrieve the order lines for the most recent order
//
//            if (trace) {
//                LOG.trace("ordStatGetOrderLines START");
//            }
//            w.getVaryBase().setOrdStatGetOrderLinesValue(ordStatGetOrderLines,o_id,d_id,w_id);
//            try (ResultSet rs = ordStatGetOrderLines.executeQuery()) {
//                if (trace) {
//                    LOG.trace("ordStatGetOrderLines END");
//                }
//
//                while (rs.next()) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("[");
//                    sb.append(rs.getLong("OL_SUPPLY_W_ID"));
//                    sb.append(" - ");
//                    sb.append(rs.getLong("OL_I_ID"));
//                    sb.append(" - ");
//                    sb.append(rs.getLong("OL_QUANTITY"));
//                    sb.append(" - ");
//                    sb.append(mTPCCUtil.formattedDouble(rs.getDouble("OL_AMOUNT")));
//                    sb.append(" - ");
//                    if (rs.getTimestamp("OL_DELIVERY_D") != null) {
//                        sb.append(rs.getTimestamp("OL_DELIVERY_D"));
//                    } else {
//                        sb.append("99-99-9999");
//                    }
//                    sb.append("]");
//                    orderLines.add(sb.toString());
//                }
//            }
//
//
//            if (orderLines.isEmpty()) {
//                String msg = String.format("Order record had no order line items [C_W_ID=%d, C_D_ID=%d, C_ID=%d, O_ID=%d]",
//                        w_id, d_id, c.c_id, o_id);
//                if (trace) {
//                    LOG.warn(msg);
//                }
//            }
//
//            if (trace) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("\n");
//                sb.append("+-------------------------- ORDER-STATUS -------------------------+\n");
//                sb.append(" Date: ");
//                sb.append(mTPCCUtil.getCurrentTime());
//                sb.append("\n\n Warehouse: ");
//                sb.append(w_id);
//                sb.append("\n District:  ");
//                sb.append(d_id);
//                sb.append("\n\n Customer:  ");
//                sb.append(c.c_id);
//                sb.append("\n   Name:    ");
//                sb.append(c.c_first);
//                sb.append(" ");
//                sb.append(c.c_middle);
//                sb.append(" ");
//                sb.append(c.c_last);
//                sb.append("\n   Balance: ");
//                sb.append(c.c_balance);
//                sb.append("\n\n");
//                if (o_id == -1) {
//                    sb.append(" Customer has no orders placed.\n");
//                } else {
//                    sb.append(" Order-Number: ");
//                    sb.append(o_id);
//                    sb.append("\n    Entry-Date: ");
//                    sb.append(o_entry_d);
//                    sb.append("\n    Carrier-Number: ");
//                    sb.append(o_carrier_id);
//                    sb.append("\n\n");
//                    if (orderLines.size() != 0) {
//                        sb.append(" [Supply_W - Item_ID - Qty - Amount - Delivery-Date]\n");
//                        for (String orderLine : orderLines) {
//                            sb.append(" ");
//                            sb.append(orderLine);
//                            sb.append("\n");
//                        }
//                    } else {
//                        LOG.trace(" This Order has no Order-Lines.\n");
//                    }
//                }
//                sb.append("+-----------------------------------------------------------------+\n\n");
//                LOG.trace(sb.toString());
//            }
//
//
//    }
//
//    // attention duplicated code across trans... ok for now to maintain separate
//    // prepared statements
//    public Customer getCustomerById(int c_w_id, int c_d_id, int c_id, Connection conn, mTPCCWorker w) throws SQLException {
//        boolean trace = LOG.isTraceEnabled();
//
//
//            if (trace) {
//                LOG.trace("payGetCust START");
//            }
//            w.getVaryBase().setPayGetCustValue(payGetCust,c_w_id,c_d_id,c_id);
//            try (ResultSet rs = payGetCust.executeQuery()) {
//                if (trace) {
//                    LOG.trace("payGetCust END");
//                }
//                if (!rs.next()) {
//                    String msg = String.format("Failed to get CUSTOMER [C_W_ID=%d, C_D_ID=%d, C_ID=%d]",
//                            c_w_id, c_d_id, c_id);
//                    if (trace) {
//                        LOG.warn(msg);
//                    }
//                    throw new RuntimeException(msg);
//                }
//
//                Customer c = mTPCCUtil.newCustomerFromResults(rs);
//                c.c_id = c_id;
//                c.c_last = rs.getString("C_LAST");
//                return c;
//            }
//
//    }
//
//    // attention this code is repeated in other transacitons... ok for now to
//    // allow for separate statements.
//    public Customer getCustomerByName(int c_w_id, int c_d_id, String c_last, Connection conn, mTPCCWorker w) throws SQLException {
//        ArrayList<Customer> customers = new ArrayList<>();
//        boolean trace = LOG.isDebugEnabled();
//
//
//
//
//            if (trace) {
//                LOG.trace("customerByName START");
//            }
//            w.getVaryBase().setCustomerByNameValue(customerByName,c_w_id,c_d_id,c_last);
//            try (ResultSet rs = customerByName.executeQuery()) {
//                if (trace) {
//                    LOG.trace("customerByName END");
//                }
//
//                while (rs.next()) {
//                    Customer c = mTPCCUtil.newCustomerFromResults(rs);
//                    c.c_id = rs.getInt("C_ID");
//                    c.c_last = c_last;
//                    customers.add(c);
//                }
//
//        }
//
//        if (customers.size() == 0) {
//            String msg = String.format("Failed to get CUSTOMER [C_W_ID=%d, C_D_ID=%d, C_LAST=%s]",
//                    c_w_id, c_d_id, c_last);
//            if (trace) {
//                LOG.warn(msg);
//            }
//            throw new RuntimeException(msg);
//        }
//
//        // TPC-C 2.5.2.2: Position n / 2 rounded up to the next integer, but
//        // that counts starting from 1.
//        int index = customers.size() / 2;
//        if (customers.size() % 2 == 0) {
//            index -= 1;
//        }
//        return customers.get(index);
//    }


}



