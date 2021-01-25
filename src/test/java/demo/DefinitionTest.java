package demo;

import com.alibaba.fastjson.JSON;
import my.workflow.definition.DefinitionProcessFactory;
import my.workflow.engine.impl.LocalThreadWorkFlowEngine;
import my.workflow.process.ProcessInstance;
import my.workflow.definition.ProcessDefinition;
import my.workflow.definition.DefinitionWorkFactory;
import my.workflow.process.factory.IProcessFactory;
import my.workflow.process.factory.IProcessFactoryManager;
import my.workflow.process.factory.ProcessFactoryManagerImpl;
import my.workflow.storage.memory.MemoryPauseStorage;
import works.builder.HelloWorkBuilder;
import works.builder.VarSetWorkBuilder;

public class DefinitionTest {


    //@Test
    public void conditionTest() {
        IProcessFactoryManager factoryManager = getFactoryManager();
        IProcessFactory factory = factoryManager.getProcessFactory("definition_factory");
        ProcessDefinition definition = ProcessDefinition.fromJSON(JSON.parseObject(getConditionalProcessDefinition()));
        ProcessInstance instance = factory.produce("for_test", definition);

        LocalThreadWorkFlowEngine engine = LocalThreadWorkFlowEngine.Builder.newEngine()
                .setPauseStorage(new MemoryPauseStorage(0)).setProcessFactoryManager(factoryManager)
                .build();
        engine.startProcessInstance(instance);
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            return;
        }
        System.out.printf("ok\n");
    }

    public static IProcessFactoryManager getFactoryManager() {
        ProcessFactoryManagerImpl manager = new ProcessFactoryManagerImpl();

        DefinitionWorkFactory workFactory = new DefinitionWorkFactory();
        workFactory.registerWorkBuilder(new HelloWorkBuilder());
        workFactory.registerWorkBuilder(new VarSetWorkBuilder());

        DefinitionProcessFactory processFactory = DefinitionProcessFactory.Builder.newFactory().setWorkFactory(workFactory).build();

        manager.registerProcessFactory(processFactory);

        return manager;
    }

    public static String getConditionalProcessDefinition() {
        return "{\n" +
                "    \"works\": {\n" +
                "        \"1\": {\n" +
                "            \"id\": \"1\",\n" +
                "            \"type\" : \"varset\",\n" +
                "            \"parameters\": {\n" +
                "                \"tiaojian\" : true\n" +
                "            }\n" +
                "        },\n" +
                "        \"2\": {\n" +
                "            \"id\": \"2\",\n" +
                "            \"type\" : \"hello\",\n" +
                "            \"parameters\": {\n" +
                "                \"helloWhat\" : \"ggggg\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"3\": {\n" +
                "            \"id\": \"3\",\n" +
                "            \"type\" : \"hello\",\n" +
                "            \"parameters\": {\n" +
                "                \"helloWhat\" : \"cccc\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"condition\" : {\n" +
                "            \"id\": \"condition\",\n" +
                "            \"type\" : \"conditional\",\n" +
                "            \"parameters\": {\n" +
                "                \"varName\" : \"tiaojian\",\n" +
                "                \"succWorkId\" : \"3\"\n" +
                "            }\n" +
                "        }\n" +
                "    }, \n" +
                "    \"transitions\" : [\n" +
                "        {\n" +
                "            \"src\" : \"1\",\n" +
                "            \"dest\" : \"condition\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"src\" : \"condition\",\n" +
                "            \"dest\" : \"2\"\n" +
                "        }\n" +
                "        ,\n" +
                "        {\n" +
                "            \"src\" : \"condition\",\n" +
                "            \"dest\" : \"3\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"currentWorks\" : [\n" +
                "        \"1\"\n" +
                "    ]\n" +
                "}";
    }
}
