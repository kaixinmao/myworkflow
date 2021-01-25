package my.workflow.definition;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class WorkDefinition {
    private String id;
    private String type;
    /**
     * 配置参数
     */
    private JSONObject parameters;

    public WorkDefinition() {
        this.parameters = new JSONObject();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getParameters() {
        return parameters;
    }

    public void putParameter(String name, Object value) {
        if (StringUtils.isEmpty(name) || value == null) {
            return;
        }

        this.parameters.put(name, value);
    }


    public JSONObject toJSON() {
        JSONObject workJSONObject = (JSONObject) JSONObject.toJSON(this);

        return workJSONObject;
    }

    public static WorkDefinition fromJSON(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.isEmpty()) {
            return null;
        }

        String id = jsonObject.getString("id");
        String type = jsonObject.getString("type");
        JSONObject parameters = jsonObject.getJSONObject("parameters");

        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(type)) {
            return null;
        }

        WorkDefinition workDefinition = new WorkDefinition();
        workDefinition.id = id;
        workDefinition.type = type;
        if (parameters != null && !parameters.isEmpty()) {
            workDefinition.parameters = parameters;
        }

        return workDefinition;
    }
}
