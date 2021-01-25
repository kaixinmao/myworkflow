package my.workflow.process.factory;

import com.alibaba.fastjson.JSONObject;
import my.workflow.process.ProcessInstance;

public interface IProcessFactory {
    String getName();


    /**
     * 判断下是否为有效的原料
     * 调用该方法判断后，依然需要在produce时判断是否为空，保证产生正确的对象
     *
     * @param material
     * @return
     */
    boolean isAvailableMaterial(IProcessFactoryMaterial material);

    /**
     * 给定数据生成ProcessInstance对象
     * @param material
     * @return
     */
    ProcessInstance produce(String id, IProcessFactoryMaterial material);

    /**
     * 自动将json转为 material 生产
     * @param jsonMaterial
     * @return
     */
    ProcessInstance produce(String id, JSONObject jsonMaterial);

    /**
     * 从JSONObject中生成工厂原料
     * @param data
     * @return
     */
    IProcessFactoryMaterial toFactoryMaterial(JSONObject data);
}
