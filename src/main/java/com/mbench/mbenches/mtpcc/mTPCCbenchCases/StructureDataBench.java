package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class StructureDataBench extends VaryBase{

    public StructureDataBench() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }


}
