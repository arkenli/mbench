package com.mbench.mbenches.mtpcc;


import com.mbench.mbenches.mtpcc.mTPCCbenchCases.*;

import java.sql.Connection;
import java.util.Random;

public class mTPCCBenchmarkCaseFactory {
    public static VaryBase getTargetBenchmark(String benchName) {
        return switch (benchName) {
            case "addColumn" -> new AddColumnBench();
            case "deleteColumn" -> new DeleteColumnBench();
            case "original" -> new NoneChange();
            case "addForeCons" -> new AddForeConsBench();
            case "addFullTextInd" -> new AddFulltextIndBench();
            case "changeColType" -> new ChangeColTypeBench();
            case "createInd" -> new CreateIndBench();
            case "dropForeCons" -> new DropForeConsBench();
            case "renameColumn" -> new RenameColumnBench();
            case "renameTable" -> new RenameTableColumn();
            case "serDefaultValue" -> new SetDefaultValueBench();
            case "structureData" -> new StructureDataBench();
            default -> null;
        };

    }
}
