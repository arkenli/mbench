package com.mbench.mbenches.mtpcc.procedures;



import com.mbench.api.Procedure;
import com.mbench.mbenches.mtpcc.mTPCCWorker;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public abstract class VaryProcedure extends Procedure {

    public abstract void run(Connection conn, Random gen, int terminalWarehouseID, int numWarehouses, int terminalDistrictLowerID, int terminalDistrictUpperID, mTPCCWorker w) throws SQLException;
}
