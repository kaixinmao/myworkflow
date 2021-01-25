package my.workflow.common;

import java.util.HashMap;
import java.util.Map;

public enum  ProcessStatusEnum {
    WAITING(0),
    ENDED(2),
    ERROR(-1), //整个流程执行出现错误，退出执行，ENDED的一种异常形式
    PAUSED(11),
    RUNNING(100),
    UNKNOWN(-99);

    private final static Map<Integer, ProcessStatusEnum> INNER;

    static {
        INNER = new HashMap<>();
        for (ProcessStatusEnum processStatusEnum : ProcessStatusEnum.values()) {
            INNER.put(processStatusEnum.code, processStatusEnum);
        }
    }

    private Integer code;


    ProcessStatusEnum(Integer code) {
        this.code = code;
    }

    public static ProcessStatusEnum parseFromCode(Integer code) {
        return INNER.getOrDefault(code, UNKNOWN);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
