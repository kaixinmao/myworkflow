package my.workflow.definition;

import my.workflow.mywork.builder.ConditionalWorkBuilder;
import my.workflow.process.work.IWork;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 主要从WorkDefinition创建Work对象
 */
public class DefinitionWorkFactory {
    private Map<String, IWorkTypeBuilder> workBuilders;

    public DefinitionWorkFactory() {
        this(true);
    }

    public DefinitionWorkFactory(boolean registerMyWork) {
        this.workBuilders = new HashMap<>();
        if (registerMyWork) {
            this.registerMyWorkBuilders();
        }
    }

    /**
     * 注册支撑的Work类型
     * @param workBuilder
     */
    public void registerWorkBuilder(IWorkTypeBuilder workBuilder) {
        if (workBuilder == null) {
            return;
        }

        this.workBuilders.put(workBuilder.getType(), workBuilder);
    }

    public void removeWorkBuilder(String type) {
        if (StringUtils.isEmpty(type)) {
            return;
        }

        this.workBuilders.remove(type);
    }

    public IWork createWork(WorkDefinition workDefinition) {
        if (workDefinition == null) {
            return null;
        }

        if (!this.workBuilders.containsKey(workDefinition.getType())) {
            return null;
        }

        return this.workBuilders.get(workDefinition.getType()).build(workDefinition);
    }

    /**
     * 注册一些默认的Builder
     */
    private void registerMyWorkBuilders() {
        this.registerWorkBuilder(new ConditionalWorkBuilder());
    }
}
