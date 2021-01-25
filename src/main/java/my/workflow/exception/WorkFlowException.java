package my.workflow.exception;

public class WorkFlowException extends RuntimeException {
    public WorkFlowException() {
        super();
    }
    public WorkFlowException(String message) {
        super(message);
    }
}
