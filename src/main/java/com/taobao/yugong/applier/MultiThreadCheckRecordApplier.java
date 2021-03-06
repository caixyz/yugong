package com.taobao.yugong.applier;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.MDC;

import com.taobao.yugong.common.YuGongConstants;
import com.taobao.yugong.common.model.YuGongContext;
import com.taobao.yugong.common.model.record.Record;
import com.taobao.yugong.common.utils.YuGongUtils;
import com.taobao.yugong.common.utils.thread.ExecutorTemplate;
import com.taobao.yugong.common.utils.thread.NamedThreadFactory;
import com.taobao.yugong.exception.YuGongException;

/**
 * @author agapple 2014年2月25日 下午11:38:06
 * @since 1.0.0
 */
public class MultiThreadCheckRecordApplier extends CheckRecordApplier {

    private int                threadSize = 5;
    private int                splitSize  = 50;
    private ThreadPoolExecutor executor;
    private String             executorName;

    public MultiThreadCheckRecordApplier(YuGongContext context){
        super(context);
    }

    public MultiThreadCheckRecordApplier(YuGongContext context, int threadSize, int splitSize){
        super(context);

        this.threadSize = threadSize;
        this.splitSize = splitSize;
    }

    public MultiThreadCheckRecordApplier(YuGongContext context, int threadSize, int splitSize,
                                         ThreadPoolExecutor executor){
        super(context);

        this.threadSize = threadSize;
        this.splitSize = splitSize;
        this.executor = executor;
    }

    public void start() {
        super.start();

        executorName = this.getClass().getSimpleName() + "-" + context.getTableMeta().getFullName();
        if (executor == null) {
            executor = new ThreadPoolExecutor(threadSize,
                threadSize,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue(threadSize * 2),
                new NamedThreadFactory(executorName),
                new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    public void apply(final List<Record> records) throws YuGongException {
        // no one,just return
        if (YuGongUtils.isEmpty(records)) {
            return;
        }

        if (records.size() > splitSize) {
            ExecutorTemplate template = new ExecutorTemplate(executor);
            try {
                int index = 0;// 记录下处理成功的记录下标
                int size = records.size();
                // 全量复制时，无顺序要求，数据可以随意切割，直接按照splitSize切分后提交到多线程中进行处理
                for (; index < size;) {
                    int end = (index + splitSize > size) ? size : (index + splitSize);
                    final List<Record> subList = records.subList(index, end);
                    template.submit(new Runnable() {

                        public void run() {
                            String name = Thread.currentThread().getName();
                            try {
                                MDC.put(YuGongConstants.MDC_TABLE_SHIT_KEY, context.getTableMeta().getFullName());
                                Thread.currentThread().setName(executorName);
                                doApply(subList);
                            } finally {
                                Thread.currentThread().setName(name);
                            }

                        }
                    });
                    index = end;// 移动到下一批次
                }

                // 等待所有结果返回
                template.waitForResult();
            } finally {
                template.clear();
            }
        } else {
            doApply(records);
        }
    }

    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
    }

    public void setSplitSize(int splitSize) {
        this.splitSize = splitSize;
    }
}
