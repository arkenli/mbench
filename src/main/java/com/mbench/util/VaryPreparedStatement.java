package com.mbench.util;

import com.mbench.api.SQLStmt;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class VaryPreparedStatement {
    private PreparedStatement preparedStatement;
    private SQLStmt sqlStmt;
    private int indexNum=1;
    private int indexLimit=Integer.MAX_VALUE;
    Map<String,PreparedStatement> map=new HashMap<>();

    public VaryPreparedStatement(PreparedStatement preparedStatement){

        this.preparedStatement=preparedStatement;
        //this.indexLimit=countLimit();

    }
    public PreparedStatement getPreparedStatement(){
        return this.preparedStatement;
    }
    public boolean isFull(){
        return indexNum>indexLimit;
    }
    public void setPreparedStatement(PreparedStatement preparedStatement){
        this.preparedStatement=preparedStatement;
    }
    public void addIndexNum(int count){
        this.indexNum+=count;
    }

    public void setIndexNum(int indexNum){
        this.indexNum=indexNum;
    }
    public void clearIndex(){
        this.indexNum=1;
    }
    public void setValue(int value) throws SQLException {
        if (indexNum>indexLimit){
            return;
        }
        preparedStatement.setInt(indexNum,value);
        indexNum++;
    }
    public void setValue(String value) throws SQLException {
        if (indexNum>indexLimit){
            return;
        }
        preparedStatement.setString(indexNum,value);
        indexNum++;
    }

    public void setValue(float value) throws SQLException {
        if (indexNum>indexLimit){
            return;
        }
        preparedStatement.setFloat(indexNum,value);
        indexNum++;
    }
    public void setValue(Timestamp value) throws SQLException {
        if (indexNum>indexLimit){
            return;
        }
        preparedStatement.setTimestamp(indexNum,value);
        indexNum++;
    }
    public void setValue(double value) throws SQLException {
        if (indexNum>indexLimit){
            return;
        }
        preparedStatement.setDouble(indexNum,value);
        indexNum++;
    }
    public void setValue(BigDecimal value) throws SQLException{
        if (indexNum>indexLimit){
            return;
        }
        preparedStatement.setBigDecimal(indexNum,value);
        indexNum++;
    }
    private int countLimit(){
        int limit=0;
        for (char c:sqlStmt.getSQL().toCharArray()){
            if (c=='?')
                limit++;
        }
        return limit;
    }
    public int executeUpdate() throws SQLException {
        int res=preparedStatement.executeUpdate();
        return res;
    }
    public void executeBatch() throws SQLException {
        preparedStatement.executeBatch();
    }
    public ResultSet executeQuery() throws SQLException {
        ResultSet res=preparedStatement.executeQuery();
        return res;
    }
    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
        clearIndex();
    }
    public void clearBatch() throws SQLException {
        preparedStatement.clearBatch();

    }


}
