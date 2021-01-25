package my.workflow.engine.impl;

import my.workflow.common.ProcessStatusEnum;
import my.workflow.common.WorkRunnerStatusEnum;
import my.workflow.engine.IWorkFlowEngine;
import my.workflow.engine.IProcessJob;
import my.workflow.common.ProcessJobStatusEnum;
import my.workflow.engine.IProcessExceptionHandler;
import my.workflow.engine.IWorkRunner;
import my.workflow.engine.event.IProcessJobListener;
import my.workflow.process.ProcessInstance;
import my.workflow.process.event.IWorkRunnerListener;
import my.workflow.process.work.IWork;

import java.util.*;

public class LocalThreadProcessJob implements IProcessJob {

    private ProcessJobStatusEnum status = ProcessJobStatusEnum.STOPPED;

    private ProcessInstance instance;

    private IWorkFlowEngine engine;

    private Map<String, LocalWorkRunner> runners;

    private IProcessExceptionHandler processExceptionHandler;

    public LocalThreadProcessJob(IWorkFlowEngine engine, ProcessInstance processInstance) {
        this.engine = engine;
        this.instance = processInstance;
        this.runners = new HashMap<>();
    }

    @Override
    public IProcessExceptionHandler getProcessExceptionHandler() {
        return this.processExceptionHandler;
    }

    public void setProcessExceptionHandler(IProcessExceptionHandler exceptionHandler) {
        this.processExceptionHandler = exceptionHandler;
    }

    public Map<String, LocalWorkRunner> getWorkRunners() {
        return Collections.unmodifiableMap(this.runners);
    }

    @Override
    public void run() {
        synchronized (this.status) {
            if (!this.status.equals(ProcessJobStatusEnum.STOPPED)) {
                return;
            }

            this.status = ProcessJobStatusEnum.RUNNING;
        }

        //从beginningWork中创建runner
        List<IWork> currentWorks = new ArrayList(this.instance.getCurrentWorks().values());
        if (currentWorks.isEmpty()) {
            //没有开始节点，直接结束了
            return;
        }

        for (IWork work : currentWorks) {
            LocalWorkRunner runner = new LocalWorkRunner("from_" + work.getId(), this);
            runner.setCurrentWork(work);
            this.runners.put(runner.getId(), runner);
        }

        try {
            //触发beforeStarted
            for (IProcessJobListener listener : this.engine.getProcessJobListeners()) {
                listener.beforeStarted(this.instance);
            }
        } catch (Exception e) {
            //before操作失败，暂不考虑直接结束主流程
            this.engine.getExceptionHandler().catchException(e);
        }

        try {
            //暂停或者等待开始的，重新开始一个个执行
            //*** 当前执行Job为串行执行，后续可以再改造
            for (Map.Entry<String, LocalWorkRunner> entry : this.runners.entrySet()) {
                entry.getValue().exec();
            }
        } catch (Exception e) {
            if (this.processExceptionHandler != null) {
                this.processExceptionHandler.catchException(this.getProcessInstance(), e);
            }
        } finally {
            synchronized (this.status) {
                //如果不是临时暂停，运行结束，直接设置为停止状态
                this.status = ProcessJobStatusEnum.STOPPED;
            }
            try {
                //Job结束前处理动作
                this.doJobStop();
            } catch (Exception e) {
                this.engine.getExceptionHandler().catchException(e);
            }
        }
    }

    @Override
    public void stop() {
        //不是RUNNING状态，肯定已经开始在Stop了或者已经Stop完了
        if (!this.status.equals(ProcessJobStatusEnum.RUNNING)) {
            return;
        }

        synchronized (this.instance) {
            ProcessStatusEnum processStatus = this.instance.getStatus();
            if (processStatus.equals(ProcessStatusEnum.RUNNING)) {
                //Job停止了，但是ProcessInstance还处于运行状态，应该暂停处理等待下次运行
                this.instance.setStatus(ProcessStatusEnum.PAUSED);
            }
        }
    }

    @Override
    public ProcessJobStatusEnum getStatus() {
        return this.status;
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return this.instance;
    }

    @Override
    public List<IWorkRunnerListener> getWorkRunnerListeners() {
        return this.engine.getWorkRunnerListeners();
    }

    /**
     * jobStop后的处理操作
     * 停止只可能是所有的WorkRunner全部停止运行退出
     * 正常全部停止，或者Error异常退出（Process状态），或者暂停退出
     *
     * 最后计算ProcessInstance当前应该处于状态，并设置
     */
    private void doJobStop() {
        //检查并设置所有ProcessInstance中所有Runner退出状态，如果有Pause的，就是Paused，是Error就是Error，否则为Ended
        ProcessStatusEnum processNextStatus = ProcessStatusEnum.ENDED;
        if (this.instance.getStatus().equals(ProcessStatusEnum.ERROR)) {
            processNextStatus = ProcessStatusEnum.ERROR;
        } else {

            //判断Runner状态
            for (Map.Entry<String, LocalWorkRunner> entry : this.runners.entrySet()) {
                IWorkRunner runner = entry.getValue();
                if (runner.getStatus().equals(WorkRunnerStatusEnum.PAUSED)) {
                    processNextStatus = ProcessStatusEnum.PAUSED;
                    break;
                }
            }
        }

        this.instance.setStatus(processNextStatus);

        try {
            if (processNextStatus.equals(ProcessStatusEnum.PAUSED)) {
                this.doProcessInstancePaused();
            } else {
                this.doProcessInstanceStopped();
            }
        } catch (Exception e) {
            this.engine.getExceptionHandler().catchException(e);
        }

        //触发afterStopped
        for (IProcessJobListener listener : this.engine.getProcessJobListeners()) {
            listener.afterStopped(this.instance);
        }
    }

    /**
     * 处理processInstance暂停停止的情况
     */
    private void doProcessInstancePaused() {
        //保存全量信息数据
        this.engine.getPauseStorageService().saveProcessInstance(this.instance);
    }

    /**
     * 处理processInstance停止后的处理
     */
    private void doProcessInstanceStopped() {
        //运行完毕，删除对应环境
        this.engine.getPauseStorageService().delProcessInstanceById(this.instance.getId());
    }

    /**
     * 不管Job是pause还是stop，先对ProcessInstance进行暂停
     */
    private void innerPauseProcessInstance() {

    }
}
