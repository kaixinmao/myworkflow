package common;

import com.alibaba.fastjson.JSONObject;
import my.workflow.process.ProcessInstance;
import my.workflow.process.work.IWork;
import my.workflow.process.work.impl.Transition;
import works.HelloWork;
import works.JSONVarSetWork;
import works.SleepWork;

public class DemoProcessInstanceBuilder {


    public static ProcessInstance instanceWithHelloWork() {
        ProcessInstance instance = new ProcessInstance("hello_work");
        instance.putCurrentWork(DemoProcessInstanceBuilder.getHelloWorkChain());

        return instance;
    }

    public static ProcessInstance instanceWithJSONVarWork() {
        ProcessInstance instance = new ProcessInstance("jsonvar_work");
        JSONVarSetWork jsonVarWork = new JSONVarSetWork("jsonvar");
        JSONObject value = new JSONObject();
        value.put("data", "the value");
        jsonVarWork.setValue("hello_str", value);

        SleepWork sleepWork = new SleepWork("sleep_work", 3);

        jsonVarWork.addOutTransition(new Transition(jsonVarWork, sleepWork));

        IWork helloWorkChain = getHelloWorkChain();
        sleepWork.addOutTransition(new Transition(sleepWork, helloWorkChain));

        instance.putCurrentWork(jsonVarWork);

        return instance;
    }
    

    private static IWork getHelloWorkChain() {
        IWork beginningWork = buildBaseHelloWorkChain(1, 10);

        int index = 11;

        IWork secondWork = beginningWork.getOutTransitions().get(0).getDestinationWork();

        //再增加几个并发的链
        IWork aWorks = buildBaseHelloWorkChain(index, 5);
        index = index + 5;

        IWork bWorks = buildBaseHelloWorkChain(index, 6);
        index = index + 6;

        secondWork.addOutTransition(new Transition(secondWork, aWorks));
        secondWork.addOutTransition(new Transition(secondWork, bWorks));

        return beginningWork;
    }

    private static IWork buildBaseHelloWorkChain(int index, int num) {
        HelloWork beginningWork = new HelloWork(String.valueOf(index), String.valueOf(index));
        HelloWork work = beginningWork;

        int i = index;
        while(i < index + num - 1) {
            i++;
            HelloWork nextWork = new HelloWork(String.valueOf(i), String.valueOf(i));
            Transition transition = new Transition(work, nextWork);
            work.addOutTransition(transition);
            nextWork.addInTransition(transition);
            work = nextWork;
        }

        return beginningWork;
    }

    private static IWork getSleepWork(String id, int seconds) {
        if (seconds < 0) {
            seconds = 5;
        }

        return new SleepWork(id, seconds);
    }
}
