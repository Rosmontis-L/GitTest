package org.example.utils;

public class SnowflakeIdGenerator {

    //起始时间戳
    private static final long START_TIMESTAMP = 1766991482L;

    //占用位数
    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    //各部分最大值
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    //左移位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;//12
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;//17
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    private final long dataCenterId;//数据中心ID
    private final long workerId;//工作节点ID
    private long lastTimestamp = -1L;//上一次生成ID的时间戳
    private long sequence = 0L;//当前毫秒内的序列号

    public SnowflakeIdGenerator(){
        this(1, 1);
    }

    private SnowflakeIdGenerator(long dataCenterId, long workerId) {
        //校验ID合法性
        if(dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0){
            throw new IllegalArgumentException("Data center ID can't be greater than " + MAX_DATA_CENTER_ID + " or less than 0");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        // 获取当前时间戳
        long timestamp = getCurrentTimestamp();

        //处理时钟回拨问题
        if(timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate ID.");
        }

        //同一毫秒的ID生成
        if(timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if(sequence == 0) {
                timestamp = getNextTimestamp(lastTimestamp);
            }
        } else {
            // 不同毫秒
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
}
