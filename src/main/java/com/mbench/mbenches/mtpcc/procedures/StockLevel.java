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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class StockLevel extends VaryProcedure {

    private static final Logger LOG = LoggerFactory.getLogger(StockLevel.class);
//    private PreparedStatement stockGetDistOrderId = null;
//    private PreparedStatement stockGetCountStock = null;

    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {

        int threshold = mTPCCUtil.randomNumber(10, 20, gen);
        int d_id = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);

        int o_id = getOrderId(conn, w_id, d_id, w);

        int stock_count = getStockCount(conn, w_id, threshold, d_id, o_id, w);

        if (LOG.isTraceEnabled()) {
            String terminalMessage = "\n+-------------------------- STOCK-LEVEL --------------------------+" +
                    "\n Warehouse: " +
                    w_id +
                    "\n District:  " +
                    d_id +
                    "\n\n Stock Level Threshold: " +
                    threshold +
                    "\n Low Stock Count:       " +
                    stock_count +
                    "\n+-----------------------------------------------------------------+\n\n";
            LOG.trace(terminalMessage);
        }


    }

    private int getOrderId(Connection conn, int w_id, int d_id,mTPCCWorker w) throws SQLException {
        try (PreparedStatement stockGetDistOrderId = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("StockGetDistOrderId"))) {
            w.getVaryBase().setStockGetDistOrderIdValue(stockGetDistOrderId,w_id,d_id);

            try (ResultSet rs = stockGetDistOrderId.executeQuery()) {

                if (!rs.next()) {
                    throw new RuntimeException("D_W_ID=" + w_id + " D_ID=" + d_id + " not found!");
                }
                return rs.getInt("D_NEXT_O_ID");
            }
        }

    }

    private int getStockCount(Connection conn, int w_id, int threshold, int d_id, int o_id, mTPCCWorker w) throws SQLException {
        try (PreparedStatement stockGetCountStock = this.getPreparedStatement(conn, w.getVaryBase().getSQLStmt("StockGetCountStock"))) {
            w.getVaryBase().setStockGetCountStockValue(stockGetCountStock,w_id,d_id,o_id,o_id-20,w_id,threshold);

            try (ResultSet rs = stockGetCountStock.executeQuery()) {
                if (!rs.next()) {
                    String msg = String.format("Failed to get StockLevel result for COUNT query [W_ID=%d, D_ID=%d, O_ID=%d]", w_id, d_id, o_id);

                    throw new RuntimeException(msg);
                }

                return rs.getInt("STOCK_COUNT");
            }
        }
    }

//    public void run(Connection conn, Random gen, int w_id, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException {
//
//        boolean trace = LOG.isTraceEnabled();
//
//        stockGetDistOrderId = this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("StockGetDistOrderId")));
//        stockGetCountStock= this.getPreparedStatement(conn, new SQLStmt(w.getVaryBase().getSQLString("StockGetCountStock")));
//            int threshold = mTPCCUtil.randomNumber(10, 20, gen);
//            int d_id = mTPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);
//
//            int o_id;
//            // XXX int i_id = 0;
//            int stock_count;
//
//
//            if (trace) {
//                LOG.trace(String.format("stockGetDistOrderId BEGIN [W_ID=%d, D_ID=%d]", w_id, d_id));
//            }
//         w.getVaryBase().setStockGetDistOrderIdValue(stockGetDistOrderId,w_id,d_id);
//            try (ResultSet rs =stockGetDistOrderId.executeQuery()) {
//                if (trace) {
//                    LOG.trace("stockGetDistOrderId END");
//                }
//
//                if (!rs.next()) {
//                    throw new RuntimeException("D_W_ID=" + w_id + " D_ID=" + d_id + " not found!");
//                }
//                o_id = rs.getInt("D_NEXT_O_ID");
//            }
//
//
//            if (trace) {
//                LOG.trace(String.format("stockGetCountStock BEGIN [W_ID=%d, D_ID=%d, O_ID=%d]", w_id, d_id, o_id));
//            }
//        w.getVaryBase().setStockGetCountStockValue(stockGetCountStock,w_id,d_id,o_id,o_id-20,w_id,threshold);
//            try (ResultSet rs = stockGetCountStock.executeQuery()) {
//                if (trace) {
//                    LOG.trace("stockGetCountStock END");
//                }
//
//                if (!rs.next()) {
//                    String msg = String.format("Failed to get StockLevel result for COUNT query " +
//                            "[W_ID=%d, D_ID=%d, O_ID=%d]", w_id, d_id, o_id);
//                    if (trace) {
//                        LOG.warn(msg);
//                    }
//                    throw new RuntimeException(msg);
//                }
//                stock_count = rs.getInt("STOCK_COUNT");
//                if (trace) {
//                    LOG.trace("stockGetCountStock RESULT={}", stock_count);
//                }
//
//            }
//
//            if (trace) {
//                String terminalMessage = "\n+-------------------------- STOCK-LEVEL --------------------------+" +
//                        "\n Warehouse: " +
//                        w_id +
//                        "\n District:  " +
//                        d_id +
//                        "\n\n Stock Level Threshold: " +
//                        threshold +
//                        "\n Low Stock Count:       " +
//                        stock_count +
//                        "\n+-----------------------------------------------------------------+\n\n";
//                LOG.trace(terminalMessage);
//            }
//
//    }
}
