package my.workflow.definition;

import my.workflow.process.work.IWork;

public interface IWorkTypeBuilder {
    String getType();
    IWork build(WorkDefinition workDefinition);
}
