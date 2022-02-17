package com.mbench.mbenches.mtpcc;

import com.mbench.WorkloadConfiguration;
import com.mbench.api.BenchmarkModule;

import com.mbench.api.Loader;
import com.mbench.api.Worker;
import com.mbench.mbenches.mtpcc.procedures.NewOrder;
import com.mbench.Controllers.ChangeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mTPCCBench extends BenchmarkModule {
    private static final Logger LOG = LoggerFactory.getLogger(mTPCCBench.class);
    public mTPCCBench(WorkloadConfiguration workConf) {
        super(workConf);
    }

    @Override
    protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl() throws IOException {
        ArrayList<Worker<? extends BenchmarkModule>> workers = new ArrayList<>();

        try {
            List<mTPCCWorker> terminals = createTerminals();
            workers.addAll(terminals);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return workers;
    }

    @Override
    protected Loader<? extends BenchmarkModule> makeLoaderImpl() {
        return new mTPCCLoader(this);
    }

    @Override
    protected Package getProcedurePackageImpl() {
        return (NewOrder.class.getPackage());
    }

    protected ArrayList<mTPCCWorker> createTerminals() throws SQLException {

        mTPCCWorker[] terminals = new mTPCCWorker[workConf.getTerminals()];

        int numWarehouses = (int) workConf.getScaleFactor();//tpccConf.getNumWarehouses();
        if (numWarehouses <= 0) {
            numWarehouses = 1;
        }
        int numTerminals = workConf.getTerminals();

        final double terminalsPerWarehouse = (double) numTerminals
                / numWarehouses;
        int workerId = 0;

        for (int w = 0; w < numWarehouses; w++) {
            // Compute the number of terminals in *this* warehouse
            int lowerTerminalId = (int) (w * terminalsPerWarehouse);
            int upperTerminalId = (int) ((w + 1) * terminalsPerWarehouse);
            // protect against double rounding errors
            int w_id = w + 1;
            if (w_id == numWarehouses) {
                upperTerminalId = numTerminals;
            }
            int numWarehouseTerminals = upperTerminalId - lowerTerminalId;

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("w_id %d = %d terminals [lower=%d / upper%d]",
                        w_id, numWarehouseTerminals, lowerTerminalId, upperTerminalId));
            }

            final double districtsPerTerminal = mTPCCConfig.configDistPerWhse
                    / (double) numWarehouseTerminals;
            for (int terminalId = 0; terminalId < numWarehouseTerminals; terminalId++) {
                int lowerDistrictId = (int) (terminalId * districtsPerTerminal);
                int upperDistrictId = (int) ((terminalId + 1) * districtsPerTerminal);
                if (terminalId + 1 == numWarehouseTerminals) {
                    upperDistrictId = mTPCCConfig.configDistPerWhse;
                }
                lowerDistrictId += 1;
//                ChangeConfig workerConfig=new ChangeConfig(workloadConfiguration.getTerminalTimeList().get(timeIndex),workloadConfiguration.getTerminalChangeList().get(timeIndex));
                ChangeController.setWorkerChangeTime(workerId);
                mTPCCWorker terminal = new mTPCCWorker(this, workerId++,
                        w_id, lowerDistrictId, upperDistrictId,numWarehouses);

                terminals[lowerTerminalId + terminalId] = terminal;
            }

        }


        return new ArrayList<>(Arrays.asList(terminals));
    }

}
