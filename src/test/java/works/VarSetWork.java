package works;

import com.alibaba.fastjson.JSONObject;
import my.workflow.engine.IWorkRunner;
import my.workflow.process.ProcessInstance;
import my.workflow.process.work.AbstractWork;
import my.workflow.process.work.IWork;
import my.workflow.process.work.WorkReport;

import java.util.HashMap;
import java.util.Map;

public class VarSetWork extends AbstractWork implements IWork {

    public final static String TYPE = "varset";

    private Map<String, Object> variables;

    public VarSetWork(String id) {
        super(id);
        this.variables = new HashMap<>();
    }

    public String getType() {
        return TYPE;
    }

    public void setValue(Map<String, Object> variables) {
        if (variables == null && variables.isEmpty()) {
            return;
        }

        this.variables.putAll(variables);
    }

    public WorkReport exec(IWorkRunner runner) {

        ProcessInstance instance = runner.getProcessInstance();
        if (!this.variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : this.variables.entrySet()) {
                instance.putVariable(entry.getKey(), entry.getValue());
            }
        }

        WorkReport report = new WorkReport(this.getId());

        return report;
    }
}