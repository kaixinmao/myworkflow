package my.workflow.definition;

import com.alibaba.fastjson.JSONObject;
import my.workflow.process.ProcessInstance;
import my.workflow.process.factory.IProcessFactory;
import my.workflow.process.factory.IProcessFactoryMaterial;
import my.workflow.process.work.IWork;
import my.workflow.process.work.impl.Transition;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class DefinitionProcessFactory implements IProcessFactory {

    private DefinitionWorkFactory definitionWorkFactory;


    private DefinitionProcessFactory() {

    }

    public DefinitionWorkFactory getWorkFactory() {
        return definitionWorkFactory;
    }

    public String getName() {
        return "definition_factory";
    }

    public boolean isAvailableMaterial(IProcessFactoryMaterial material) {
        if (material == null) {
            return false;
        }

        if (material instanceof ProcessDefinition) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 给定数据生成ProcessInstance对象
     * @param material
     * @return
     */
    public ProcessInstance produce(String id, IProcessFactoryMaterial material) {
        if (!this.isAvailableMaterial(material) || this.definitionWorkFactory == null || StringUtils.isEmpty(id)) {
            return null;
        }

        ProcessDefinition definition = (ProcessDefinition) material;

        if (definition == null) {
            return null;
        }

        ProcessInstance instance = new ProcessInstance(id);

        //变量设置
        for (Map.Entry<String, JSONObject> entry : definition.getVariables().entrySet()) {
            instance.putVariable(entry.getKey(), entry.getValue());
        }

        Map<String, IWork> works = new HashMap<>();

        //实例化Work信息
        Map<String, WorkDefinition> workDefinitions = definition.getWorks();
        for (Map.Entry<String, WorkDefinition> entry : workDefinitions.entrySet()) {
            IWork work = this.definitionWorkFactory.createWork(entry.getValue());
            if (work != null) {
                //所有work都放进去
                instance.putWork(work);
                works.put(work.getId(), work);
            }
        }

        //处理transition问题
        for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
            IWork sourceWork = works.get(transitionDefinition.getSrc());
            IWork destinationWork = works.get(transitionDefinition.getDest());
            if (sourceWork == null || destinationWork == null) {
                //找不到对应的work是有问题的
                continue;
            }
            Transition transition = new Transition(sourceWork, destinationWork);
            sourceWork.addOutTransition(transition);
            destinationWork.addInTransition(transition);
        }

        //处理currentWork
        for (String currentWorkId : definition.getCurrentWorks()) {
            IWork work = works.get(currentWorkId);
            if (work == null) {
                continue;
            }
            instance.putCurrentWork(work);
        }

        instance.setFactory(this);
        instance.setFactoryMaterial(material);

        return instance;
    }

    /**
     * 自动将json转为 material 生产
     * @param jsonMaterial
     * @return
     */
    public ProcessInstance produce(String id, JSONObject jsonMaterial) {
        IProcessFactoryMaterial material = this.toFactoryMaterial(jsonMaterial);
        if (material == null) {
            return null;
        }

        return this.produce(id, material);
    }

    /**
     * 从JSONObject中生成工厂原料
     * @param jsonMaterial
     * @return
     */
    public IProcessFactoryMaterial toFactoryMaterial(JSONObject jsonMaterial) {
        if (jsonMaterial == null) {
            return null;
        }

        ProcessDefinition definition = ProcessDefinition.fromJSON(jsonMaterial);
        return definition;
    }

    public static class Builder {
        private DefinitionWorkFactory definitionWorkFactory;

        private Builder() {

        }

        public static DefinitionProcessFactory.Builder newFactory() {
            return new DefinitionProcessFactory.Builder();
        }

        public DefinitionProcessFactory.Builder setWorkFactory(DefinitionWorkFactory definitionWorkFactory) {
            this.definitionWorkFactory = definitionWorkFactory;
            return this;
        }

        public DefinitionProcessFactory build() {
            if (this.definitionWorkFactory == null) {
                return null;
            }

            DefinitionProcessFactory factory = new DefinitionProcessFactory();
            factory.definitionWorkFactory = this.definitionWorkFactory;

            return factory;
        }
    }
}
