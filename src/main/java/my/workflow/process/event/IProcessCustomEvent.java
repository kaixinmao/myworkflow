package my.workflow.process.event;

import my.workflow.engine.IWorkRunner;
import my.workflow.process.ProcessInstance;
import my.workflow.process.work.IWork;

public interface IProcessCustomEvent {

    /**
     * 事件名称在Process内应该唯一
     * @return
     */
    String getName();

    ProcessInstance getProcessInstance();

    IWorkRunner getWorkRunner();

    IWork getWork();

    /**
     * 获取事件附带数据，由业务方根据情况自己处理
     * @return
     */
    Object getData();
}
