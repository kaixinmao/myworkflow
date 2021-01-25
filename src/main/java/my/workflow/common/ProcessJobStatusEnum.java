package my.workflow.common;

import java.util.HashMap;
import java.util.Map;

public enum ProcessJobStatusEnum {
    STOPPED(0),
    STOPPING(1),
    RUNNING(100),
    UNKNOWN(-99);

    private final static Map<Integer, ProcessJobStatusEnum> INNER;

    static {
        INNER = new HashMap<>();
        for (ProcessJobStatusEnum processJobStatusEnum : ProcessJobStatusEnum.values()) {
            INNER.put(processJobStatusEnum.code, processJobStatusEnum);
        }
    }

    private Integer code;


    ProcessJobStatusEnum(Integer code) {
        this.code = code;
    }

    public static ProcessJobStatusEnum parseFromCode(Integer code) {
        return INNER.getOrDefault(code, UNKNOWN);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
