package my.workflow.process.event;

import my.workflow.process.work.WorkReport;
import my.workflow.engine.IWorkRunner;
import my.workflow.process.ProcessInstance;
import my.workflow.process.work.ITransition;
import my.workflow.process.work.IWork;

import java.util.List;

/**
 * Process执行内部事件监听
 * 默认什么都不做
 */
public interface IWorkRunnerListener {
    /**
     * 出现执行错误，后续会单独再触发Stopped操作
     * @param instance
     */
    default void onWorkRunnerError(ProcessInstance instance, IWorkRunner runner, IWork work, WorkReport report) {

    }

    /**
     * runner从Waiting或paused状态开始执行时
     * @param instance
     * @param runner
     */
    default void beforeWorkRunnerStart(ProcessInstance instance, IWorkRunner runner) {

    }

    /**
     * 运行被暂停
     * @param instance
     */
    default void afterWorkRunnerPaused(ProcessInstance instance, IWorkRunner runner) {

    }

    /**
     * runner执行完一个Work后，选择好一条线并准备开始下一个Work迭代的时候
     * @param instance
     * @param runner
     * @param transition
     */
    default void onWorkRunnerTransition(ProcessInstance instance, IWorkRunner runner, ITransition transition) {

    }


    default void afterWorkRunnerStopped(ProcessInstance instance, IWorkRunner runner) {

    }

    default void afterWorkBeExecuted(ProcessInstance instance, IWorkRunner runner, IWork work, WorkReport report) {

    }

    /**
     * 由Work发起的通用事件，由业务自行处理
     * @param instance
     * @param event
     */
    default void onWorkCustomEvent(ProcessInstance instance, IWorkRunner runner, IWork work, IProcessCustomEvent event) {

    }
}
