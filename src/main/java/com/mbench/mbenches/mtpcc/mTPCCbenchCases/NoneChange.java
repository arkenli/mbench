package com.mbench.mbenches.mtpcc.mTPCCbenchCases;

import java.sql.Connection;
import java.util.Random;

public class NoneChange extends VaryBase{
    public NoneChange() {
        super();
    }

    @Override
    public void applyChange() {
        stage++;
    }

}
