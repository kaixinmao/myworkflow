package my.workflow.definition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import my.workflow.process.factory.IProcessFactoryMaterial;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ProcessDefinition implements IProcessFactoryMaterial {


    private Map<String, WorkDefinition> works;
    private List<TransitionDefinition> transitions;
    /**
     * 每次被Instance实例化的时候，就会放入到variables中，供使用
     * 因为每次实例化都存在，所以不用放到jsonVariables中被保存实例化
     */
    private Map<String, JSONObject> variables;

    private List<String> currentWorks;

    public ProcessDefinition() {
        this.works = new HashMap<>();
        this.transitions = new ArrayList<>();
        this.variables = new HashMap<>();
        this.currentWorks = new ArrayList<>();
    }

    public Map<String, WorkDefinition> getWorks() {
        return Collections.unmodifiableMap(this.works);
    }

    public void removeWork(String id) {
        this.works.remove(id);
    }

    public void addWork(WorkDefinition work) {
        if (work == null) {
            return;
        }

        this.works.put(work.getId(), work);
    }

    public List<TransitionDefinition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    public void addTransition(TransitionDefinition transitionDefinition) {
        if (transitionDefinition != null) {
            this.transitions.add(transitionDefinition);
        }
    }

    public Map<String, JSONObject> getVariables() {
        return Collections.unmodifiableMap(this.variables);
    }

    public void removeVariable(String name) {
        this.variables.remove(name);
    }

    public void addVariable(String name, JSONObject value) {
        if (StringUtils.isEmpty(name) || value == null) {
            return;
        }

        this.variables.put(name, value);
    }

    public List<String> getCurrentWorks() {
        return Collections.unmodifiableList(this.currentWorks);
    }

    public void addCurrentWorks(String workId) {
        if (StringUtils.isEmpty(workId)) {
            return;
        }

        if (!this.works.containsKey(workId)) {
            return;
        }

        this.currentWorks.add(workId);
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        JSONObject worksObject = new JSONObject();
        for (Map.Entry<String, WorkDefinition> entry : this.works.entrySet()) {
            WorkDefinition work = entry.getValue();
            worksObject.put(work.getId(), work.toJSON());
        }
        o.put("works", worksObject);

        o.put("variables", JSON.toJSON(this.variables));
        o.put("currentWorks", JSON.toJSON(this.currentWorks));
        o.put("transitions", JSON.toJSON(this.transitions));

        return o;
    }


    /**
     *
     * @param jsonObject
     * @return
     */
    public static ProcessDefinition fromJSON(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.isEmpty()) {
            return null;
        }

        ProcessDefinition processDefinition = new ProcessDefinition();

        //处理works属性
        JSONObject worksJSONObject = jsonObject.getJSONObject("works");
        Map<String, WorkDefinition> works = parseWorkDefinitions(worksJSONObject);
        if (!works.isEmpty()) {
            processDefinition.works = works;
        }

        //处理transitions属性
        JSONArray transitionsJSONArray = jsonObject.getJSONArray("transitions");
        if (transitionsJSONArray != null && !transitionsJSONArray.isEmpty()) {
            List<TransitionDefinition> transitionDefinitions = transitionsJSONArray.toJavaList(TransitionDefinition.class);
            if (!CollectionUtils.isEmpty(transitionDefinitions)) {
                processDefinition.transitions.addAll(transitionDefinitions);
            }
        }

        //处理constVariables
        JSONObject variablesJSONObject = jsonObject.getJSONObject("constVariables");
        Map<String, JSONObject> variables = parseConstVariables(variablesJSONObject);
        if (!variables.isEmpty()) {
            processDefinition.variables = variables;
        }


        //处理currentWorks设置
        JSONArray currentWorksJSONArray = jsonObject.getJSONArray("currentWorks");
        if (currentWorksJSONArray != null && !currentWorksJSONArray.isEmpty()) {
            List<String> currentWorks = currentWorksJSONArray.toJavaList(String.class);
            if (!CollectionUtils.isEmpty(currentWorks)) {
                for (String workId : currentWorks) {
                    //检查是否在定义中存在
                    if (processDefinition.works.containsKey(workId)) {
                        processDefinition.currentWorks.add(workId);
                    }
                }
            }
        }

        return processDefinition;
    }

    public static ProcessDefinition fromJSON(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        if (jsonObject == null) {
            return null;
        }

        return fromJSON(jsonObject);

    }

    private static Map<String, WorkDefinition> parseWorkDefinitions(JSONObject jsonObject) {
        Map<String, WorkDefinition> res = new HashMap<>();

        if (jsonObject == null || jsonObject.isEmpty()) {
            return res;
        }

        for (String key : jsonObject.keySet()) {
            JSONObject workDefinitionJSONObject = jsonObject.getJSONObject(key);
            if (workDefinitionJSONObject == null) {
                //TODO 出错了，直接跳过
                continue;
            }

            WorkDefinition workDefinition = WorkDefinition.fromJSON(workDefinitionJSONObject);
            if (workDefinition == null) {
                continue;
            }
            res.put(workDefinition.getId(), workDefinition);
        }

        return res;
    }

    private static Map<String, JSONObject> parseConstVariables(JSONObject jsonObject) {

        Map<String, JSONObject> variables = new HashMap<>();

        if (jsonObject == null || jsonObject.isEmpty()) {
            return variables;
        }

        for (String key : jsonObject.keySet()) {
            JSONObject v = jsonObject.getJSONObject(key);
            if (v == null) {
                continue;
            }

            variables.put(key, v);
        }

        return variables;
    }
}
