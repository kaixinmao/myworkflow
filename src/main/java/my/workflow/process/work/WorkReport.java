package my.workflow.process.work;

/**
 * 通用的Work执行返回结果
 */
public class WorkReport {

    private String workId;

    private boolean success;

    private String errorMsg;

    public WorkReport(String workId) {
        this(workId, true);
    }

    public WorkReport(String workId, boolean success) {
        this.workId = workId;
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
