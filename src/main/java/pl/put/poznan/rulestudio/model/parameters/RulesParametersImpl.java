package pl.put.poznan.rulestudio.model.parameters;

import pl.put.poznan.rulestudio.enums.RuleType;
import pl.put.poznan.rulestudio.enums.UnionType;

public class RulesParametersImpl extends ClassUnionsParametersImpl implements RulesParameters {

    private RuleType typeOfRules;
    private String filterSelector;

    private RulesParametersImpl(UnionType typeOfUnions, Double consistencyThreshold, RuleType typeOfRules, String filterSelector) {
        super(typeOfUnions, consistencyThreshold);
        this.typeOfRules = typeOfRules;
        this.filterSelector = filterSelector;
    }

    public static RulesParametersImpl getInstance(UnionType typeOfUnions, Double consistencyThreshold, RuleType typeOfRules, String filterSelector) {
        if (typeOfUnions == null) {
            return null;
        }

        return new RulesParametersImpl(typeOfUnions, consistencyThreshold, typeOfRules, filterSelector);
    }

    @Override
    public RuleType getTypeOfRules() {
        return typeOfRules;
    }
    
    @Override
	public String getFilterSelector() {
		return filterSelector;
	}

    @Override
    public Boolean equalsTo(RulesParameters that) {
        if (that == null) return false;
        if (!super.equalsTo(that)) return false;
        return this.getTypeOfRules() == that.getTypeOfRules() && this.getFilterSelector().equals(that.getFilterSelector());
    }

    @Override
    public String toString() {
        return "RulesParametersImpl{" +
                "typeOfRules=" + typeOfRules +
                ", filterSelector=" + filterSelector +
                "} " + super.toString();
    }

}
