package my.workflow.process.factory;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ProcessFactoryManagerImpl implements IProcessFactoryManager {
    private Map<String, IProcessFactory> processFactories;

    public ProcessFactoryManagerImpl() {
        this.processFactories = new HashMap<>();
    }

    public IProcessFactory getProcessFactory(String factoryName) {
        if (StringUtils.isEmpty(factoryName)) {
            return null;
        }

        if (this.processFactories.containsKey(factoryName)) {
            return this.processFactories.get(factoryName);
        }

        return null;
    }

    public void registerProcessFactory(IProcessFactory processFactory) {
        if (processFactory == null) {
            return;
        }

        this.processFactories.put(processFactory.getName(), processFactory);
    }
}
