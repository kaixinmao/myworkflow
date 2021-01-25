package my.workflow.engine;

import my.workflow.process.ProcessInstance;

/**
 * 区别于EngineExceptionHandler，用于处理具体ProcessInstance执行过程中的错误
 * 一般该方法在ProcessInstance执行线程中被触发处理
 */
public interface IProcessExceptionHandler {
    default void catchException(ProcessInstance instance, Exception e) {

    }
}
