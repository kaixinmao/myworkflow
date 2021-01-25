package my.workflow.storage;

import my.workflow.process.ProcessInstance;

public interface IPauseStorage {

    /**
     * 根据ProcessInstance的ID从存储中获取对象
     * @param id
     * @return
     */
    ProcessInstance getProcessInstanceById(String id);

    /**
     * 删除ProcessInstance数据
     * @param id
     */
    void delProcessInstanceById(String id);


    /**
     * 序列化并保存ProcessInstance数据，每次执行均将覆盖
     * @param instance
     */
    void saveProcessInstance(ProcessInstance instance);

}
