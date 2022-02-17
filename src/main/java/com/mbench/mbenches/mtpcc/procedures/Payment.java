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
import com.mbench.mbenches.mtpcc.pojos.Customer;
import com.mbench.mbenches.mtpcc.pojos.District;
import com.mbench.mbenches.mtpcc.pojos.Warehouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

public class Payment extends VaryProcedure {

    private static final Logger LOG = LoggerFactory.getLogger(Payment.class);


    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker worker) throws SQLException {

        int districtID = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);

        float paymentAmount = (float) (mTPCCUtil.randomNumber(100, 500000, gen) / 100.0);

        updateWarehouse(conn, w_id, paymentAmount, worker);

        Warehouse w = getWarehouse(conn, w_id, worker);

        updateDistrict(conn, w_id, districtID, paymentAmount, worker);

        District d = getDistrict(conn, w_id, districtID, worker);

        int x = mTPCCUtil.randomNumber(1, 100, gen);

        int customerDistrictID = getCustomerDistrictId(gen, districtID, x);
        int customerWarehouseID = getCustomerWarehouseID(gen, w_id, numWarehouses, x);

        Customer c = getCustomer(conn, gen, customerDistrictID, customerWarehouseID, paymentAmount, worker);

        if (c.c_credit.equals("BC")) {
            // bad credit
            c.c_data = getCData(conn, w_id, districtID, customerDistrictID, customerWarehouseID, paymentAmount, c, worker);

            updateBalanceCData(conn, customerDistrictID, customerWarehouseID, c, worker);

        } else {
            // GoodCredit

            updateBalance(conn, customerDistrictID, customerWarehouseID, c, worker);

        }

        insertHistory(conn, w_id, districtID, customerDistrictID, customerWarehouseID, paymentAmount, w.w_name, d.d_name, c, worker);

        if (LOG.isTraceEnabled()) {
            StringBuilder terminalMessage = new StringBuilder();
            terminalMessage.append("\n+---------------------------- PAYMENT ----------------------------+");
            terminalMessage.append("\n Date: ").append(mTPCCUtil.getCurrentTime());
            terminalMessage.append("\n\n Warehouse: ");
            terminalMessage.append(w_id);
            terminalMessage.append("\n   Street:  ");
            terminalMessage.append(w.w_street_1);
            terminalMessage.append("\n   Street:  ");
            terminalMessage.append(w.w_street_2);
            terminalMessage.append("\n   City:    ");
            terminalMessage.append(w.w_city);
            terminalMessage.append("   State: ");
            terminalMessage.append(w.w_state);
            terminalMessage.append("  Zip: ");
            terminalMessage.append(w.w_zip);
            terminalMessage.append("\n\n District:  ");
            terminalMessage.append(districtID);
            terminalMessage.append("\n   Street:  ");
            terminalMessage.append(d.d_street_1);
            terminalMessage.append("\n   Street:  ");
            terminalMessage.append(d.d_street_2);
            terminalMessage.append("\n   City:    ");
            terminalMessage.append(d.d_city);
            terminalMessage.append("   State: ");
            terminalMessage.append(d.d_state);
            terminalMessage.append("  Zip: ");
            terminalMessage.append(d.d_zip);
            terminalMessage.append("\n\n Customer:  ");
            terminalMessage.append(c.c_id);
            terminalMessage.append("\n   Name:    ");
            terminalMessage.append(c.c_first);
            terminalMessage.append(" ");
            terminalMessage.append(c.c_middle);
            terminalMessage.append(" ");
            terminalMessage.append(c.c_last);
            terminalMessage.append("\n   Street:  ");
            terminalMessage.append(c.c_street_1);
            terminalMessage.append("\n   Street:  ");
            terminalMessage.append(c.c_street_2);
            terminalMessage.append("\n   City:    ");
            terminalMessage.append(c.c_city);
            terminalMessage.append("   State: ");
            terminalMessage.append(c.c_state);
            terminalMessage.append("  Zip: ");
            terminalMessage.append(c.c_zip);
            terminalMessage.append("\n   Since:   ");
            if (c.c_since != null) {
                terminalMessage.append(c.c_since.toString());
            } else {
                terminalMessage.append("");
            }
            terminalMessage.append("\n   Credit:  ");
            terminalMessage.append(c.c_credit);
            terminalMessage.append("\n   %Disc:   ");
            terminalMessage.append(c.c_discount);
            terminalMessage.append("\n   Phone:   ");
            terminalMessage.append(c.c_phone);
            terminalMessage.append("\n\n Amount Paid:      ");
            terminalMessage.append(paymentAmount);
            terminalMessage.append("\n Credit Limit:     ");
            terminalMessage.append(c.c_credit_lim);
            terminalMessage.append("\n New Cust-Balance: ");
            terminalMessage.append(c.c_balance);
            if (c.c_credit.equals("BC")) {
                if (c.c_data.length() > 50) {
                    terminalMessage.append("\n\n Cust-Data: ").append(c.c_data.substring(0, 50));
                    int data_chunks = c.c_data.length() > 200 ? 4 : c.c_data.length() / 50;
                    for (int n = 1; n < data_chunks; n++) {
                        terminalMessage.append("\n            ").append(c.c_data.substring(n * 50, (n + 1) * 50));
                    }
                } else {
                    terminalMessage.append("\n\n Cust-Data: ").append(c.c_data);
                }
            }
            terminalMessage.append("\n+-----------------------------------------------------------------+\n\n");

            LOG.trace(terminalMessage.toString());

        }

    }

    private int getCustomerWarehouseID(Random gen, int w_id, int numWarehouses, int x) {
        int customerWarehouseID;
        if (x <= 85) {
            customerWarehouseID = w_id;
        } else {
            do {
                customerWarehouseID = mTPCCUtil.randomNumber(1, numWarehouses, gen);
            }
            while (customerWarehouseID == w_id && numWarehouses > 1);
        }
        return customerWarehouseID;
    }

    private int getCustomerDistrictId(Random gen, int districtID, int x) {
        if (x <= 85) {
            return districtID;
        } else {
            return mTPCCUtil.randomNumber(1, mTPCCConfig.configDistPerWhse, gen);
        }


    }

    private void updateWarehouse(Connection conn, int w_id, float paymentAmount,mTPCCWorker w) throws SQLException {
        try (PreparedStatement payUpdateWhse = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayUpdateWhse"))) {
            w.getVaryBase().setPayUpdateWhseValue(payUpdateWhse,BigDecimal.valueOf(paymentAmount),w_id);
            // MySQL reports deadlocks due to lock upgrades:
            // t1: read w_id = x; t2: update w_id = x; t1 update w_id = x
            int result = payUpdateWhse.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("W_ID=" + w_id + " not found!");
            }
        }
    }

    private Warehouse getWarehouse(Connection conn, int w_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement payGetWhse = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayGetWhse"))) {
            w.getVaryBase().setPayGetWhseValue(payGetWhse,w_id);
            try (ResultSet rs = payGetWhse.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("W_ID=" + w_id + " not found!");
                }

                Warehouse warehouse = new Warehouse();
                warehouse.w_street_1 = rs.getString("W_STREET_1");
                warehouse.w_street_2 = rs.getString("W_STREET_2");
                warehouse.w_city = rs.getString("W_CITY");
                warehouse.w_state = rs.getString("W_STATE");
                warehouse.w_zip = rs.getString("W_ZIP");
                warehouse.w_name = rs.getString("W_NAME");

                return warehouse;
            }
        }
    }

    private Customer getCustomer(Connection conn, Random gen, int customerDistrictID, int customerWarehouseID, float paymentAmount, mTPCCWorker w) throws SQLException {
        int y = mTPCCUtil.randomNumber(1, 100, gen);

        Customer c;

        if (y <= 60) {
            // 60% lookups by last name
            c = getCustomerByName(customerWarehouseID, customerDistrictID, mTPCCUtil.getNonUniformRandomLastNameForRun(gen), conn, w);
        } else {
            // 40% lookups by customer ID
            c = getCustomerById(customerWarehouseID, customerDistrictID, mTPCCUtil.getCustomerID(gen), conn, w);
        }

        c.c_balance -= paymentAmount;
        c.c_ytd_payment += paymentAmount;
        c.c_payment_cnt += 1;

        return c;
    }

    private void updateDistrict(Connection conn, int w_id, int districtID, float paymentAmount, mTPCCWorker w) throws SQLException {
        try (PreparedStatement payUpdateDist = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayUpdateDist"))) {
            w.getVaryBase().setPayUpdateDistValue(payUpdateDist,BigDecimal.valueOf(paymentAmount),w_id,districtID);

            int result = payUpdateDist.executeUpdate();

            if (result == 0) {
                throw new RuntimeException("D_ID=" + districtID + " D_W_ID=" + w_id + " not found!");
            }
        }
    }

    private District getDistrict(Connection conn, int w_id, int districtID, mTPCCWorker w) throws SQLException {
        try (PreparedStatement payGetDist = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayGetDist"))) {
            w.getVaryBase().setPayGetDistValue(payGetDist,w_id,districtID);

            try (ResultSet rs = payGetDist.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("D_ID=" + districtID + " D_W_ID=" + w_id + " not found!");
                }

                District d = new District();
                d.d_street_1 = rs.getString("D_STREET_1");
                d.d_street_2 = rs.getString("D_STREET_2");
                d.d_city = rs.getString("D_CITY");
                d.d_state = rs.getString("D_STATE");
                d.d_zip = rs.getString("D_ZIP");
                d.d_name = rs.getString("D_NAME");

                return d;
            }
        }
    }

    private String getCData(Connection conn, int w_id, int districtID, int customerDistrictID, int customerWarehouseID, float paymentAmount, Customer c, mTPCCWorker w) throws SQLException {

        try (PreparedStatement payGetCustCdata = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayGetCustCdata"))) {
            String c_data;
            w.getVaryBase().setPayGetCustCdataValue(payGetCustCdata,customerWarehouseID,customerDistrictID,c.c_id);
            try (ResultSet rs = payGetCustCdata.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("C_ID=" + c.c_id + " C_W_ID=" + customerWarehouseID + " C_D_ID=" + customerDistrictID + " not found!");
                }
                c_data = rs.getString("C_DATA");
            }

            c_data = c.c_id + " " + customerDistrictID + " " + customerWarehouseID + " " + districtID + " " + w_id + " " + paymentAmount + " | " + c_data;
            if (c_data.length() > 500) {
                c_data = c_data.substring(0, 500);
            }

            return c_data;
        }

    }

    private void updateBalanceCData(Connection conn, int customerDistrictID, int customerWarehouseID, Customer c, mTPCCWorker w) throws SQLException {
        try (PreparedStatement payUpdateCustBalCdata = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayUpdateCustBalCdata"))) {
            w.getVaryBase().setPayUpdateCustBalCdataValue(payUpdateCustBalCdata,c.c_balance,c.c_ytd_payment,c.c_payment_cnt,c.c_data,customerWarehouseID,customerDistrictID,c.c_id);

            int result = payUpdateCustBalCdata.executeUpdate();

            if (result == 0) {
                throw new RuntimeException("Error in PYMNT Txn updating Customer C_ID=" + c.c_id + " C_W_ID=" + customerWarehouseID + " C_D_ID=" + customerDistrictID);
            }
        }
    }

    private void updateBalance(Connection conn, int customerDistrictID, int customerWarehouseID, Customer c,mTPCCWorker w) throws SQLException {

        try (PreparedStatement payUpdateCustBal = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayUpdateCustBal"))) {
            w.getVaryBase().setPayUpdateCustBalValue(payUpdateCustBal,c.c_balance,c.c_ytd_payment,c.c_payment_cnt,customerWarehouseID,customerDistrictID,c.c_id);

            int result = payUpdateCustBal.executeUpdate();

            if (result == 0) {
                throw new RuntimeException("C_ID=" + c.c_id + " C_W_ID=" + customerWarehouseID + " C_D_ID=" + customerDistrictID + " not found!");
            }
        }
    }

    private void insertHistory(Connection conn, int w_id, int districtID, int customerDistrictID, int customerWarehouseID, float paymentAmount, String w_name, String d_name, Customer c, mTPCCWorker w) throws SQLException {
        if (w_name.length() > 10) {
            w_name = w_name.substring(0, 10);
        }
        if (d_name.length() > 10) {
            d_name = d_name.substring(0, 10);
        }
        String h_data = w_name + "    " + d_name;

        try (PreparedStatement payInsertHist = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayInsertHist"))) {
            w.getVaryBase().setPayInsertHistValue(payInsertHist,customerDistrictID,customerWarehouseID,c.c_id,districtID,w_id,new Timestamp(System.currentTimeMillis()),paymentAmount,h_data);
            payInsertHist.executeUpdate();
        }
    }

    // attention duplicated code across trans... ok for now to maintain separate
    // prepared statements
    public Customer getCustomerById(int c_w_id, int c_d_id, int c_id, Connection conn,mTPCCWorker w) throws SQLException {

        try (PreparedStatement payGetCust = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("PayGetCust"))) {
            w.getVaryBase().setPayGetCustValue(payGetCust,c_w_id,c_d_id,c_id);

            try (ResultSet rs = payGetCust.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("C_ID=" + c_id + " C_D_ID=" + c_d_id + " C_W_ID=" + c_w_id + " not found!");
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
    public Customer getCustomerByName(int c_w_id, int c_d_id, String customerLastName, Connection conn, mTPCCWorker w) throws SQLException {
        ArrayList<Customer> customers = new ArrayList<>();

        try (PreparedStatement customerByName = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("CustomerByName"))) {
            w.getVaryBase().setCustomerByNameValue(customerByName,c_w_id,c_d_id,customerLastName);

            try (ResultSet rs = customerByName.executeQuery()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("C_LAST={} C_D_ID={} C_W_ID={}", customerLastName, c_d_id, c_w_id);
                }

                while (rs.next()) {
                    Customer c = mTPCCUtil.newCustomerFromResults(rs);
                    c.c_id = rs.getInt("C_ID");
                    c.c_last = customerLastName;
                    customers.add(c);
                }
            }
        }

        if (customers.size() == 0) {
            throw new RuntimeException("C_LAST=" + customerLastName + " C_D_ID=" + c_d_id + " C_W_ID=" + c_w_id + " not found!");
        }

        // TPC-C 2.5.2.2: Position n / 2 rounded up to the next integer, but
        // that
        // counts starting from 1.
        int index = customers.size() / 2;
        if (customers.size() % 2 == 0) {
            index -= 1;
        }
        return customers.get(index);
    }




//
//    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {
//
//        // initializing all prepared statements
//        // Payment Txn
//        payUpdateWhse = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayUpdateWhse")));
//        payGetWhse = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayGetWhse")));
//        payUpdateDist = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayUpdateDist")));
//        payGetDist = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayGetDist")));
//        payGetCust = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayGetCust")));
//        payGetCustCdata = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayGetCustCdata")));
//        payUpdateCustBalCdata = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayUpdateCustBalCdata")));
//        payUpdateCustBal = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayUpdateCustBal")));
//        payInsertHist = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("PayInsertHist")));
//        customerByName = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("CustomerByName")));
//
//            // payUpdateWhse =this.getPreparedStatement(conn, payUpdateWhseSQL);
//            int districtID = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);
//            int customerID = mTPCCUtil.getCustomerID(gen);
//
//            int x = mTPCCUtil.randomNumber(1, 100, gen);
//            int customerDistrictID;
//            int customerWarehouseID;
//            if (x <= 85) {
//                customerDistrictID = districtID;
//                customerWarehouseID = w_id;
//            } else {
//                customerDistrictID = mTPCCUtil.randomNumber(1, mTPCCConfig.configDistPerWhse, gen);
//                do {
//                    customerWarehouseID = mTPCCUtil.randomNumber(1, numWarehouses, gen);
//                }
//                while (customerWarehouseID == w_id && numWarehouses > 1);
//            }
//
//            long y = mTPCCUtil.randomNumber(1, 100, gen);
//            boolean customerByName;
//            String customerLastName = null;
//            customerID = -1;
//            if (y <= 60) {
//                // 60% lookups by last name
//                customerByName = true;
//                customerLastName = mTPCCUtil.getNonUniformRandomLastNameForRun(gen);
//            } else {
//                // 40% lookups by customer ID
//                customerByName = false;
//                customerID = mTPCCUtil.getCustomerID(gen);
//            }
//
//            float paymentAmount = (float) (mTPCCUtil.randomNumber(100, 500000, gen) / 100.0);
//
//            String w_street_1, w_street_2, w_city, w_state, w_zip, w_name;
//            String d_street_1, d_street_2, d_city, d_state, d_zip, d_name;
//
//
//            // MySQL reports deadlocks due to lock upgrades:
//            // t1: read w_id = x; t2: update w_id = x; t1 update w_id = x
//            w.getVaryBase().setPayUpdateWhseValue(payUpdateWhse,BigDecimal.valueOf(paymentAmount),w_id);
//            int result = payUpdateWhse.executeUpdate();
//            if (result == 0) {
//                throw new RuntimeException("W_ID=" + w_id + " not found!");
//            }
//
//        w.getVaryBase().setGetWhseValue(payGetWhse,w_id);
//            try (ResultSet rs = payGetWhse.executeQuery()) {
//                if (!rs.next()) {
//                    throw new RuntimeException("W_ID=" + w_id + " not found!");
//                }
//                w_street_1 = rs.getString("W_STREET_1");
//                w_street_2 = rs.getString("W_STREET_2");
//                w_city = rs.getString("W_CITY");
//                w_state = rs.getString("W_STATE");
//                w_zip = rs.getString("W_ZIP");
//                w_name = rs.getString("W_NAME");
//            }
//
//        w.getVaryBase().setPayUpdateDistValue(payUpdateDist,BigDecimal.valueOf(paymentAmount),w_id,districtID);
//            result = payUpdateDist.executeUpdate();
//            if (result == 0) {
//                throw new RuntimeException("D_ID=" + districtID + " D_W_ID=" + w_id + " not found!");
//            }
//
//        w.getVaryBase().setPayGetDistValue(payGetDist,w_id,districtID);
//            try (ResultSet rs = payGetDist.executeQuery()) {
//                if (!rs.next()) {
//                    throw new RuntimeException("D_ID=" + districtID + " D_W_ID=" + w_id + " not found!");
//                }
//                d_street_1 = rs.getString("D_STREET_1");
//                d_street_2 = rs.getString("D_STREET_2");
//                d_city = rs.getString("D_CITY");
//                d_state = rs.getString("D_STATE");
//                d_zip = rs.getString("D_ZIP");
//                d_name = rs.getString("D_NAME");
//            }
//
//            Customer c;
//            if (customerByName) {
//
//                c = getCustomerByName(customerWarehouseID, customerDistrictID, customerLastName, conn,w);
//            } else {
//
//                c = getCustomerById(customerWarehouseID, customerDistrictID, customerID, conn,w);
//            }
//            //to do:add subsidy
//            c.c_balance -= paymentAmount;
//            c.c_ytd_payment += paymentAmount;
//            c.c_payment_cnt += 1;
//            String c_data = null;
//            if (c.c_credit.equals("BC")) { // bad credit
//                w.getVaryBase().setPayGetCustCdataValue(payGetCustCdata,customerWarehouseID,customerDistrictID,c.c_id);
//                try (ResultSet rs = payGetCustCdata.executeQuery()) {
//                    if (!rs.next()) {
//                        throw new RuntimeException("C_ID=" + c.c_id + " C_W_ID=" + customerWarehouseID + " C_D_ID=" + customerDistrictID + " not found!");
//                    }
//                    c_data = rs.getString("C_DATA");
//                }
//
//                c_data = c.c_id + " " + customerDistrictID + " " + customerWarehouseID + " " + districtID + " " + w_id + " " + paymentAmount + " | " + c_data;
//                if (c_data.length() > 500) {
//                    c_data = c_data.substring(0, 500);
//                }
//
//                w.getVaryBase().setPayUpdateCustBalCdataValue(payUpdateCustBalCdata,c.c_balance,c.c_ytd_payment,c.c_payment_cnt,c_data,customerWarehouseID,customerDistrictID,c.c_id);
//                result = payUpdateCustBalCdata.executeUpdate();
//
//                if (result == 0) {
//                    throw new RuntimeException("Error in PYMNT Txn updating Customer C_ID=" + c.c_id + " C_W_ID=" + customerWarehouseID + " C_D_ID=" + customerDistrictID);
//                }
//
//            } else { // GoodCredit
//
//                w.getVaryBase().setPayUpdateCustBalValue(payUpdateCustBal,c.c_balance,c.c_ytd_payment,c.c_payment_cnt,customerWarehouseID,customerDistrictID,c.c_id);
//                result = payUpdateCustBal.executeUpdate();
//
//                if (result == 0) {
//                    throw new RuntimeException("C_ID=" + c.c_id + " C_W_ID=" + customerWarehouseID + " C_D_ID=" + customerDistrictID + " not found!");
//                }
//
//            }
//
//            if (w_name.length() > 10) {
//                w_name = w_name.substring(0, 10);
//            }
//            if (d_name.length() > 10) {
//                d_name = d_name.substring(0, 10);
//            }
//            String h_data = w_name + "    " + d_name;
//
//            w.getVaryBase().setPayInsertHistValue(payInsertHist,customerDistrictID,customerWarehouseID,c.c_id,districtID,w_id,new Timestamp(System.currentTimeMillis()),paymentAmount,h_data);
//            payInsertHist.executeUpdate();
//
//            //conn.commit();
//
//            if (LOG.isTraceEnabled()) {
//                StringBuilder terminalMessage = new StringBuilder();
//                terminalMessage.append("\n+---------------------------- PAYMENT ----------------------------+");
//                terminalMessage.append("\n Date: ").append(mTPCCUtil.getCurrentTime());
//                terminalMessage.append("\n\n Warehouse: ");
//                terminalMessage.append(w_id);
//                terminalMessage.append("\n   Street:  ");
//                terminalMessage.append(w_street_1);
//                terminalMessage.append("\n   Street:  ");
//                terminalMessage.append(w_street_2);
//                terminalMessage.append("\n   City:    ");
//                terminalMessage.append(w_city);
//                terminalMessage.append("   State: ");
//                terminalMessage.append(w_state);
//                terminalMessage.append("  Zip: ");
//                terminalMessage.append(w_zip);
//                terminalMessage.append("\n\n District:  ");
//                terminalMessage.append(districtID);
//                terminalMessage.append("\n   Street:  ");
//                terminalMessage.append(d_street_1);
//                terminalMessage.append("\n   Street:  ");
//                terminalMessage.append(d_street_2);
//                terminalMessage.append("\n   City:    ");
//                terminalMessage.append(d_city);
//                terminalMessage.append("   State: ");
//                terminalMessage.append(d_state);
//                terminalMessage.append("  Zip: ");
//                terminalMessage.append(d_zip);
//                terminalMessage.append("\n\n Customer:  ");
//                terminalMessage.append(c.c_id);
//                terminalMessage.append("\n   Name:    ");
//                terminalMessage.append(c.c_first);
//                terminalMessage.append(" ");
//                terminalMessage.append(c.c_middle);
//                terminalMessage.append(" ");
//                terminalMessage.append(c.c_last);
//                terminalMessage.append("\n   Street:  ");
//                terminalMessage.append(c.c_street_1);
//                terminalMessage.append("\n   Street:  ");
//                terminalMessage.append(c.c_street_2);
//                terminalMessage.append("\n   City:    ");
//                terminalMessage.append(c.c_city);
//                terminalMessage.append("   State: ");
//                terminalMessage.append(c.c_state);
//                terminalMessage.append("  Zip: ");
//                terminalMessage.append(c.c_zip);
//                terminalMessage.append("\n   Since:   ");
//                if (c.c_since != null) {
//                    terminalMessage.append(c.c_since.toString());
//                } else {
//                    terminalMessage.append("");
//                }
//                terminalMessage.append("\n   Credit:  ");
//                terminalMessage.append(c.c_credit);
//                terminalMessage.append("\n   %Disc:   ");
//                terminalMessage.append(c.c_discount);
//                terminalMessage.append("\n   Phone:   ");
//                terminalMessage.append(c.c_phone);
//                terminalMessage.append("\n\n Amount Paid:      ");
//                terminalMessage.append(paymentAmount);
//                terminalMessage.append("\n Credit Limit:     ");
//                terminalMessage.append(c.c_credit_lim);
//                terminalMessage.append("\n New Cust-Balance: ");
//                terminalMessage.append(c.c_balance);
//                if (c.c_credit.equals("BC")) {
//                    if (c_data.length() > 50) {
//                        terminalMessage.append("\n\n Cust-Data: ").append(c_data.substring(0, 50));
//                        int data_chunks = c_data.length() > 200 ? 4 : c_data.length() / 50;
//                        for (int n = 1; n < data_chunks; n++) {
//                            terminalMessage.append("\n            ").append(c_data.substring(n * 50, (n + 1) * 50));
//                        }
//                    } else {
//                        terminalMessage.append("\n\n Cust-Data: ").append(c_data);
//                    }
//                }
//                terminalMessage.append("\n+-----------------------------------------------------------------+\n\n");
//
//                LOG.trace(terminalMessage.toString());
//            }
//
//
//    }
//
//    // attention duplicated code across trans... ok for now to maintain separate
//    // prepared statements
//    public Customer getCustomerById(int c_w_id, int c_d_id, int c_id, Connection conn, mTPCCWorker w) throws SQLException {
//
//
//
//
//        w.getVaryBase().setPayGetCustValue(payGetCust,c_w_id,c_d_id,c_id);
//            try (ResultSet rs = payGetCust.executeQuery()) {
//                if (!rs.next()) {
//                    throw new RuntimeException("C_ID=" + c_id + " C_D_ID=" + c_d_id + " C_W_ID=" + c_w_id + " not found!");
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
//    public Customer getCustomerByName(int c_w_id, int c_d_id, String customerLastName, Connection conn, mTPCCWorker w) throws SQLException {
//        ArrayList<Customer> customers = new ArrayList<>();
//        w.getVaryBase().setCustomerByNameValue(customerByName,c_w_id,c_d_id,customerLastName);
//            try (ResultSet rs = customerByName.executeQuery()) {
//                if (LOG.isTraceEnabled()) {
//                    LOG.trace("C_LAST={} C_D_ID={} C_W_ID={}", customerLastName, c_d_id, c_w_id);
//                }
//
//                while (rs.next()) {
//                    Customer c = mTPCCUtil.newCustomerFromResults(rs);
//                    c.c_id = rs.getInt("C_ID");
//                    c.c_last = customerLastName;
//                    customers.add(c);
//                }
//
//        }
//
//        if (customers.size() == 0) {
//            throw new RuntimeException("C_LAST=" + customerLastName + " C_D_ID=" + c_d_id + " C_W_ID=" + c_w_id + " not found!");
//        }
//
//        // TPC-C 2.5.2.2: Position n / 2 rounded up to the next integer, but
//        // that
//        // counts starting from 1.
//        int index = customers.size() / 2;
//        if (customers.size() % 2 == 0) {
//            index -= 1;
//        }
//        return customers.get(index);
//    }


}
