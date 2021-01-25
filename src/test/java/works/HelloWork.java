package works;

import com.alibaba.fastjson.JSONObject;
import my.workflow.process.work.WorkReport;
import my.workflow.engine.IWorkRunner;
import my.workflow.process.work.AbstractWork;
import my.workflow.process.work.IWork;

public class HelloWork extends AbstractWork implements IWork {

    public final static String TYPE = "hello";

    private String helloWhat;

    public HelloWork(String id, String helloWhat) {
        super(id);
        this.helloWhat = helloWhat;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public WorkReport exec(IWorkRunner runner) {

        System.out.printf("Hello %s\n", this.helloWhat);

        JSONObject obj = runner.getProcessInstance().getJSONVariable("hello_str");

        if (obj != null) {
            System.out.printf("work %s get json variable: %s\n", this.getId(), obj.getString("data"));
        }

        WorkReport report = new WorkReport(this.getId());

        return report;
    }

}
