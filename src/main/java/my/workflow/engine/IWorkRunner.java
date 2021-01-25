package my.workflow.engine;

import my.workflow.process.work.WorkReport;
import my.workflow.common.WorkRunnerStatusEnum;
import my.workflow.process.ProcessInstance;
import my.workflow.process.work.IWork;

import java.util.List;

public interface IWorkRunner {

    String getId();

    ProcessInstance getProcessInstance();

    IWorkRunner getParent();

    List<IWorkRunner> getParents();

    WorkRunnerStatusEnum getStatus();

    void setCurrentWork(IWork work);
    IWork getCurrentWork();

    /**
     * 返回已经执行过的Work数量
     * @return
     */
    int getSteps();

    void exec();
}
