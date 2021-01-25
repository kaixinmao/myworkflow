package my.workflow.process.factory;

import org.apache.commons.lang3.StringUtils;

public interface IProcessFactoryManager {
    IProcessFactory getProcessFactory(String factoryName);

    void registerProcessFactory(IProcessFactory processFactory);
}
