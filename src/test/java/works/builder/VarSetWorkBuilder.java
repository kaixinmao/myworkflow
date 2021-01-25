package works.builder;

import my.workflow.definition.IWorkTypeBuilder;
import my.workflow.definition.WorkDefinition;
import my.workflow.process.work.IWork;
import works.VarSetWork;


public class VarSetWorkBuilder implements IWorkTypeBuilder {

    public String getType() {
        return VarSetWork.TYPE;
    }

    public IWork build(WorkDefinition workDefinition) {
        VarSetWork work = new VarSetWork(workDefinition.getId());
        work.setValue(workDefinition.getParameters());
        return work;
    }
}
