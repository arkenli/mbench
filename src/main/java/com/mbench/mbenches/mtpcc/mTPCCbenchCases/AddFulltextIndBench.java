package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class AddFulltextIndBench extends VaryBase{
    public AddFulltextIndBench() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }

}
