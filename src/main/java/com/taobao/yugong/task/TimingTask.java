package com.taobao.yugong.task;

import com.taobao.yugong.YuGongLauncher;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;


public class TimingTask {
    private static final Logger logger = LoggerFactory.getLogger(TimingTask.class);
    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    public TimingTask(){
        /*Date time = getTime();
        System.out.println("指定时间time=" + time);
        timer = new Timer();
        timer.schedule(new SubTask(), time);*/
        TRun();
    }

   /* Timer timer;
    public Date getTime(){
        Date time=null;
        try {
            String conf = System.getProperty("yugong.conf", "classpath:yugong.properties");
            PropertiesConfiguration config = new PropertiesConfiguration();
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                config.load(TimingTask.class.getClassLoader().getResourceAsStream(conf));
            } else {
                config.load(new FileInputStream(conf));
            }
            int hour=16;
            int min=00;
            int second=00;
            String exeTime= StringUtils.upperCase(config.getString("yugong.execution.time"));
            if(exeTime!=null){
                String[] arrTime=exeTime.split(":");
                if(arrTime.length==3) {
                    hour = Integer.valueOf(arrTime[0]);
                    min=Integer.valueOf(arrTime[1]);
                    second=Integer.valueOf(arrTime[2]);
                }
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            calendar.set(Calendar.SECOND, second);
            time = calendar.getTime();
        }catch (Exception ex)
        {
            logger.error("get run time error->"+ex.toString());
        }
        return time;
    }*/
   public static String getConfigValueStr(String keyName){
        String time="";
        try {
            String conf = System.getProperty("yugong.conf", "classpath:yugong.properties");
            PropertiesConfiguration config = new PropertiesConfiguration();
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                config.load(TimingTask.class.getClassLoader().getResourceAsStream(conf));
            } else {
                config.load(new FileInputStream(conf));
            }
            if(config!=null){
                String exeTime= StringUtils.upperCase(config.getString(keyName));
                if(exeTime!=null){
                    time=exeTime;
                }
            }
        }catch (Exception ex){
            logger.error("getRunTimeStr->"+ex.toString());
        }
        return time;
    }


    public static List<String> getConfigList(String keyName){
        List tablelist=null;
        try {
            String conf = System.getProperty("yugong.conf", "classpath:yugong.properties");
            PropertiesConfiguration config = new PropertiesConfiguration();
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                config.load(TimingTask.class.getClassLoader().getResourceAsStream(conf));
            } else {
                config.load(new FileInputStream(conf));
            }
            if(config!=null){
                tablelist = config.getList("yugong.table.white");
            }
        }catch (Exception ex){
            logger.error("getRunTimeStr->"+ex.toString());
        }
        return tablelist;
    }

    private void TRun() {
        long dayInterval = 24 * 60 * 60 * 1000;
        String exeTimeStr="04:10:00";
        String exeTimeTemp=getConfigValueStr("yugong.execution.time");
        if(exeTimeTemp!=null)
            exeTimeStr=exeTimeTemp;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd "+exeTimeStr);
        try {
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if (System.currentTimeMillis() > startTime.getTime()){
                startTime = new Date(startTime.getTime() + dayInterval);
            }
            Timer t = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    //execution extraction
                    logger.info("start execution extraction...");
                    YuGongLauncher.init();
                }
            };
            t.schedule(task, startTime, dayInterval);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cleanHistory(){
        String path="../conf/positioner";
        File file = new File(path);
        if(file.exists()){
            int del=0;
            File[] subFiles=file.listFiles();
            for(File sFile:subFiles) {
                if (sFile.isFile())
                    if (sFile.delete())
                        del++;
            }
            logger.info("successful clean up <"+del+"> files");
        }
        else
            logger.error("failed to clean history data.folder does not exist！！");
    }
}

