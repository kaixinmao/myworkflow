package my.workflow.process.work;

public interface ITransition {
    IWork getSourceWork();

    IWork getDestinationWork();

    void setSkipped(boolean skipped);

    Boolean isSkipped();
}
