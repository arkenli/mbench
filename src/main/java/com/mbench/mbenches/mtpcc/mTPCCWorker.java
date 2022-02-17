package com.mbench.mbenches.mtpcc;

import com.mbench.api.Procedure;
import com.mbench.api.TransactionType;
import com.mbench.api.Worker;
import com.mbench.mbenches.mtpcc.procedures.VaryProcedure;
import com.mbench.Controllers.ChangeController;
import com.mbench.types.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public class mTPCCWorker extends Worker<mTPCCBench> {
    private static final Logger LOG = LoggerFactory.getLogger(mTPCCWorker.class);
    int terminalWarehouseID;
    int numWarehouses;
    private final int terminalDistrictLowerID;
    private final int terminalDistrictUpperID;
    private final Random gen = new Random();
    private boolean isChanged;
    public mTPCCWorker(mTPCCBench benchmarkModule, int id,
                       int terminalWarehouseID, int terminalDistrictLowerID,
                       int terminalDistrictUpperID, int numWarehouses)
            throws SQLException {
        super(benchmarkModule, id);

        this.terminalWarehouseID = terminalWarehouseID;
        this.terminalDistrictLowerID = terminalDistrictLowerID;
        this.terminalDistrictUpperID = terminalDistrictUpperID;

        this.numWarehouses = numWarehouses;
        this.isChanged=false;
        this.setVaryBase(mTPCCBenchmarkCaseFactory.getTargetBenchmark(getWorkloadConfiguration().getBenchCase()));
    }


    @Override
    protected TransactionStatus executeWork(Connection conn, TransactionType nextTransaction) throws Procedure.UserAbortException, SQLException {
        if (!isChanged&& ChangeController.workerNeedChange(getId())){
            nextStage();
            getVaryBase().applyChange();
            this.isChanged=true;
            LOG.info("Worker "+ getId() + "Changed");
        }
        try {
            VaryProcedure proc = (VaryProcedure) this.getProcedure(nextTransaction.getProcedureClass());
            proc.run(conn, gen, terminalWarehouseID, numWarehouses,
                    terminalDistrictLowerID, terminalDistrictUpperID, this);
        } catch (ClassCastException ex) {
            //fail gracefully
            LOG.error("We have been invoked with an INVALID transactionType?!", ex);
            throw new RuntimeException("Bad transaction type = " + nextTransaction);
        }
        return (TransactionStatus.SUCCESS);
    }
    public Random getGen(){
        return this.gen;
    }
    @Override
    protected long getPreExecutionWaitInMillis(TransactionType type) {
        // TPC-C 5.2.5.2: For keying times for each type of transaction.
        return type.getPreExecutionWait();
    }

    @Override
    protected long getPostExecutionWaitInMillis(TransactionType type) {
        // TPC-C 5.2.5.4: For think times for each type of transaction.
        long mean = type.getPostExecutionWait();

        float c = this.getBenchmarkModule().rng().nextFloat();
        long thinkTime = (long) (-1 * Math.log(c) * mean);
        if (thinkTime > 10 * mean) {
            thinkTime = 10 * mean;
        }

        return thinkTime;
    }
    public boolean isChanged(){
        return this.isChanged;
    }
}
