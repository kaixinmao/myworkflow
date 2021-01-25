package my.workflow.process;

import com.alibaba.fastjson.JSONObject;
import my.workflow.common.ProcessStatusEnum;
import my.workflow.process.event.IProcessCustomEvent;
import my.workflow.process.event.IWorkRunnerListener;
import my.workflow.process.factory.FalseProcessFactory;
import my.workflow.process.factory.IProcessFactory;
import my.workflow.process.factory.IProcessFactoryMaterial;
import my.workflow.process.work.IWork;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ProcessInstance {

    /**
     * 全局唯一ID，同一definition可产生多个ProcessInstance
     */
    private String id;

    /**
     * 创建者类型标记
     */
    private IProcessFactory factory;

    /**
     * 创建者ID或相关标记信息
     */
    private IProcessFactoryMaterial factoryMaterial;

    private ProcessStatusEnum status;

    /**
     * 暂停后会被序列化保存的变量信息，对有暂停业务场景可以使用
     * 业务自身处理JSON对象的转换与应用
     * 注意内部不能出现不可被JSON序列化或者反序列化不能成功的对象
     */
    private Map<String, JSONObject> jsonVariables;

    /**
     * 全局唯一变量，对象被销毁跟着被销毁处理
     */
    private Map<String, Object> variables;

    private Map<String, IWorkRunnerListener> eventListeners;

    /**
     * 所有的works对象集合，方便查找处理
     */
    private Map<String, IWork> works;

    /**
     * 当前正在或者准备执行的Work
     */
    private Map<String, IWork> currentWorks;


    public ProcessInstance(String id) {
        this.id = id;
        this.currentWorks = new HashMap<>();
        this.works = new HashMap<>();
        this.eventListeners = new HashMap<>();
        this.jsonVariables = new HashMap<>();
        this.variables = new HashMap<>();
        this.status = ProcessStatusEnum.WAITING;
        this.factory = FalseProcessFactory.getInstance();
        this.factoryMaterial = null;
    }


    public String getId() {
        return this.id;
    }

    public IProcessFactory getFactory() {
        return factory;
    }

    public void setFactory(IProcessFactory factory) {
        this.factory = factory;
    }

    public IProcessFactoryMaterial getFactoryMaterial() {
        return factoryMaterial;
    }

    public void setFactoryMaterial(IProcessFactoryMaterial factoryMaterial) {
        this.factoryMaterial = factoryMaterial;
    }

    public Map<String ,IWork> getAllWorks() {
        return Collections.unmodifiableMap(this.works);
    }

    public void putWork(IWork work) {
        if (work == null) {
            return;
        }

        this.works.put(work.getId(), work);
    }

    public void removeWork(String workId) {
        this.works.remove(workId);
    }

    public IWork getWork(String workId) {
        return this.works.get(workId);
    }

    public Map<String, IWork> getCurrentWorks() {
        return Collections.unmodifiableMap(this.currentWorks);
    }

    public void putCurrentWork(IWork work) {
        if (work != null) {
            this.currentWorks.put(work.getId(), work);
        }
    }

    public void removeCurrentWork(String workId) {
        if (StringUtils.isEmpty(workId)) {
            return;
        }

        this.currentWorks.remove(workId);
    }

    public void clearCurrentWork() {
        this.currentWorks.clear();
    }

    public ProcessStatusEnum getStatus() {
        return this.status;
    }

    //注意并发操作
    public void setStatus(ProcessStatusEnum status) {
        this.status = status;
    }

    public void putJSONVariable(String varName, JSONObject value) {
        if (StringUtils.isEmpty(varName) || value == null) {
            return;
        }

        synchronized (this.jsonVariables) {
            this.jsonVariables.put(varName, value);
        }
    }

    public JSONObject getJSONVariable(String varName) {
        if (StringUtils.isEmpty(varName)) {
            return null;
        }

        return this.jsonVariables.get(varName);
    }

    public Map<String, JSONObject> getAllJSONVariables() {
        return Collections.unmodifiableMap(this.jsonVariables);
    }

    public void clearJSONVariables() {
        this.jsonVariables.clear();
    }

    public void putVariable(String varName, Object value) {
        if (StringUtils.isEmpty(varName) || value == null) {
            return;
        }
        this.variables.put(varName, value);
    }

    public Object getVariable(String varName) {
        if (StringUtils.isEmpty(varName)) {
            return null;
        }

        return this.variables.get(varName);
    }

    public void clearVariables() {
        this.variables.clear();
    }

    public Map<String, Object> getAllVariables() {
        return Collections.unmodifiableMap(this.variables);
    }

    /**
     * 注册Event事件监听器
     * @param id
     * @param listener
     */
    public void registerEventListener(String id, IWorkRunnerListener listener) {
        if (StringUtils.isEmpty(id) || listener == null) {
            return;
        }

        this.eventListeners.put(id, listener);
    }

    public void registerEventListener(IWorkRunnerListener listener) {
        if (listener == null) {
            return;
        }

        this.registerEventListener(listener.getClass().getName(), listener);
    }

    /**
     * 触发
     * @param event
     */
    public void postCustomEvent(IProcessCustomEvent event) {
        if(event == null) {
            return;
        }
    }
}
