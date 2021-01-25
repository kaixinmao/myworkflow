package my.workflow.engine.impl;

import my.workflow.common.ProcessStatusEnum;
import my.workflow.process.event.IWorkRunnerListener;
import my.workflow.process.work.WorkReport;
import my.workflow.common.WorkRunnerStatusEnum;
import my.workflow.engine.IWorkRunner;
import my.workflow.process.ProcessInstance;
import my.workflow.process.work.ITransition;
import my.workflow.process.work.IWork;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class LocalWorkRunner implements IWorkRunner {

    private String id;

    private int steps = 0;

    private WorkRunnerStatusEnum status;

    private IWork currentWork;

    private LocalWorkRunner parentRunner;

    private LocalThreadProcessJob processJob;

    private ProcessInstance processInstance;

    public LocalWorkRunner(String id, LocalThreadProcessJob processJob) {
        this.id = id;
        this.status = WorkRunnerStatusEnum.WAITING;
        this.processJob = processJob;
        this.processInstance = processJob.getProcessInstance();
    }

    public LocalWorkRunner(String id, LocalWorkRunner parentRunner) {
        this(id, parentRunner.getProcessJob());
        this.parentRunner = parentRunner;
    }

    public LocalThreadProcessJob getProcessJob() {
        return this.processJob;
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return this.processInstance;
    }

    @Override
    public IWorkRunner getParent() {
        return this.parentRunner;
    }

    @Override
    public List<IWorkRunner> getParents() {
        if (this.parentRunner == null) {
            return new ArrayList<IWorkRunner>();
        }

        List<IWorkRunner> parents = new ArrayList<>();
        parents.add(this.parentRunner);
        IWorkRunner tmpParent = this.parentRunner;
        while(true) {
            IWorkRunner p = tmpParent.getParent();
            if (p == null) {
                //暂时用该判断方式，感觉用null有点粗暴
                break;
            }
            parents.add(p);
            tmpParent = p;
        }

        return parents;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public WorkRunnerStatusEnum getStatus() {
        return this.status;
    }


    @Override
    public void setCurrentWork(IWork work) {
        if (this.currentWork != null) {
            //同步去掉当前正在执行的work
            this.processInstance.removeCurrentWork(this.currentWork.getId());
        }
        this.currentWork = work;
        if (this.currentWork != null) {
            this.processInstance.putCurrentWork(this.currentWork);
        }
    }

    @Override
    public IWork getCurrentWork() {
        return this.currentWork;
    }

    @Override
    public int getSteps() {
        return this.steps;
    }

    @Override
    public void exec() {
        //外部线程可能会操作processInstance状态信息，需要线程保护
        synchronized (this.processInstance) {
            if (this.processInstance.getStatus().equals(ProcessStatusEnum.ENDED)) {
                //已经执行完不需要再执行了
                return;
            }

            if (!this.processInstance.getStatus().equals(ProcessStatusEnum.RUNNING)) {
                this.processInstance.setStatus(ProcessStatusEnum.RUNNING);
            }
        }

        //beforeWorkRunnerStart
        for (IWorkRunnerListener listener : this.processJob.getWorkRunnerListeners()) {
            listener.beforeWorkRunnerStart(this.processInstance, this);
        }

        this.doExecLoop();

        //检查当前Job以什么状态退出，并做对应的退出操作
        switch (this.status) {
            case PAUSED:
                this.doPause();
                break;
            case STOPPED:
                this.doStop();
                break;
        }
    }

    /**
     * exec具体Work执行的循环操作
     */
    private void doExecLoop() {

        IWork work;

        /**
         * 开始爬整个流程
         * 如果执行失败直接退出
         * 检查可用出线，第一个作为下个执行目标
         * 其余出线fork新的runner进行运行
         * 检查nextWork的入线，是否有多条
         * 如果有多条，检查是否除自己外，还有其它正在运行或者等待运行的runner
         * 如果有其它运行的，退出当前runner，等待别人来运行
         *
         * 每执行完一次，检查processInstance状态，看是否有强制停止或者暂停操作
         * 如果有，instance状态设置runner状态，并进行相关Do处理
         */
        while(true) {
            work = this.currentWork;

            //Work功能执行
            WorkReport report;
            try {
                report = work.exec(this);
            } catch (Exception e) {
                //XXX 处理错误信息
                report = new WorkReport(work.getId(), false);
                report.setErrorMsg(e.getMessage());
                this.processJob.getProcessExceptionHandler().catchException(this.processInstance, e);
            }
            this.steps++;//增加执行work数量

            //检查Report信息，开始走下一个流程
            if (!report.isSuccess()) {
                synchronized (this.processInstance) {
                    this.processInstance.setStatus(ProcessStatusEnum.ERROR);
                }

                //onWorkRunnerError
                for (IWorkRunnerListener listener : this.processJob.getWorkRunnerListeners()) {
                    listener.onWorkRunnerError(this.processInstance, this, work, report);
                }

                //停止继续工作
                break;
            }

            //afterWorkBeExecuted
            for (IWorkRunnerListener listener : this.processJob.getWorkRunnerListeners()) {
                listener.afterWorkBeExecuted(this.processInstance, this, work, report);
            }

            //获取出线，准备下一个工作
            IWork nextWork = null;
            List<LocalWorkRunner> forkRunners = new ArrayList<>();
            List<ITransition> outTransitions = work.getOutTransitions();
            for (ITransition outTransition : outTransitions) {
                if (outTransition.isSkipped()) {
                    continue;
                }

                if (nextWork == null) {
                    nextWork = outTransition.getDestinationWork();
                    //亲自处理该任务

                    //onWorkRunnerTransition
                    for (IWorkRunnerListener listener : this.processJob.getWorkRunnerListeners()) {
                        listener.onWorkRunnerTransition(this.processInstance, this, outTransition);
                    }
                    continue;
                }

                //fork Runner来执行新的线条
                LocalWorkRunner runner = this.forkWorkRunnerByTransition(outTransition);
                if (runner == null) {
                    continue;
                }
                forkRunners.add(runner);
            }

            //判断是否还有其它正在跑的入线，有的话让其它最后一个来跑
            if (!this.isOnlyOneInTransitionRunner(nextWork)) {
                nextWork = null;
            }

            //先运行fork出来的Runner
            this.runForkRunners(forkRunners);

            //是否还有下一个Work
            if (nextWork == null) {
                this.status = WorkRunnerStatusEnum.STOPPED;
                break;
            }

            //设置下次需要执行的Work
            this.setCurrentWork(nextWork);

            if (!this.checkAndDoOnceWorkReport()) {
                break;
            }
        }// end of while true
    }

    /**
     * 每完成一次Work执行需要做的事情
     * 返回结果决定是否继续执行
     */
    private boolean checkAndDoOnceWorkReport() {
        ProcessStatusEnum processStatus = this.processInstance.getStatus();
        //检查processInstance状态，进行处理
        if (processStatus.equals(ProcessStatusEnum.RUNNING)) {
            return true;
        }

        switch (processStatus) {
            case PAUSED:
                this.status = WorkRunnerStatusEnum.PAUSED;
                break;
            case ENDED:
                this.status = WorkRunnerStatusEnum.STOPPED;
                break;
        }
        return false;
    }

    private void runForkRunners(List<LocalWorkRunner> forkRunners) {
        if (CollectionUtils.isEmpty(forkRunners)) {
            return;
        }

        for(LocalWorkRunner runner : forkRunners) {
            runner.exec();
        }
    }

    private boolean isOnlyOneInTransitionRunner(IWork nextWork) {
        if (nextWork == null) {
            return true;
        }

        //检查其它进线还有没runner在跑
        List<ITransition> inTransitions = nextWork.getInTransitions();
        if (inTransitions.size() == 1) {
            return true;
        }

        //如果有多条线查找正在跑的Runner
        for (ITransition inTransition : inTransitions) {
            IWorkRunner runner = this.upFindWorkRunner(inTransition);
            if (runner == null || runner.equals(this)) {
                continue;
            }

            //有其它入线还要跑，该runner任务结束了
            return false;
        }

        return true;
    }

    /**
     * runner执行完毕时被调用
     */
    private void doStop() {
        this.setCurrentWork(null);
        this.status = WorkRunnerStatusEnum.STOPPED;

        //afterWorkRunnerStopped
        for (IWorkRunnerListener listener : this.processJob.getWorkRunnerListeners()) {
            listener.afterWorkRunnerStopped(this.processInstance, this);
        }
    }

    /**
     * runner暂停时被调用
     */
    private void doPause() {
        //currentWork不会被设置为null，恢复执行时会继续执行
        this.status = WorkRunnerStatusEnum.PAUSED;

        //afterWorkRunnerPaused
        for (IWorkRunnerListener listener : this.processJob.getWorkRunnerListeners()) {
            listener.afterWorkRunnerPaused(this.processInstance, this);
        }
    }


    private LocalWorkRunner forkWorkRunnerByTransition(ITransition transition) {
        if (transition == null) {
            return null;
        }

        IWork work = transition.getDestinationWork();
        if (work == null) {
            return null;
        }

        List<ITransition> inTransitions = work.getInTransitions();
        if (inTransitions.size() > 1) {
            //需要检查其它线是不是在跑
            for (ITransition inTransition : inTransitions) {
                if (transition.equals(inTransition)) {
                    continue;
                }
                IWorkRunner runner = this.upFindWorkRunner(transition);
                if (runner != null) {
                    //还有其它入线正在运行或者等待运行
                    return null;
                }
            }
        }

        //确定没有其它线有运行的，准备开始执行
        //确定一个没有被占用的runner名称
        String runnerId = "to_" + work.getId();
        Map<String, LocalWorkRunner> runners = this.processJob.getWorkRunners();
        int index = 1;
        while(true) {
            if (!runners.containsKey(runnerId)) {
                break;
            }
            runnerId += "_" + String.valueOf(index);
            index++;
        }

        LocalWorkRunner runner = new LocalWorkRunner(runnerId, this);
        runner.setCurrentWork(work);

        return runner;
    }

    /**
     * 按当前线往上查找正在运行的Runner。
     * 查找范围为多条出线节点或者无进线Work结束
     *
     * @param transition
     * @return
     */
    private IWorkRunner upFindWorkRunner(ITransition transition) {
        if (transition == null) {
            return null;
        }
        Map<String, IWork> needCheckWorksMap = new HashMap<>();

        while(true) {
            IWork work = transition.getSourceWork();
            if (work == null) {
                //没有节点了
                break;
            }

            List<ITransition> outTransitions = work.getOutTransitions();
            if (outTransitions.size() > 1) {
                //有多条出线
                break;
            }
            needCheckWorksMap.put(work.getId(), work);
        }

        //检查当前所有Runner是否正在运行该节点。（当前只考虑一个Process一个线程处理，未考虑并发问题）
        Map<String, LocalWorkRunner> runners = this.processJob.getWorkRunners();
        for (Map.Entry<String, LocalWorkRunner> entry : runners.entrySet()) {
            LocalWorkRunner runner = entry.getValue();
            if (!runner.getStatus().equals(WorkRunnerStatusEnum.RUNNING)
                    && !runner.getStatus().equals(WorkRunnerStatusEnum.WAITING)
                    && !runner.getStatus().equals(WorkRunnerStatusEnum.PAUSED)
            ) {
                //只考虑正在运行的Runner
                continue;
            }
            IWork currentWork = runner.getCurrentWork();
            if (needCheckWorksMap.containsKey(currentWork.getId())) {
                return runner;
            }
        }

        return null;
    }
}
