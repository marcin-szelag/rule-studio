package pl.put.poznan.rulestudio.model.parameters;

import pl.put.poznan.rulestudio.enums.RuleType;

public interface RulesParameters extends ClassUnionsParameters {

    RuleType getTypeOfRules();
    
    String getFilterSelector();

    Boolean equalsTo(RulesParameters that);
}
