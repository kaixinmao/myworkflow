package my.workflow.engine;

/**
 * 有些engine内部产生的错误是在线程中运行的，提供handler方便对整个执行流程异常进行处理
 */
public interface IEngineExceptionHandler {
    default void catchException(Exception e) {

    }
}
