package works;

import my.workflow.process.work.WorkReport;
import my.workflow.engine.IWorkRunner;
import my.workflow.process.work.AbstractWork;
import my.workflow.process.work.IWork;

public class SleepWork extends AbstractWork implements IWork {
    public final static String TYPE = "sleep";

    private int sleepSeconds;

    public SleepWork(String id, int sleepSeconds) {
        super(id);
        this.sleepSeconds = sleepSeconds;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public WorkReport exec(IWorkRunner runner) {

        System.out.printf("workid %s Sleep %d seconds\n", this.getId(), this.sleepSeconds);

        try {
            Thread.sleep(this.sleepSeconds * 1000);
        } catch (Exception e) {

        }

        WorkReport report = new WorkReport(this.getId());

        return report;
    }
}
