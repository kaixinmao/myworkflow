package my.workflow.engine.event;

import my.workflow.process.ProcessInstance;

public interface IProcessJobListener {
    default void beforeStarted(ProcessInstance instance) {

    }

    default void afterStopped(ProcessInstance instance) {

    }
}
