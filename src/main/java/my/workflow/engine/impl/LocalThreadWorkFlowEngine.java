package my.workflow.engine.impl;

import my.workflow.common.ProcessStatusEnum;
import my.workflow.engine.IEngineExceptionHandler;
import my.workflow.engine.IWorkFlowEngine;
import my.workflow.engine.IProcessJob;
import my.workflow.engine.IProcessExceptionHandler;
import my.workflow.engine.event.IProcessJobListener;
import my.workflow.process.ProcessInstance;
import my.workflow.process.event.IWorkRunnerListener;
import my.workflow.process.factory.IProcessFactoryManager;
import my.workflow.storage.IPauseStorage;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LocalThreadWorkFlowEngine implements IWorkFlowEngine, IProcessJobListener {

    private Map<String, IProcessJob> runningProcessJobs;

    private List<IProcessJobListener> processJobListeners;

    private List<IWorkRunnerListener> workRunnerListeners;

    private IPauseStorage pauseStorageService;

    private IProcessFactoryManager processFactoryManager;

    private IEngineExceptionHandler exceptionHandler;

    private IProcessExceptionHandler processExceptionHandler;

    private ExecutorService executorService;

    /**
     * 正在运行的Job信息，processId作为key，定时或者事件处理运行结果
     */
    private Map<String, Future> processJobFutures;

    private LocalThreadWorkFlowEngine() {
        this.runningProcessJobs = new HashMap<>();
        this.processJobListeners = new ArrayList<>();
        this.workRunnerListeners = new ArrayList<>();

        /**
         * 保存
         */
        this.processJobFutures = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(5);

        this.exceptionHandler = new IEngineExceptionHandler() {
            @Override
            public void catchException(Exception e) {

            }
        };

        this.processExceptionHandler = new IProcessExceptionHandler() {
            @Override
            public void catchException(ProcessInstance instance, Exception e) {

            }
        };

        this.registerProcessJobListener(this);
    }

    public IPauseStorage getPauseStorageService() {
        return this.pauseStorageService;
    }
    public IProcessFactoryManager getProcessFactoryManager() {
        return this.processFactoryManager;
    }
    public IEngineExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    @Override
    public void startProcessInstance(ProcessInstance instance) {
        if (instance == null || StringUtils.isEmpty(instance.getId())) {
            return;
        }

        LocalThreadProcessJob processJob;
        synchronized (this.runningProcessJobs) {
            if (this.runningProcessJobs.containsKey(instance.getId())) {
                //TODO 错误处理，有相同ID的Process正在执行
                return;
            }

            processJob = new LocalThreadProcessJob(this, instance);
            processJob.setProcessExceptionHandler(this.processExceptionHandler);

            this.runningProcessJobs.put(instance.getId(), processJob);
        }

        //开始真正执行一个Job任务
        this.innerRunProcessJob(processJob);

        return;
    }

    @Override
    public void restartPausedProcessInstance(String processId) {
        if (StringUtils.isEmpty(processId)) {
            return;
        }

        synchronized (this.runningProcessJobs) {
            if (this.runningProcessJobs.containsKey(processId)) {
                return;
            }
        }

        ProcessInstance instance = this.pauseStorageService.getProcessInstanceById(processId);
        if (instance == null) {
            return;
        }

        //走普通的处理流程
        this.startProcessInstance(instance);
    }

    @Override
    public void pauseProcessInstance(String processId) {
        if (StringUtils.isEmpty(processId)) {
            return;
        }

        synchronized (this.runningProcessJobs) {
            if (!this.runningProcessJobs.containsKey(processId)) {
                return;
            }
            IProcessJob processJob = this.runningProcessJobs.get(processId);
            if (processJob == null) {
                return;
            }
            processJob.stop();
        }
    }

    @Override
    public void endProcessInstance(String processId) {
        if (StringUtils.isEmpty(processId)) {
            return;
        }

        synchronized (this.runningProcessJobs) {
            if (!this.runningProcessJobs.containsKey(processId)) {
                return;
            }
            IProcessJob processJob = this.runningProcessJobs.get(processId);
            if (processJob == null) {
                return;
            }
            processJob.getProcessInstance().setStatus(ProcessStatusEnum.ENDED);
            processJob.stop();
        }
    }

    /**
     * TODO 没有预期的Join阻塞线程等待执行
     */
    public void join() {
        this.executorService.shutdown();
    }

    public List<IProcessJobListener> getProcessJobListeners() {
        return Collections.unmodifiableList(this.processJobListeners);
    }

    public void registerProcessJobListener(IProcessJobListener listener) {
        if (listener == null) {
            return;
        }

        this.processJobListeners.add(listener);
    }

    public List<IWorkRunnerListener> getWorkRunnerListeners() {
        return Collections.unmodifiableList(this.workRunnerListeners);
    }

    public void registerWorkRunnerListener(IWorkRunnerListener listener) {
        if (listener == null) {
            return;
        }

        this.workRunnerListeners.add(listener);
    }


    //JOB 监听
    public void afterStopped(ProcessInstance instance) {
        synchronized (this.runningProcessJobs) {
            this.runningProcessJobs.remove(instance.getId());
        }

        synchronized (this.processJobFutures) {
            this.processJobFutures.remove(instance.getId());
        }
    }


    private void innerRunProcessJob(IProcessJob processJob) {
        if (processJob == null) {
            return;
        }

        Future future = this.executorService.submit(processJob);
        synchronized (this.processJobFutures) {
            this.processJobFutures.put(processJob.getProcessInstance().getId(), future);
        }
    }

    public static class Builder {
        private IPauseStorage pauseStorage;
        private IProcessFactoryManager processFactoryManager;
        private IEngineExceptionHandler exceptionHandler;
        private IProcessExceptionHandler processExceptionHandler;

        private Builder() {

        }

        public static LocalThreadWorkFlowEngine.Builder newEngine() {
            return new LocalThreadWorkFlowEngine.Builder();
        }

        public LocalThreadWorkFlowEngine.Builder setPauseStorage(IPauseStorage pauseStorage) {
            this.pauseStorage = pauseStorage;
            return this;
        }

        public LocalThreadWorkFlowEngine.Builder setProcessFactoryManager(IProcessFactoryManager processFactoryManager) {
            this.processFactoryManager = processFactoryManager;
            return this;
        }

        public LocalThreadWorkFlowEngine.Builder setExceptionHandler(IEngineExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public LocalThreadWorkFlowEngine.Builder setProcessExceptionHandler(IProcessExceptionHandler processExceptionHandler) {
            this.processExceptionHandler = processExceptionHandler;
            return this;
        }

        public LocalThreadWorkFlowEngine build() {
            if (this.pauseStorage == null || this.processFactoryManager == null) {
                return null;
            }

            LocalThreadWorkFlowEngine engine = new LocalThreadWorkFlowEngine();
            engine.pauseStorageService = this.pauseStorage;
            engine.processFactoryManager = this.processFactoryManager;
            if (this.exceptionHandler != null) {
                engine.exceptionHandler = this.exceptionHandler;
            }

            if (this.processExceptionHandler != null) {
                engine.processExceptionHandler = this.processExceptionHandler;
            }

            return engine;
        }
    }
}
