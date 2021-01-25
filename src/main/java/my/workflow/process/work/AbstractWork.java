package my.workflow.process.work;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractWork implements IWork {

    private String id;

    private List<ITransition> inTransitions;

    private List<ITransition> outTransitions;

    public AbstractWork(String id) {
        this.id = id;
        this.inTransitions = new ArrayList<>();
        this.outTransitions = new ArrayList<>();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public List<ITransition> getOutTransitions() {
        return Collections.unmodifiableList(this.outTransitions);
    }

    public void addOutTransition(ITransition transition) {
        if (transition == null) {
            return;
        }
        IWork srcWork = transition.getSourceWork();
        //来源与当前Work不相同
        if (srcWork == null || !srcWork.getId().equals(this.id)) {
            //XXX 应该日志报错
            return;
        }

        this.outTransitions.add(transition);
    }

    public void addOutTransitions(List<ITransition> transitions) {
        if (CollectionUtils.isEmpty(transitions)) {
            return;
        }

        for (ITransition transition : transitions) {
            this.addOutTransition(transition);
        }
    }

    public List<ITransition> getInTransitions() {
        return Collections.unmodifiableList(this.inTransitions);
    }

    public void addInTransition(ITransition transition) {
        if (transition == null) {
            return;
        }

        IWork destWork = transition.getDestinationWork();
        if (destWork == null || !destWork.getId().equals(this.id)) {
            //XXX 应该日志报错
        }

        this.inTransitions.add(transition);
    }

    public void addInTransitions(List<ITransition> transitions) {
        if (CollectionUtils.isEmpty(transitions)) {
            return;
        }

        for (ITransition transition : transitions) {
            this.addInTransition(transition);
        }
    }

}
