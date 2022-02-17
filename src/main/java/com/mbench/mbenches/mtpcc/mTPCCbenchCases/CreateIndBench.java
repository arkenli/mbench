package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class CreateIndBench extends VaryBase{
    public CreateIndBench() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }

}
