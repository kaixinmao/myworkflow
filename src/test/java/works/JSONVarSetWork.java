package works;

import com.alibaba.fastjson.JSONObject;
import my.workflow.process.work.WorkReport;
import my.workflow.engine.IWorkRunner;
import my.workflow.process.work.AbstractWork;
import my.workflow.process.work.IWork;

public class JSONVarSetWork extends AbstractWork implements IWork {

    public final static String TYPE = "jsonvarset";

    private String varName;

    private JSONObject value;

    public JSONVarSetWork(String id) {
        super(id);
    }

    public String getType() {
        return TYPE;
    }

    public void setValue(String varName, JSONObject value) {
        this.varName = varName;
        this.value = value;
    }

    public WorkReport exec(IWorkRunner runner) {

        runner.getProcessInstance().putJSONVariable(this.varName, this.value);

        WorkReport report = new WorkReport(this.getId());

        return report;
    }
}
