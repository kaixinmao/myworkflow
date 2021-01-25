package my.workflow.process.factory;

import com.alibaba.fastjson.JSONObject;
import my.workflow.process.ProcessInstance;

/**
 * 假工厂，默认的ProcessInstance将会设置为该值，统一结构
 */
public class FalseProcessFactory implements IProcessFactory {

    private static FalseProcessFactory instance;


    public String getName() {
        return "false_factory";
    }


    public boolean isAvailableMaterial(IProcessFactoryMaterial material) {
        return false;
    }

    public ProcessInstance produce(String id, IProcessFactoryMaterial material) {
        return null;
    }

    /**
     * 自动将json转为 material 生产
     * @param jsonMaterial
     * @return
     */
    public ProcessInstance produce(String id, JSONObject jsonMaterial) {
        return null;
    }

    /**
     * 从JSONObject中生成工厂原料
     * @param data
     * @return
     */
    public IProcessFactoryMaterial toFactoryMaterial(JSONObject data) {
        return null;
    }

    public static IProcessFactory getInstance() {
        if (instance == null) {
            instance = new FalseProcessFactory();
        }

        return instance;
    }
}
