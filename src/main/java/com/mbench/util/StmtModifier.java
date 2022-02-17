package com.mbench.util;


import com.mbench.api.SQLStmt;

public class StmtModifier {
    public static SQLStmt renameColumn(SQLStmt origin, String a, String b){
        return new SQLStmt(origin.getSQL().replaceAll(a,b));
    }
    public static SQLStmt addColumnOnSelect(SQLStmt origin, String columnName,String newColumn){
        String str=origin.getSQL();
        int index=str.indexOf("FROM")-1;
        String newStr=str.substring(0,index)+","+newColumn+" "+str.substring(index);
        return new SQLStmt(newStr);

    }

    //only support for equals now
    public static <T> SQLStmt addColumnOnWhere(SQLStmt origin, String columnName, T value){
        String str=origin.getSQL();
        int index=str.indexOf("WHERE");
        String newStr;
        if (index==-1){
            newStr=str+" WHERE "+columnName+" = "+value.toString();
        }
        else {
            index += 5;
            newStr = str.substring(0, index) + " " + columnName + " = " + value.toString() +", ";
        }
        return new SQLStmt(newStr);

    }
    public static <T> SQLStmt addColumnOnSet(SQLStmt origin, String columnName, T value){
        String str=origin.getSQL();
        int index=str.indexOf("WHERE")-1;
        String newStr=str.substring(0,index)+columnName+" = "+value.toString()+" ";
        return new SQLStmt(newStr);

    }
    public static <T> SQLStmt addColumnOnInsert(SQLStmt origin, String columnName,T value){
        String str=origin.getSQL();
        int index=str.indexOf(")");
        String newStr;
        newStr=str.substring(0,index)+", "+columnName+str.substring(index);
        index=newStr.lastIndexOf(")");
        return new SQLStmt(newStr.substring(0,index)+","+value+newStr.substring(index));
    }
    public static SQLStmt dropColumnOnSelect(SQLStmt origin, String columnName){
        String str=origin.getSQL();
        int index=str.indexOf(columnName);
        return new SQLStmt(str.substring(0,index)+str.substring(index+1+columnName.length()));
    }
    public static SQLStmt dropColumnOnInsert(SQLStmt origin, String columnName){
        String str=origin.getSQL();
        int index=str.indexOf(columnName);
        String newStr;
        if (str.charAt(index+columnName.length()+1)==',') {
            newStr=str.substring(0,index)+str.substring(index+columnName.length()+1);
        }else {
            newStr=str.substring(0,index)+str.substring(index+columnName.length());
        }
        return new SQLStmt(newStr);
    }
    public static SQLStmt dropColumn(SQLStmt origin, String columnName){
        return null;
    }
}
