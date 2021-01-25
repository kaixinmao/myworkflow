package my.workflow.process.utils;

import com.alibaba.fastjson.JSONObject;
import my.workflow.common.ProcessStatusEnum;
import my.workflow.process.ProcessInstance;
import my.workflow.process.work.IWork;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessInstanceUtils {

    /**
     * 运行时数据转换为JSON方便处理
     * @param instance
     * @return
     */
    public static JSONObject runtimeDataToJSON(ProcessInstance instance) {

        JSONObject jsonVariables = new JSONObject();
        Map<String, JSONObject> instanceJsonVariables = instance.getAllJSONVariables();
        if (instanceJsonVariables != null && !instanceJsonVariables.isEmpty()) {
            for (Map.Entry<String, JSONObject> entry : instanceJsonVariables.entrySet()) {
                jsonVariables.put(entry.getKey(), entry.getValue());
            }
        }

        Map<String, IWork> currentWorks = instance.getCurrentWorks();

        //XXX 注意并发风险，后续再处理。
        JSONObject ret = new JSONObject();
        ret.put("jsonVariables", jsonVariables);
        ret.put("status", instance.getStatus().getCode());
        ret.put("currentWorks", currentWorks.keySet());
        return ret;
    }

    public static void restoreRuntimeDataFromJSON(ProcessInstance instance, JSONObject data) {
        if (instance == null) {
            return;
        }

        //清除当前状态信息
        instance.clearCurrentWork();
        instance.clearJSONVariables();
        instance.clearVariables();

        JSONObject variables = data.getJSONObject("jsonVariables");
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                Object v = entry.getValue();
                if (v instanceof JSONObject) {
                    instance.putJSONVariable(entry.getKey(), (JSONObject) v);
                }
            }
        }


        Integer statusInt = data.getInteger("status");
        instance.setStatus( ProcessStatusEnum.parseFromCode(statusInt));

        List<String> workIds = data.getJSONArray("currentWorks").toJavaList(String.class);

        if (!CollectionUtils.isEmpty(workIds)) {
            for (String workId : workIds) {
                if (instance.getWork(workId) == null) {
                    continue;
                }
                instance.putCurrentWork(instance.getWork(workId));
            }
        }
    }
}
