package my.workflow.process.work;

import com.alibaba.fastjson.JSONObject;
import my.workflow.engine.IWorkRunner;

import java.util.List;

/**
 * 工作流最基础元素，整个流程将以Work为单位进行执行。
 */
public interface IWork {

    /**
     * 获取该Work在Process中的唯一ID信息
     * @return
     */
    String getId();

    String getType();

    WorkReport exec(IWorkRunner runner);

    /**
     * 所有出线记录
     * 包括已经Skipped掉的，但必须标记Skipped，否则将被执行
     * @return
     */
    List<ITransition> getOutTransitions();

    /**
     * 所有入线记录。
     * 注意：由于可能的条件或者并发执行终止，某些入线为非Skipped，但是依然不会被执行
     * 不能作为执行来源
     *
     * @return
     */
    List<ITransition> getInTransitions();

    void addInTransition(ITransition transition);
    void addInTransitions(List<ITransition> transitions);

    void addOutTransition(ITransition transition);
    void addOutTransitions(List<ITransition> transitions);
}
