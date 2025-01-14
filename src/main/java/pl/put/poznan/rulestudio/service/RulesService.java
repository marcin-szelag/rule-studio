package pl.put.poznan.rulestudio.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import org.rulelearn.approximations.Union;
import org.rulelearn.approximations.Unions;
import org.rulelearn.approximations.VCDominanceBasedRoughSetCalculator;
import org.rulelearn.core.UnknownValueException;
import org.rulelearn.data.Attribute;
import org.rulelearn.data.InformationTable;
import org.rulelearn.measures.dominance.EpsilonConsistencyMeasure;
import org.rulelearn.rules.ApproximatedSetProvider;
import org.rulelearn.rules.ApproximatedSetRuleDecisionsProvider;
import org.rulelearn.rules.AttributeOrderRuleConditionsPruner;
import org.rulelearn.rules.BasicRuleCoverageInformation;
import org.rulelearn.rules.CertainRuleInducerComponents;
import org.rulelearn.rules.CompositeRuleCharacteristicsFilter;
import org.rulelearn.rules.EvaluationAndCoverageStoppingConditionChecker;
import org.rulelearn.rules.OptimizingRuleConditionsGeneralizer;
import org.rulelearn.rules.PossibleRuleInducerComponents;
import org.rulelearn.rules.Rule;
import org.rulelearn.rules.RuleCharacteristics;
import org.rulelearn.rules.RuleConditions;
import org.rulelearn.rules.RuleCoverageInformation;
import org.rulelearn.rules.RuleInducerComponents;
import org.rulelearn.rules.RuleInductionStoppingConditionChecker;
import org.rulelearn.rules.RuleSemantics;
import org.rulelearn.rules.RuleSetWithCharacteristics;
import org.rulelearn.rules.RuleSetWithComputableCharacteristics;
import org.rulelearn.rules.UnionProvider;
import org.rulelearn.rules.UnionWithSingleLimitingDecisionRuleDecisionsProvider;
import org.rulelearn.rules.VCDomLEM;
import org.rulelearn.rules.ruleml.RuleMLBuilder;
import org.rulelearn.rules.ruleml.RuleParseException;
import org.rulelearn.rules.ruleml.RuleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import pl.put.poznan.rulestudio.enums.OrderByRuleCharacteristic;
import pl.put.poznan.rulestudio.enums.RuleType;
import pl.put.poznan.rulestudio.enums.RulesFormat;
import pl.put.poznan.rulestudio.exception.EmptyResponseException;
import pl.put.poznan.rulestudio.exception.NoDataException;
import pl.put.poznan.rulestudio.exception.NoRulesException;
import pl.put.poznan.rulestudio.exception.NotSuitableForInductionOfPossibleRulesException;
import pl.put.poznan.rulestudio.exception.WrongParameterException;
import pl.put.poznan.rulestudio.model.CalculationsStopWatch;
import pl.put.poznan.rulestudio.model.DescriptiveAttributes;
import pl.put.poznan.rulestudio.model.NamedResource;
import pl.put.poznan.rulestudio.model.Project;
import pl.put.poznan.rulestudio.model.ProjectClassUnions;
import pl.put.poznan.rulestudio.model.ProjectRules;
import pl.put.poznan.rulestudio.model.ProjectsContainer;
import pl.put.poznan.rulestudio.model.RuLeStudioRule;
import pl.put.poznan.rulestudio.model.RuLeStudioRuleSet;
import pl.put.poznan.rulestudio.model.ValidityRulesContainer;
import pl.put.poznan.rulestudio.model.parameters.RulesParameters;
import pl.put.poznan.rulestudio.model.response.AttributeFieldsResponse;
import pl.put.poznan.rulestudio.model.response.AttributeFieldsResponse.AttributeFieldsResponseBuilder;
import pl.put.poznan.rulestudio.model.response.ChosenRuleResponse;
import pl.put.poznan.rulestudio.model.response.ChosenRuleResponse.ChosenRuleResponseBuilder;
import pl.put.poznan.rulestudio.model.response.DescriptiveAttributesResponse;
import pl.put.poznan.rulestudio.model.response.MainRulesResponse;
import pl.put.poznan.rulestudio.model.response.MainRulesResponse.MainRulesResponseBuilder;
import pl.put.poznan.rulestudio.model.response.ObjectAbstractResponse;
import pl.put.poznan.rulestudio.model.response.ObjectResponse;
import pl.put.poznan.rulestudio.model.response.ObjectWithAttributesResponse;

@Service
public class RulesService {

    private static final Logger logger = LoggerFactory.getLogger(RulesService.class);

    @Autowired
    ProjectsContainer projectsContainer;

    private class ArrayIndexComparator implements Comparator<Integer> {
        private final Number[] array;

        public ArrayIndexComparator(Number[] array) {
            this.array = array;
        }

        public Integer[] createIndexArray()
        {
            Integer[] indices = new Integer[array.length];
            for (int i = 0; i < array.length; i++)
            {
                indices[i] = i;
            }
            return indices;
        }

        @Override
        public int compare(Integer ind1, Integer ind2) {
            if (array[ind1] instanceof Integer) {
                Integer val1 = (Integer)array[ind1];
                Integer val2 = (Integer)array[ind2];

                if ((val1.equals(RuleCharacteristics.UNKNOWN_INT_VALUE)) && (val2.equals(RuleCharacteristics.UNKNOWN_INT_VALUE))) {
                    return ind1.compareTo(ind2);
                }
                if (val1.equals(RuleCharacteristics.UNKNOWN_INT_VALUE)) {
                    return -1;
                }
                if (val2.equals(RuleCharacteristics.UNKNOWN_INT_VALUE)) {
                    return 1;
                }
                return val1.compareTo(val2);

            } else if (array[ind1] instanceof Double) {
                Double val1 = (Double)array[ind1];
                Double val2 = (Double)array[ind2];

                if ((val1.equals(RuleCharacteristics.UNKNOWN_DOUBLE_VALUE)) && (val2.equals(RuleCharacteristics.UNKNOWN_DOUBLE_VALUE))) {
                    return ind1.compareTo(ind2);
                }
                if (val1.equals(RuleCharacteristics.UNKNOWN_DOUBLE_VALUE)) {
                    return -1;
                }
                if (val2.equals(RuleCharacteristics.UNKNOWN_DOUBLE_VALUE)) {
                    return 1;
                }
                return val1.compareTo(val2);
            }

            return ind1.compareTo(ind2);
        }
    }

    interface NumberCharacteristic {
        Number get(int index);
    }

    private static void collectIntegerCharacteristicLoop(int rulesNumber, Number[] characteristicValues, NumberCharacteristic function) {
        for(int i = 0; i < rulesNumber; i++) {
            try {
                characteristicValues[i] = function.get(i);
            } catch (UnknownValueException e)  {
                logger.debug(e.getMessage());
                characteristicValues[i] = RuleCharacteristics.UNKNOWN_INT_VALUE;
            }
        }
    }

    private static void collectDoubleCharacteristicLoop(int rulesNumber, Number[] characteristicValues, NumberCharacteristic function) {
        for(int i = 0; i < rulesNumber; i++) {
            try {
                characteristicValues[i] = function.get(i);
            } catch (UnknownValueException e)  {
                logger.debug(e.getMessage());
                characteristicValues[i] = RuleCharacteristics.UNKNOWN_DOUBLE_VALUE;
            }
        }
    }

    public static RuleSetWithComputableCharacteristics parseComputableRules(MultipartFile rulesFile, Attribute[] attributes) throws IOException {
        Map<Integer, RuleSetWithCharacteristics> parsedRules = null;
        RuleParser ruleParser = new RuleParser(attributes);
        parsedRules = ruleParser.parseRulesWithCharacteristics(rulesFile.getInputStream());

        for(RuleSetWithCharacteristics rswc : parsedRules.values()) {
            logger.info("ruleSet.size=" + rswc.size());
            if(logger.isDebugEnabled()) {
                for(int i = 0; i < rswc.size(); i++) {
                    RuleCharacteristics ruleCharacteristics = rswc.getRuleCharacteristics(i);
                    logger.debug(i + ":\t" + ruleCharacteristics.toString());
                }
            }
        }

        Map.Entry<Integer, RuleSetWithCharacteristics> entry = parsedRules.entrySet().iterator().next();
        RuleSetWithCharacteristics ruleSetWithCharacteristics = entry.getValue();

        Rule[] rules = new Rule[ruleSetWithCharacteristics.size()];
        for(int i = 0; i < ruleSetWithCharacteristics.size(); i++) {
            rules[i] = ruleSetWithCharacteristics.getRule(i);
        }

        RuleCoverageInformation[] ruleCoverageInformation = new RuleCoverageInformation[ruleSetWithCharacteristics.size()];
        for(int i = 0; i < ruleSetWithCharacteristics.size(); i++) {
            RuleConditions ruleConditions = new RuleConditions(new InformationTable(new Attribute[0], new ArrayList<>()), new IntArraySet(), new IntArraySet(), new IntArraySet(), org.rulelearn.rules.RuleType.POSSIBLE, RuleSemantics.AT_MOST);
            ruleCoverageInformation[i] = new RuleCoverageInformation(ruleConditions);
        }

        return new RuleSetWithComputableCharacteristics(
                rules,
                ruleCoverageInformation
        );
    }

    public static RuleSetWithCharacteristics parseRules(MultipartFile rulesFile, Attribute[] attributes) throws IOException {
        Map<Integer, RuleSetWithCharacteristics> parsedRules = null;
        RuleParser ruleParser = new RuleParser(attributes);
        try {
            parsedRules = ruleParser.parseRulesWithCharacteristics(rulesFile.getInputStream());
        } catch (RuleParseException e) {
            WrongParameterException ex = new WrongParameterException(e.getMessage());
            logger.error(ex.getMessage());
            throw ex;
        }

        if(parsedRules == null) {
            WrongParameterException ex = new WrongParameterException(String.format("Given file with rules could not be successfully read as RuleML file."));
            logger.error(ex.getMessage());
            throw ex;
        } else if (parsedRules.entrySet().iterator().next().getValue().size() == 0) {
            WrongParameterException ex = new WrongParameterException(String.format("Parser could not process any rule. Make sure that the file is not empty and its content is compatible with current project's metadata."));
            logger.error(ex.getMessage());
            throw ex;
        }

        for(RuleSetWithCharacteristics rswc : parsedRules.values()) {
            logger.info("ruleSet.size=" + rswc.size());
            if(logger.isDebugEnabled()) {
                for(int i = 0; i < rswc.size(); i++) {
                    RuleCharacteristics ruleCharacteristics = rswc.getRuleCharacteristics(i);
                    logger.debug(i + ":\t" + ruleCharacteristics.toString());
                }
            }
        }

        Map.Entry<Integer, RuleSetWithCharacteristics> entry = parsedRules.entrySet().iterator().next();
        RuleSetWithCharacteristics ruleSetWithCharacteristics = entry.getValue();

        logger.info("LearningInformationTableHash:\t{}", ruleSetWithCharacteristics.getLearningInformationTableHash());
        return ruleSetWithCharacteristics;
    }

    public static RuleSetWithCharacteristics calculateRuleSetWithCharacteristics(Unions unions, RuleType typeOfRules, String filterSelector) {
        if((typeOfRules == RuleType.POSSIBLE) || (typeOfRules == RuleType.BOTH)) {
            if(!unions.getInformationTable().isSuitableForInductionOfPossibleRules()) {
                NotSuitableForInductionOfPossibleRulesException ex = new NotSuitableForInductionOfPossibleRulesException("Creating possible rules is not possible - learning data contain missing attribute values that can lead to non-transitivity of dominance/indiscernibility relation.");
                logger.error(ex.getMessage());
                throw ex;
            }

            logger.info("Current learning data is acceptable to create possible rules.");
        }

        RuleInducerComponents ruleInducerComponents = null;

        ApproximatedSetProvider unionAtLeastProvider = new UnionProvider(Union.UnionType.AT_LEAST, unions);
        ApproximatedSetProvider unionAtMostProvider = new UnionProvider(Union.UnionType.AT_MOST, unions);
        ApproximatedSetRuleDecisionsProvider unionRuleDecisionsProvider = new UnionWithSingleLimitingDecisionRuleDecisionsProvider();

        RuleSetWithComputableCharacteristics rules = null;
        RuleSetWithCharacteristics resultSet = null;


        if((typeOfRules == RuleType.POSSIBLE) || (typeOfRules == RuleType.BOTH)) {
            ruleInducerComponents = new PossibleRuleInducerComponents.Builder().
                    build();

            rules = (new VCDomLEM(ruleInducerComponents, unionAtLeastProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(filterSelector));
            rules.calculateAllCharacteristics();
            resultSet = rules;

            rules = (new VCDomLEM(ruleInducerComponents, unionAtMostProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(filterSelector));
            rules.calculateAllCharacteristics();
            resultSet = RuleSetWithCharacteristics.join(resultSet, rules);
        }


        if((typeOfRules == RuleType.CERTAIN) || (typeOfRules == RuleType.BOTH)) {
            final RuleInductionStoppingConditionChecker stoppingConditionChecker =
                    new EvaluationAndCoverageStoppingConditionChecker(
                            EpsilonConsistencyMeasure.getInstance(),
                            EpsilonConsistencyMeasure.getInstance(),
                            EpsilonConsistencyMeasure.getInstance(),
                            ((VCDominanceBasedRoughSetCalculator) unions.getRoughSetCalculator()).getLowerApproximationConsistencyThreshold()
                    );

            ruleInducerComponents = new CertainRuleInducerComponents.Builder().
                    ruleInductionStoppingConditionChecker(stoppingConditionChecker).
                    ruleConditionsPruner(new AttributeOrderRuleConditionsPruner(stoppingConditionChecker)).
                    ruleConditionsGeneralizer(new OptimizingRuleConditionsGeneralizer(stoppingConditionChecker)).
                    build();

            rules = (new VCDomLEM(ruleInducerComponents, unionAtLeastProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(filterSelector));
            rules.calculateAllCharacteristics();
            if (resultSet == null) {
                resultSet = rules;
            } else {
                resultSet = RuleSetWithCharacteristics.join(resultSet, rules);
            }

            rules = (new VCDomLEM(ruleInducerComponents, unionAtMostProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(filterSelector));
            rules.calculateAllCharacteristics();
            resultSet = RuleSetWithCharacteristics.join(resultSet, rules);
        }

        resultSet.setLearningInformationTableHash(unions.getInformationTable().getHash());
        return resultSet;
    }

    public static void calculateRulesInProject(Project project, RulesParameters rulesParameters) {
        UnionsService.calculateClassUnionsInProject(project, rulesParameters);
        final ProjectClassUnions projectClassUnions = project.getProjectClassUnions();

        final ProjectRules previousProjectRules = project.getProjectRules();
        if((previousProjectRules != null) && (!previousProjectRules.isExternalRules()) && (previousProjectRules.isCurrentLearningData()) && (previousProjectRules.getRulesParameters().equalsTo(rulesParameters))) {
            logger.info("Rules are already calculated with given configuration, skipping current calculation.");
            return;
        }

        CalculationsStopWatch calculationsStopWatch = new CalculationsStopWatch();

        RuleSetWithCharacteristics ruleSetWithCharacteristics = calculateRuleSetWithCharacteristics(projectClassUnions.getUnions(), rulesParameters.getTypeOfRules(), rulesParameters.getFilterSelector());

        ArrayList<String> descriptiveAttributesPriorityArrayList = new ArrayList<>();
        if (previousProjectRules != null) {
            descriptiveAttributesPriorityArrayList.add(previousProjectRules.getDescriptiveAttributes().getCurrentAttributeName());
        }
        descriptiveAttributesPriorityArrayList.add(project.getDescriptiveAttributes().getCurrentAttributeName());
        final String[] descriptiveAttributesPriority = descriptiveAttributesPriorityArrayList.toArray(new String[0]);

        ProjectRules projectRules = new ProjectRules(ruleSetWithCharacteristics, rulesParameters, descriptiveAttributesPriority, project.getInformationTable());
        calculationsStopWatch.stop();
        projectRules.setCalculationsTime(calculationsStopWatch.getReadableTime());

        project.setProjectRules(projectRules);
    }

    public static ProjectRules getRulesFromProject(Project project) {
        ProjectRules projectRules = project.getProjectRules();
        if(projectRules == null) {
            EmptyResponseException ex = new EmptyResponseException("There are no rules in project to show.");
            logger.error(ex.getMessage());
            throw ex;
        }

        return projectRules;
    }

    public static int[] getCoveringObjectsIndices(RuleSetWithCharacteristics ruleSetWithCharacteristics, Integer ruleIndex) {
        int[] indices;

        if((ruleIndex < 0) || (ruleIndex >= ruleSetWithCharacteristics.size())) {
            WrongParameterException ex = new WrongParameterException(String.format("Given rule's index \"%d\" is incorrect. You can choose rule from %d to %d", ruleIndex, 0, ruleSetWithCharacteristics.size() - 1));
            logger.error(ex.getMessage());
            throw ex;
        }

        final RuleCharacteristics ruleCharacteristics = ruleSetWithCharacteristics.getRuleCharacteristics(ruleIndex);
        final BasicRuleCoverageInformation basicRuleCoverageInformation = ruleCharacteristics.getRuleCoverageInformation();
        if(basicRuleCoverageInformation != null) {
            indices = basicRuleCoverageInformation.getIndicesOfCoveredObjects().toIntArray();
        } else {
            indices = new int[0];
        }

        return indices;
    }

    public static int[] getCoveringObjectsIndices(RuLeStudioRuleSet ruLeStudioRuleSet, Integer ruleIndex) {
        final RuLeStudioRule[] rules = ruLeStudioRuleSet.getRuLeStudioRules();
        if((ruleIndex < 0) || (ruleIndex >= rules.length)) {
            WrongParameterException ex = new WrongParameterException(String.format("Given rule's index \"%d\" is incorrect. You can choose rule from %d to %d", ruleIndex, 0, rules.length - 1));
            logger.error(ex.getMessage());
            throw ex;
        }

        return rules[ruleIndex].getIndicesOfCoveredObjects();
    }

    public MainRulesResponse getRules(UUID id, OrderByRuleCharacteristic orderBy, Boolean desc) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("orderBy=").append(orderBy).append(", ");
            sb.append("desc=").append(desc);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        ProjectRules projectRules = getRulesFromProject(project);
        if ((projectRules.isExternalRules()) && ((projectRules.isCoveragePresent() == null) || (!projectRules.isCoveragePresent()))) {
            RulesService.checkCoverageOfUploadedRules(projectRules, project.getInformationTable(), project.getDescriptiveAttributes());
        }
        ValidityRulesContainer validityRulesContainer = new ValidityRulesContainer(project);
        projectRules.setValidityRulesContainer(validityRulesContainer);

        try {
            projectRules = (ProjectRules) project.getProjectRules().clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            projectRules = project.getProjectRules();
        }

        final RuleSetWithCharacteristics ruleSetWithCharacteristics = projectRules.getRuleSet();
        if (!orderBy.equals(OrderByRuleCharacteristic.NONE)) {

            int i, rulesNumber = ruleSetWithCharacteristics.size();

            Rule[] ruleArray = new Rule[rulesNumber];

            RuleCharacteristics[] ruleCharacteristicsArray = new RuleCharacteristics[rulesNumber];
            for(i = 0; i < rulesNumber; i++) {
                ruleCharacteristicsArray[i] = ruleSetWithCharacteristics.getRuleCharacteristics(i);
            }

            Number[] characteristicValues = new Number[rulesNumber];
            switch (orderBy) {
                case SUPPORT:
                    collectIntegerCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getSupport());
                    break;
                case STRENGTH:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getStrength());
                    break;
                case CONFIDENCE:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getConfidence());
                    break;
                case COVERAGE_FACTOR:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getCoverageFactor());
                    break;
                case COVERAGE:
                    collectIntegerCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getCoverage());
                    break;
                case NEGATIVE_COVERAGE:
                    collectIntegerCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getNegativeCoverage());
                    break;
                case EPSILON:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getEpsilon());
                    break;
                case EPSILON_PRIME:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getEpsilonPrime());
                    break;
                case F_CONFIRMATION:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getFConfirmation());
                    break;
                case A_CONFIRMATION:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getAConfirmation());
                    break;
                case Z_CONFIRMATION:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getZConfirmation());
                    break;
                case L_CONFIRMATION:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getLConfirmation());
                    break;
                case C1_CONFIRMATION:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getC1Confirmation());
                    break;
                case S_CONFIRMATION:
                    collectDoubleCharacteristicLoop(rulesNumber, characteristicValues, (int index) -> ruleCharacteristicsArray[index].getSConfirmation());
                    break;
                default:
                    WrongParameterException ex = new WrongParameterException(String.format("Given ordering rule characteristic \"%s\" is unrecognized.", orderBy));
                    logger.error(ex.getMessage());
                    throw ex;
            }

            final ArrayIndexComparator comparator = new ArrayIndexComparator(characteristicValues);
            final Integer[] indices = comparator.createIndexArray();
            Arrays.sort(indices, comparator);

            int x, step;
            if(desc) {
                x = rulesNumber - 1;
                step = -1;
            } else {
                x = 0;
                step = 1;
            }
            for(i = 0; i < rulesNumber; i++) {
                ruleArray[i] = ruleSetWithCharacteristics.getRule(indices[x]);
                ruleCharacteristicsArray[i] = ruleSetWithCharacteristics.getRuleCharacteristics(indices[x]);
                logger.debug("{}:\tsupport={}\tindex={}", i, characteristicValues[indices[x]], indices[x]);
                x += step;
            }

            RuleSetWithCharacteristics sortedRuleSet = new RuleSetWithCharacteristics(ruleArray, ruleCharacteristicsArray);
            sortedRuleSet.setLearningInformationTableHash(ruleSetWithCharacteristics.getLearningInformationTableHash());
            projectRules.setRuleSet(sortedRuleSet);
        }

        final MainRulesResponse mainRulesResponse = MainRulesResponseBuilder.newInstance().build(projectRules);
        logger.debug(mainRulesResponse.toString());
        return mainRulesResponse;
    }

    public MainRulesResponse putRules(UUID id, RulesParameters rulesParameters) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append(rulesParameters);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        calculateRulesInProject(project, rulesParameters);

        final ProjectRules projectRules = project.getProjectRules();
        final MainRulesResponse mainRulesResponse = MainRulesResponseBuilder.newInstance().build(projectRules);
        logger.debug(mainRulesResponse.toString());
        return mainRulesResponse;
    }

    public MainRulesResponse postRules(UUID id, RulesParameters rulesParameters, String metadata, String data) throws IOException {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append(rulesParameters).append(", ");
            sb.append("metadataSize=").append(metadata.length()).append("B, ");
            if (logger.isDebugEnabled()) sb.append("metadata=").append(metadata).append(", ");
            sb.append("dataSize=").append(data.length()).append('B');
            if (logger.isDebugEnabled()) sb.append(", ").append("data=").append(data);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final InformationTable informationTable = ProjectService.createInformationTableFromString(metadata, data);
        project.setInformationTable(informationTable);

        calculateRulesInProject(project, rulesParameters);

        final ProjectRules projectRules = project.getProjectRules();
        final MainRulesResponse mainRulesResponse = MainRulesResponseBuilder.newInstance().build(projectRules);
        logger.debug(mainRulesResponse.toString());
        return mainRulesResponse;
    }

    public DescriptiveAttributesResponse getDescriptiveAttributes(UUID id) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final ProjectRules projectRules = getRulesFromProject(project);

        final DescriptiveAttributesResponse descriptiveAttributesResponse = new DescriptiveAttributesResponse(projectRules.getDescriptiveAttributes());
        logger.debug(descriptiveAttributesResponse.toString());
        return descriptiveAttributesResponse;
    }

    public DescriptiveAttributesResponse postDescriptiveAttributes(UUID id, String objectVisibleName) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("objectVisibleName=\"").append(objectVisibleName).append('\"');
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final ProjectRules projectRules = getRulesFromProject(project);

        DescriptiveAttributes descriptiveAttributes = projectRules.getDescriptiveAttributes();
        descriptiveAttributes.setCurrentAttribute(objectVisibleName);

        final DescriptiveAttributesResponse descriptiveAttributesResponse = new DescriptiveAttributesResponse(projectRules.getDescriptiveAttributes());
        logger.debug(descriptiveAttributesResponse.toString());
        return descriptiveAttributesResponse;
    }

    public AttributeFieldsResponse getObjectNames(UUID id) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final ProjectRules projectRules = getRulesFromProject(project);

        final Integer descriptiveAttributeIndex = projectRules.getDescriptiveAttributes().getCurrentAttributeInformationTableIndex();
        final AttributeFieldsResponse attributeFieldsResponse = AttributeFieldsResponseBuilder.newInstance().build(projectRules.getInformationTable(), descriptiveAttributeIndex);
        logger.debug(attributeFieldsResponse.toString());
        return attributeFieldsResponse;
    }

    public AttributeFieldsResponse getObjectNames(UUID id, Integer ruleIndex) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("ruleIndex=").append(ruleIndex);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final ProjectRules projectRules = getRulesFromProject(project);

        final int[] indices = getCoveringObjectsIndices(projectRules.getRuleSet(), ruleIndex);
        final String[] objectNames = projectRules.getDescriptiveAttributes().extractChosenObjectNames(projectRules.getInformationTable(), indices);

        final AttributeFieldsResponse attributeFieldsResponse = AttributeFieldsResponseBuilder.newInstance().setFields(objectNames).build();
        logger.debug(attributeFieldsResponse.toString());
        return attributeFieldsResponse;
    }

    public NamedResource download(UUID id, RulesFormat rulesFormat) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("rulesFormat=").append(rulesFormat);
            logger.info(sb.toString());
        }

        Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        if(project.getProjectRules() == null) {
            NoRulesException ex = new NoRulesException("There are no rules in this project.");
            logger.error(ex.getMessage());
            throw ex;
        }

        RuleSetWithCharacteristics ruleSetWithCharacteristics = project.getProjectRules().getRuleSet();
        String rulesString;

        switch (rulesFormat) {
            case XML:
                RuleMLBuilder ruleMLBuilder = new RuleMLBuilder();
                rulesString = ruleMLBuilder.toRuleMLString(ruleSetWithCharacteristics, 1);
                break;
            case TXT:
                rulesString = ruleSetWithCharacteristics.serialize();
                break;
            default:
                WrongParameterException ex = new WrongParameterException(String.format("Given format of rules \"%s\" is unrecognized.", rulesFormat));
                logger.error(ex.getMessage());
                throw ex;
        }

        InputStream is = new ByteArrayInputStream(rulesString.getBytes());
        InputStreamResource resource = new InputStreamResource(is);

        return new NamedResource(project.getName(), resource);
    }

    public static void checkCoverageOfUploadedRules(ProjectRules projectRules, InformationTable informationTable, DescriptiveAttributes descriptiveAttributes) {
        String errorMessage;
        String ruleSetHash = projectRules.getRuleSet().getLearningInformationTableHash();

        if(ruleSetHash == null) {
            errorMessage = String.format("Provided rule set doesn't have the learning information table hash. It can't be determined, if this rule set was generated based on the current data of the project. Rule coverage information can't be calculated without a valid training set. Current data hash: \"%s\".", informationTable.getHash());
            logger.info(errorMessage);

            projectRules.setCurrentLearningData(null);
            projectRules.setCoveragePresent(false);
            projectRules.setDescriptiveAttributes(new DescriptiveAttributes());
        } else if(ruleSetHash.equals(informationTable.getHash())) {
            logger.info("Current metadata and objects in the project are correct training set of uploaded rules. Calculating rule coverage information.");
            projectRules.getRuleSet().calculateBasicRuleCoverageInformation(informationTable);

            errorMessage = null;
            projectRules.setInformationTable(informationTable);
            projectRules.setCurrentLearningData(true);
            projectRules.setCoveragePresent(true);
            projectRules.setDescriptiveAttributes(new DescriptiveAttributes(descriptiveAttributes));
        } else {
            errorMessage = String.format("Uploaded rules are not induced from the data in the current project. Access to a valid training set is required to calculate rule coverage information. Please upload new rules based on the current data or create a new project with a valid training set. Current data hash: \"%s\", rules hash: \"%s\".", informationTable.getHash(), ruleSetHash);
            logger.info(errorMessage);

            projectRules.setCurrentLearningData(false);
            projectRules.setCoveragePresent(false);
            projectRules.setDescriptiveAttributes(new DescriptiveAttributes());
        }

        projectRules.setErrorMessage(errorMessage);
    }

    private static void uploadRulesToProject(Project project, MultipartFile rulesFile) throws IOException {
        InformationTable informationTable = project.getInformationTable();
        DataService.checkInformationTable(informationTable, "There is no data in project. Couldn't read rules file.");

        Attribute[] attributes = informationTable.getAttributes();
        if((attributes == null) || (attributes.length == 0)) {
            NoDataException ex = new NoDataException("There is no metadata in project. Couldn't read rules file.");
            logger.error(ex.getMessage());
            throw ex;
        }

        RuleSetWithCharacteristics ruleSetWithCharacteristics = parseRules(rulesFile, attributes);

        project.setProjectRules(new ProjectRules(ruleSetWithCharacteristics, rulesFile.getOriginalFilename(), attributes));
    }

    public MainRulesResponse putUploadRules(UUID id, MultipartFile rulesFile) throws IOException {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("rules={\"").append(rulesFile.getOriginalFilename()).append("\", ").append(rulesFile.getContentType()).append(", ").append(rulesFile.getSize()).append("B}");
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        uploadRulesToProject(project, rulesFile);

        final ProjectRules projectRules = project.getProjectRules();
        final MainRulesResponse mainRulesResponse = MainRulesResponseBuilder.newInstance().build(projectRules);
        logger.debug(mainRulesResponse.toString());
        return mainRulesResponse;
    }

    public MainRulesResponse postUploadRules(UUID id, MultipartFile rulesFile, String metadata, String data) throws IOException {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("rules={\"").append(rulesFile.getOriginalFilename()).append("\", ").append(rulesFile.getContentType()).append(", ").append(rulesFile.getSize()).append("B}, ");
            sb.append("metadataSize=").append(metadata.length()).append("B, ");
            if (logger.isDebugEnabled()) sb.append("metadata=").append(metadata).append(", ");
            sb.append("dataSize=").append(data.length()).append('B');
            if (logger.isDebugEnabled()) sb.append(", ").append("data=").append(data);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final InformationTable informationTable = ProjectService.createInformationTableFromString(metadata, data);
        project.setInformationTable(informationTable);

        uploadRulesToProject(project, rulesFile);

        final ProjectRules projectRules = project.getProjectRules();
        final MainRulesResponse mainRulesResponse = MainRulesResponseBuilder.newInstance().build(projectRules);
        logger.debug(mainRulesResponse.toString());
        return mainRulesResponse;
    }

    public ChosenRuleResponse getChosenRule(UUID id, Integer ruleIndex) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("ruleIndex=").append(ruleIndex);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final ProjectRules projectRules = getRulesFromProject(project);

        final ChosenRuleResponse chosenRuleResponse = ChosenRuleResponseBuilder.newInstance().build(projectRules.getRuleSet(), ruleIndex, projectRules.getDescriptiveAttributes(), projectRules.getInformationTable());
        logger.debug(chosenRuleResponse.toString());
        return chosenRuleResponse;
    }

    public ObjectAbstractResponse getObject(UUID id, Integer objectIndex, Boolean isAttributes) throws IOException {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id).append(", ");
            sb.append("objectIndex=").append(objectIndex).append(", ");
            sb.append("isAttributes=").append(isAttributes);
            logger.info(sb.toString());
        }

        final Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        final ProjectRules projectRules = getRulesFromProject(project);

        ObjectAbstractResponse objectAbstractResponse;
        if(isAttributes) {
            objectAbstractResponse = new ObjectWithAttributesResponse(projectRules.getInformationTable(), objectIndex);
        } else {
            objectAbstractResponse = new ObjectResponse(projectRules.getInformationTable(), objectIndex);
        }
        logger.debug(objectAbstractResponse.toString());
        return objectAbstractResponse;
    }

    public Boolean arePossibleRulesAllowed(UUID id)  {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=").append(id);
            logger.info(sb.toString());
        }

        Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        return project.getInformationTable().isSuitableForInductionOfPossibleRules();
    }
}
