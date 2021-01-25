package my.workflow.mywork.builder;

import my.workflow.mywork.ConditionalWork;
import my.workflow.definition.IWorkTypeBuilder;
import my.workflow.definition.WorkDefinition;
import my.workflow.process.work.IWork;

public class ConditionalWorkBuilder implements IWorkTypeBuilder {

    public String getType() {
        return ConditionalWork.TYPE;
    }

    public IWork build(WorkDefinition workDefinition) {
        if (workDefinition == null) {
            return null;
        }

        if (!workDefinition.getType().equals(ConditionalWork.TYPE)) {
            return null;
        }

        String varName = workDefinition.getParameters().getString("varName");
        String onSuccessDestinationWorkId = workDefinition.getParameters().getString("succWorkId");

        ConditionalWork work = new ConditionalWork(workDefinition.getId(), varName);
        work.setOnSuccess(onSuccessDestinationWorkId);

        return work;
    }
}
