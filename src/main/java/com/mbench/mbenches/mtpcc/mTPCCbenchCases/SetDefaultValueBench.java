package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class SetDefaultValueBench extends VaryBase{

    public SetDefaultValueBench() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }


}
