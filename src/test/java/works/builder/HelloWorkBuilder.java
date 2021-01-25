package works.builder;

import my.workflow.definition.IWorkTypeBuilder;
import my.workflow.definition.WorkDefinition;
import my.workflow.process.work.IWork;
import works.HelloWork;

public class HelloWorkBuilder implements IWorkTypeBuilder {

    public String getType() {
        return HelloWork.TYPE;
    }

    public IWork build(WorkDefinition workDefinition) {
        HelloWork work = new HelloWork(workDefinition.getId(), workDefinition.getParameters().getString("helloWhat"));
        return work;
    }
}