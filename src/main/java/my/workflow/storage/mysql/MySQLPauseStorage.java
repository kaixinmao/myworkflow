package my.workflow.storage.mysql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import my.workflow.process.ProcessInstance;
import my.workflow.definition.ProcessDefinition;
import my.workflow.process.factory.IProcessFactory;
import my.workflow.process.factory.IProcessFactoryManager;
import my.workflow.process.utils.ProcessInstanceUtils;
import my.workflow.storage.IPauseStorage;
import my.workflow.storage.mysql.dao.ProcessInstanceDOMapper;
import my.workflow.storage.mysql.model.ProcessInstanceDO;
import my.workflow.storage.mysql.model.ProcessInstanceDOExample;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class MySQLPauseStorage implements IPauseStorage {


    private IProcessFactoryManager processFactoryManager;

    private ProcessInstanceDOMapper processInstanceDOMapper;

    public MySQLPauseStorage() {

    }

    public ProcessInstance getProcessInstanceById(String processId) {
        if (StringUtils.isEmpty(processId)) {
            return null;
        }

        ProcessInstanceDOExample example = new ProcessInstanceDOExample();
        example.createCriteria().andProcessIdEqualTo(processId);
        ProcessInstanceDO processInstanceDO = this.processInstanceDOMapper.selectOneByExampleWithBLOBs(example);
        if (processInstanceDO == null) {
            return null;
        }

        IProcessFactory processFactory = this.processFactoryManager.getProcessFactory(processInstanceDO.getFactory());
        if (processFactory == null) {
            //XX 找不到对应的Factory
            return null;
        }

        String materialString = processInstanceDO.getFactoryMaterial();
        if (StringUtils.isEmpty(materialString)) {
            return null;
        }

        JSONObject materialJsonObject = JSON.parseObject(materialString);
        if (materialJsonObject == null) {
            return null;
        }

        ProcessInstance processInstance = processFactory.produce(processInstanceDO.getProcessId(), materialJsonObject);
        if (processInstance == null) {
            return null;
        }

        JSONObject runtimeData = JSON.parseObject(processInstanceDO.getRuntimeData());
        if (runtimeData == null || runtimeData.isEmpty()) {
            return null;
        }

        ProcessInstanceUtils.restoreRuntimeDataFromJSON(processInstance, runtimeData);

        return processInstance;
    }

    public void delProcessInstanceById(String processId) {
        if (StringUtils.isEmpty(processId)) {
            return;
        }

        ProcessInstanceDOExample example = new ProcessInstanceDOExample();
        example.createCriteria().andProcessIdEqualTo(processId);
        this.processInstanceDOMapper.deleteByExample(example);
    }

    public void saveProcessInstance(ProcessInstance instance) {
        if (instance == null) {
            return;
        }

        ProcessInstanceDOExample example = new ProcessInstanceDOExample();
        example.createCriteria().andProcessIdEqualTo(instance.getId());

        JSONObject runtimeData = ProcessInstanceUtils.runtimeDataToJSON(instance);
        Date now = new Date();

        ProcessInstanceDO processInstanceDO = this.processInstanceDOMapper.selectOneByExample(example);
        if (processInstanceDO == null) {
            processInstanceDO = new ProcessInstanceDO();
            processInstanceDO.setFactory(instance.getFactory().getName());
            processInstanceDO.setFactoryMaterial(instance.getFactoryMaterial().toJSON().toString());
            processInstanceDO.setProcessId(instance.getId());
            processInstanceDO.setRuntimeData(runtimeData.toString());
            processInstanceDO.setStatus(instance.getStatus().getCode());
            processInstanceDO.setGmtModified(now);
            processInstanceDO.setGmtCreate(now);

            this.processInstanceDOMapper.insert(processInstanceDO);
            //XXX 错误处理
        } else {
            processInstanceDO.setRuntimeData(runtimeData.toString());
            processInstanceDO.setGmtModified(now);
            processInstanceDO.setStatus(instance.getStatus().getCode());
            this.processInstanceDOMapper.updateByExampleWithBLOBs(processInstanceDO, example);
        }
    }

    public static class Builder {
        private ProcessInstanceDOMapper processInstanceDOMapper;
        private IProcessFactoryManager processFactoryManager;

        private Builder() {

        }

        public static MySQLPauseStorage.Builder newMySQLStorage() {
            return new Builder();
        }

        public MySQLPauseStorage.Builder setProcessInstanceDOMapper(ProcessInstanceDOMapper processInstanceDOMapper) {
            this.processInstanceDOMapper = processInstanceDOMapper;
            return this;
        }

        public MySQLPauseStorage.Builder setProcessFactoryManager(IProcessFactoryManager processFactoryManager) {
            this.processFactoryManager = processFactoryManager;
            return this;
        }

        public MySQLPauseStorage build() {
            if (this.processFactoryManager == null || this.processInstanceDOMapper == null) {
                return null;
            }
            MySQLPauseStorage storage = new MySQLPauseStorage();
            storage.processInstanceDOMapper = this.processInstanceDOMapper;
            storage.processFactoryManager = this.processFactoryManager;

            return storage;
        }
    }
}
