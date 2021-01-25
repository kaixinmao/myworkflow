package my.workflow.engine;

import my.workflow.engine.event.IProcessJobListener;
import my.workflow.process.ProcessInstance;
import my.workflow.process.event.IWorkRunnerListener;
import my.workflow.process.factory.IProcessFactoryManager;
import my.workflow.storage.IPauseStorage;

import java.util.List;

public interface IWorkFlowEngine {

    /**
     * 获取engine装配的存储服务
     * @return
     */
    IPauseStorage getPauseStorageService();

    IProcessFactoryManager getProcessFactoryManager();

    /**
     * 获取异常处理器
     *
     * @return
     */
    IEngineExceptionHandler getExceptionHandler();

    /**
     * 开始执行一个具体的instance
     * @param instance
     */
    void startProcessInstance(ProcessInstance instance);


    /**
     * 根据给定的processId，尝试将暂停任务重新执行
     * @param processId
     */
    void restartPausedProcessInstance(String processId);

    /**
     * 暂停一个正在执行的流程
     * 流程暂停只能当所有的WorkRunner执行完当前节点。
     * @param processId
     */
    void pauseProcessInstance(String processId);

    /**
     * 停止一个正在执行的流程，所有的业务中间信息将被清理，不能再被重新启用执行
     * @param processId
     */
    void endProcessInstance(String processId);

    /**
     * 阻塞等待所有ProcessInstance执行完毕
     */
    void join();

    List<IProcessJobListener> getProcessJobListeners();

    void registerProcessJobListener(IProcessJobListener listener);

    List<IWorkRunnerListener> getWorkRunnerListeners();
    void registerWorkRunnerListener(IWorkRunnerListener listener);
}
