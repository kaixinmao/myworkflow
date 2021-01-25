package common;

import my.workflow.definition.IProcessInstanceIdGenerator;
import my.workflow.definition.ProcessDefinition;

import java.util.UUID;

public class RandomProcessIdGenerator implements IProcessInstanceIdGenerator {

    @Override
    public String generate(ProcessDefinition processDefinition) {
        return UUID.randomUUID().toString();
    }
}
