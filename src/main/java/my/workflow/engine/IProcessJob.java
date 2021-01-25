package my.workflow.engine;

import my.workflow.common.ProcessJobStatusEnum;
import my.workflow.process.ProcessInstance;
import my.workflow.process.event.IWorkRunnerListener;

import java.util.List;

public interface IProcessJob extends Runnable {

    /**
     * Running或paused状态才可停止，其它状态将不响应
     * 停止当前执行的Process信息，并执行中间数据信息回收
     */
    void stop();

    ProcessJobStatusEnum getStatus();

    IProcessExceptionHandler getProcessExceptionHandler();

    ProcessInstance getProcessInstance();

    List<IWorkRunnerListener> getWorkRunnerListeners();
}
