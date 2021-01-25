package my.workflow.definition;

/**
 * ID 生成器
 */
public interface IProcessInstanceIdGenerator {
    String generate(ProcessDefinition processDefinition);
}
