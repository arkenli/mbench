package com.mbench.Controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeController {
    private static final Logger LOG = LoggerFactory.getLogger(ChangeController.class);
    private static long startTime;
    private static List<Integer> time_list;
    private static List<Integer> count_list;
    private static int planIndex=0;

    private static Map<Integer,Long> workerChangePlan=new HashMap<>();



    public static boolean workerNeedChange(int id){
        long runTime=System.currentTimeMillis()-startTime;
        return runTime >= workerChangePlan.get(id);

    }
    public static void setWorkerChangeTime(int id){
        workerChangePlan.put(id,(long)time_list.get(planIndex));
        int now_num=count_list.get(planIndex)-1;
        if (now_num==0){
            planIndex++;
        }else {
            count_list.set(planIndex,now_num);
        }
        LOG.info("The worker "+ id +"will be changed "+ workerChangePlan.get(id) +" seconds after the end of the loading phase.");
    }

    public static void startTimer(){
        startTime=System.currentTimeMillis();
    }


    public static void setTimeList(List<Integer> list){
        time_list=list;
    }

    public static void setCountList(List<Integer> list){
        count_list=list;
    }

}
