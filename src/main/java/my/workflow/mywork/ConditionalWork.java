package my.workflow.mywork;

import my.workflow.process.work.ITransition;
import my.workflow.process.work.WorkReport;
import my.workflow.engine.IWorkRunner;
import my.workflow.process.work.AbstractWork;
import my.workflow.process.work.IWork;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 根据给定的varName检查变量是否为true，并选择success路径执行
 * 没有变量或者变量不为boolean类型，按 false处理
 */
public class ConditionalWork extends AbstractWork implements IWork {

    public final static String TYPE = "conditional";

    private boolean isSuccess;
    private String onSuccessDestinationWorkId;
    private String varName;

    public ConditionalWork(String id, String varName) {
        super(id);
        this.varName = varName;
    }

    public void setOnSuccess(String destinationWorkId) {
        this.onSuccessDestinationWorkId = destinationWorkId;
    }

    public String getType() {
        return TYPE;
    }

    public WorkReport exec(IWorkRunner runner) {
        Object value = runner.getProcessInstance().getVariable(this.varName);
        this.isSuccess = false;
        if (value != null && value instanceof Boolean) {
            if ((Boolean) value) {
                this.isSuccess = true;
            }
        }
        return new WorkReport(this.getId());
    }

    @Override
    public List<ITransition> getOutTransitions() {
        List<ITransition> successFailTransitions = this.findOnSuccessFailTransition();
        if (successFailTransitions.isEmpty()) {
            //TODO 应该有日志支持
            return new ArrayList<>();//强制结束了，有问题
        }

        if (this.isSuccess) {
            ITransition transition = successFailTransitions.get(0);
            if (transition != null) {
                return Arrays.asList(transition);
            }
        }

        ITransition transition = successFailTransitions.get(1);
        if (transition == null) {
            return new ArrayList<>();
        }


        return Arrays.asList(transition);
    }

    /**
     * 按success与fail返回Transition
     * @return
     */
    private List<ITransition> findOnSuccessFailTransition() {
        List<ITransition> transitions = super.getOutTransitions();
        if (transitions.size() != 2) {
            return new ArrayList<>();
        }

        if (StringUtils.isEmpty(this.onSuccessDestinationWorkId)) {
            return Arrays.asList(null, transitions.get(1));
        }


        List<ITransition> res = Arrays.asList(null, null);
        for (ITransition t : transitions) {
            if (t.getDestinationWork().getId().equals(this.onSuccessDestinationWorkId)) {
                res.set(0, t);
            } else {
                res.set(1, t);
            }
        }

        return res;
    }
}
