package my.workflow.common;

import java.util.HashMap;
import java.util.Map;

public enum WorkRunnerStatusEnum {
    WAITING(0),
    STOPPED(1),
    PAUSED(2),
    RUNNING(100),
    UNKNOWN(-99);

    private final static Map<Integer, WorkRunnerStatusEnum> INNER;

    static {
        INNER = new HashMap<>();
        for (WorkRunnerStatusEnum workRunnerStatusEnum : WorkRunnerStatusEnum.values()) {
            INNER.put(workRunnerStatusEnum.code, workRunnerStatusEnum);
        }
    }

    private Integer code;


    WorkRunnerStatusEnum(Integer code) {
        this.code = code;
    }

    public static WorkRunnerStatusEnum parseFromCode(Integer code) {
        return INNER.getOrDefault(code, UNKNOWN);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
