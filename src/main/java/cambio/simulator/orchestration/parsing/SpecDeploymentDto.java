package cambio.simulator.orchestration.parsing;

public class SpecDeploymentDto {
    private SelectorDto selector;
    private int replicas;
    private TemplateDto template;

    public SelectorDto getSelector() {
        return selector;
    }

    public void setSelector(SelectorDto selector) {
        this.selector = selector;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public TemplateDto getTemplate() {
        return template;
    }

    public void setTemplate(TemplateDto template) {
        this.template = template;
    }
}
