package demo;

import common.DemoProcessInstanceBuilder;
import my.workflow.engine.IWorkFlowEngine;
import my.workflow.engine.impl.LocalThreadWorkFlowEngine;
import my.workflow.storage.memory.MemoryPauseStorage;

public class TestEngineJob {
    private IWorkFlowEngine engine;

    public TestEngineJob() {
        LocalThreadWorkFlowEngine engine = LocalThreadWorkFlowEngine.Builder.newEngine()
                .setProcessFactoryManager(null).setPauseStorage(new MemoryPauseStorage(0))
                .build();
        this.engine = engine;
    }

    //@Test
    public void TestRunHelloWork() {
        this.engine.startProcessInstance(DemoProcessInstanceBuilder.instanceWithHelloWork());
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            return;
        }
    }

    //@Test
    public void TestJsonVarWork() {
        this.engine.startProcessInstance(DemoProcessInstanceBuilder.instanceWithJSONVarWork());

        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            return;
        }
    }
}
