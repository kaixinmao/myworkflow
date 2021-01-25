package my.workflow.storage.memory;

import my.workflow.process.ProcessInstance;
import my.workflow.storage.IPauseStorage;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存存储ProcessInstance信息，其实就是Map保存
 *
 * ×××需要非常注意内存的持续增长，只能用于试验场景×××
 */
public class MemoryPauseStorage implements IPauseStorage {
    private Map<String, ProcessInstance> instancePool;
    private long maxInstanceNum;

    public MemoryPauseStorage(int maxInstanceNum) {
        this.maxInstanceNum = maxInstanceNum;
        this.instancePool = new ConcurrentHashMap<>();
    }

    public ProcessInstance getProcessInstanceById(String processId) {
        if (StringUtils.isEmpty(processId) || !this.instancePool.containsKey(processId)) {
            return null;
        }

        return this.instancePool.get(processId);
    }

    public void delProcessInstanceById(String id) {
        if (StringUtils.isEmpty(id)) {
            return;
        }

        this.instancePool.remove(id);
    }

    public void saveProcessInstance(ProcessInstance instance) {
        if (instance == null || StringUtils.isEmpty(instance.getId())) {
            return;
        }

        if (this.maxInstanceNum <= 0 || this.instancePool.size() < this.maxInstanceNum) {
            this.instancePool.put(instance.getId(), instance);
        }
    }
}
