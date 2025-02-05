package pl.put.poznan.rulestudio.rest;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pl.put.poznan.rulestudio.enums.ClassifierType;
import pl.put.poznan.rulestudio.enums.DefaultClassificationResultType;
import pl.put.poznan.rulestudio.enums.MisclassificationMatrixType;
import pl.put.poznan.rulestudio.enums.RuleType;
import pl.put.poznan.rulestudio.enums.UnionType;
import pl.put.poznan.rulestudio.model.parameters.CrossValidationParameters;
import pl.put.poznan.rulestudio.model.parameters.CrossValidationParametersImpl;
import pl.put.poznan.rulestudio.model.response.AttributeFieldsResponse;
import pl.put.poznan.rulestudio.model.response.ChosenClassifiedObjectAbstractResponse;
import pl.put.poznan.rulestudio.model.response.ChosenCrossValidationFoldResponse;
import pl.put.poznan.rulestudio.model.response.ChosenRuleResponse;
import pl.put.poznan.rulestudio.model.response.DescriptiveAttributesResponse;
import pl.put.poznan.rulestudio.model.response.MainCrossValidationResponse;
import pl.put.poznan.rulestudio.model.response.ObjectAbstractResponse;
import pl.put.poznan.rulestudio.model.response.OrdinalMisclassificationMatrixAbstractResponse;
import pl.put.poznan.rulestudio.model.response.RuleMainPropertiesResponse;
import pl.put.poznan.rulestudio.service.CrossValidationService;

@CrossOrigin
@RequestMapping("projects/{id}/crossValidation")
@RestController
public class CrossValidationController {

    private static final Logger logger = LoggerFactory.getLogger(CrossValidationController.class);

    private final CrossValidationService crossValidationService;

    @Autowired
    public CrossValidationController(CrossValidationService crossValidationService) {
        this.crossValidationService = crossValidationService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MainCrossValidationResponse> getCrossValidation(
            @PathVariable("id") UUID id) {
        logger.info("[START] Getting cross validation...");

        final MainCrossValidationResponse result = crossValidationService.getCrossValidation(id);

        logger.info("[ END ] Getting cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MainCrossValidationResponse> putCrossValidation(
            @PathVariable("id") UUID id,
            @RequestParam(name = "typeOfUnions") UnionType typeOfUnions,
            @RequestParam(name = "consistencyThreshold") Double consistencyThreshold,
            @RequestParam(name = "typeOfRules") RuleType typeOfRules,
            @RequestParam(name = "filterSelector") String filterSelector,
            @RequestParam(name = "classifierType") ClassifierType classifierType,
            @RequestParam(name = "defaultClassificationResultType") DefaultClassificationResultType defaultClassificationResultType,
            @RequestParam(name = "numberOfFolds") Integer numberOfFolds,
            @RequestParam(name = "seed", defaultValue = "0") Long seed) {
        logger.info("[START] Putting cross validation...");

        final CrossValidationParameters crossValidationParameters = new CrossValidationParametersImpl(
        		typeOfUnions, consistencyThreshold, typeOfRules, filterSelector, classifierType, defaultClassificationResultType, numberOfFolds, seed);
        final MainCrossValidationResponse result = crossValidationService.putCrossValidation(id, crossValidationParameters);

        logger.info("[ END ] Putting cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MainCrossValidationResponse> postCrossValidation(
            @PathVariable("id") UUID id,
            @RequestParam(name = "typeOfUnions") UnionType typeOfUnions,
            @RequestParam(name = "consistencyThreshold") Double consistencyThreshold,
            @RequestParam(name = "typeOfRules") RuleType typeOfRules,
            @RequestParam(name = "filterSelector") String filterSelector,
            @RequestParam(name = "classifierType") ClassifierType classifierType,
            @RequestParam(name = "defaultClassificationResultType") DefaultClassificationResultType defaultClassificationResultType,
            @RequestParam(name = "numberOfFolds") Integer numberOfFolds,
            @RequestParam(name = "seed", defaultValue = "0") Long seed,
            @RequestParam(name = "metadata") String metadata,
            @RequestParam(name = "data") String data) throws IOException {
        logger.info("[START] Posting cross validation...");

        final CrossValidationParameters crossValidationParameters = new CrossValidationParametersImpl(
        		typeOfUnions, consistencyThreshold, typeOfRules, filterSelector, classifierType, defaultClassificationResultType, numberOfFolds, seed);
        final MainCrossValidationResponse result = crossValidationService.postCrossValidation(id, crossValidationParameters, metadata, data);

        logger.info("[ END ] Posting cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/descriptiveAttributes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DescriptiveAttributesResponse> getDescriptiveAttributes (
            @PathVariable("id") UUID id) {
        logger.info("[START] Getting descriptive attributes in cross validation...");

        final DescriptiveAttributesResponse result = crossValidationService.getDescriptiveAttributes(id);

        logger.info("[ END ] Getting descriptive attributes in cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/descriptiveAttributes", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DescriptiveAttributesResponse> postDescriptiveAttributes(
            @PathVariable("id") UUID id,
            @RequestParam(name = "objectVisibleName", required = false) String objectVisibleName) {
        logger.info("[START] Posting descriptive attributes in cross validation...");

        final DescriptiveAttributesResponse result = crossValidationService.postDescriptiveAttributes(id, objectVisibleName);

        logger.info("[ END ] Posting descriptive attributes in cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/objectNames", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttributeFieldsResponse> getObjectNames(
            @PathVariable("id") UUID id,
            @RequestParam(name = "subject", required = false) Integer foldIndex,
            @RequestParam(name = "set", required = false) Integer rulesIndex) {
        logger.info("[START] Getting object names in cross validation...");

        AttributeFieldsResponse result;
        if((foldIndex != null) && (rulesIndex != null)) {
            result = crossValidationService.getObjectNames(id, foldIndex, rulesIndex);
        } else if(foldIndex != null) {
            result = crossValidationService.getObjectNames(id, foldIndex);
        } else {
            result = crossValidationService.getObjectNames(id);
        }

        logger.info("[ END ] Getting object names in cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/{foldIndex}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChosenCrossValidationFoldResponse> getChosenCrossValidationFold(
            @PathVariable("id") UUID id,
            @PathVariable("foldIndex") Integer foldIndex) {
        logger.info("[START] Getting chosen cross validation fold...");

        final ChosenCrossValidationFoldResponse result = crossValidationService.getChosenCrossValidationFold(id, foldIndex);

        logger.info("[ END ] Getting chosen cross validation fold is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/{foldIndex}/object", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChosenClassifiedObjectAbstractResponse> getChosenClassifiedObject(
            @PathVariable("id") UUID id,
            @PathVariable("foldIndex") Integer foldIndex,
            @RequestParam("objectIndex") Integer objectIndex,
            @RequestParam(name = "isAttributes", defaultValue = "false") Boolean isAttributes) throws IOException {
        logger.info("[START] Getting chosen classified object...");

        final ChosenClassifiedObjectAbstractResponse result = crossValidationService.getChosenClassifiedObject(id, foldIndex, objectIndex, isAttributes);

        logger.info("[ END ] Getting chosen classified object is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/{foldIndex}/rules/{ruleIndex}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleMainPropertiesResponse> getRule(
            @PathVariable("id") UUID id,
            @PathVariable("foldIndex") Integer foldIndex,
            @PathVariable("ruleIndex") Integer ruleIndex) {
        logger.info("[START] Getting rule from cross validation...");

        final RuleMainPropertiesResponse result = crossValidationService.getRule(id, foldIndex, ruleIndex);

        logger.info("[ END ] Getting rule from cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/{foldIndex}/rules/{ruleIndex}/coveringObjects", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChosenRuleResponse> getRuleCoveringObjects(
            @PathVariable("id") UUID id,
            @PathVariable("foldIndex") Integer foldIndex,
            @PathVariable("ruleIndex") Integer ruleIndex) {
        logger.info("[START] Getting rule covering objects from cross validation...");

        final ChosenRuleResponse result = crossValidationService.getRuleCoveringObjects(id, foldIndex, ruleIndex);

        logger.info("[ END ] Getting rule covering objects from cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/object", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectAbstractResponse> getObject(
            @PathVariable("id") UUID id,
            @RequestParam("objectIndex") Integer objectIndex,
            @RequestParam(name = "isAttributes", defaultValue = "false") Boolean isAttributes) throws IOException {
        logger.info("[START] Getting object from cross validation...");

        final ObjectAbstractResponse result = crossValidationService.getObject(id, objectIndex, isAttributes);

        logger.info("[ END ] Getting object from cross validation is done.");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/misclassificationMatrix", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrdinalMisclassificationMatrixAbstractResponse> getMisclassificationMatrix(
            @PathVariable("id") UUID id,
            @RequestParam(name = "typeOfMatrix") MisclassificationMatrixType typeOfMatrix,
            @RequestParam(name = "numberOfFold", required = false) Integer numberOfFold) {
        logger.info("[START] Getting misclassification matrix from cross validation...");

        final OrdinalMisclassificationMatrixAbstractResponse result = crossValidationService.getMisclassificationMatrix(id, typeOfMatrix, numberOfFold);

        logger.info("[ END ] Getting misclassification matrix from cross validation is done.");
        return ResponseEntity.ok(result);
    }
}
