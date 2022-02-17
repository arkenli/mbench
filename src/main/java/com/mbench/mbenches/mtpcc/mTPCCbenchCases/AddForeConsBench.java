package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class AddForeConsBench extends VaryBase{
    public AddForeConsBench() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }


}
