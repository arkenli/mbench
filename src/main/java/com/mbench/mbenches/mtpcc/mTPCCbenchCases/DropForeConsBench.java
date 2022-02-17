package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class DropForeConsBench extends VaryBase{
    public DropForeConsBench() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }


}
