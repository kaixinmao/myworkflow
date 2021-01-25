package my.workflow.process.work.impl;

import my.workflow.process.work.ITransition;
import my.workflow.process.work.IWork;

public class Transition implements ITransition {

    private IWork srcWork;
    private IWork desWork;
    private boolean skipped;

    public Transition(IWork srcWork, IWork desWork) {
        this.srcWork = srcWork;
        this.desWork = desWork;
        this.skipped = false;
    }

    public IWork getSourceWork() {
        return this.srcWork;
    }

    public IWork getDestinationWork() {
        return this.desWork;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public Boolean isSkipped() {
        return this.skipped;
    }
}
