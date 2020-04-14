class Project {
    constructor(result) {
        this.result = result;

        this.dataUpToDate = true;
        this.tabsUpToDate = Array(5.).fill(true);
        this.externalRules = false;

        this.parameters = {
            consistencyThreshold: 0,
            defaultClassificationResult: "majorityDecisionClass",
            numberOfFolds: 2,
            typeOfClassifier: "SimpleRuleClassifier",
            typeOfRules: "certain",
            typeOfUnions: "monotonic"
        };
        this.parametersSaved = true;

        this.foldIndex = 0;

        this.settings = {
            indexOption: "default",
        };

        this.dataHistory = {
            historySnapshot: 0,
            history: []
        };
    }
}

export default Project