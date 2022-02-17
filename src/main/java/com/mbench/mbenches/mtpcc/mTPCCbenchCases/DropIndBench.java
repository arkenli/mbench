package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class DropIndBench extends VaryBase{
    public DropIndBench() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }


}
