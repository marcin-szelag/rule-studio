import React from "react";
import PropTypes from "prop-types";
import CircleHelper from "../../../Utils/Feedback/CircleHelper";
import CustomTextField from "../../../Utils/Inputs/CustomTextField";
import CustomTooltip from "../../../Utils/DataDisplay/CustomTooltip";
import styles from "./styles/Calculations.module.css";

const tooltip = {
    main: 	" is used to filter generated rules using their characteristics. " +
    		"It is possible to supply many filtering conditions concerning different rule characteristics. " +
    		"Particular filtering conditions have to be separated using &. " +
    		"Each filtering condition has to be in the form: characteristic R threshold. " +
    		"Allowed values of characteristics are: support, strength, confidence, coverage-factor, coverage, negative-coverage, length, " +
    		"epsilon, epsilon', F, A, Z, L, c1, S (the last six concerning confirmation measures). " +
    		"Allowed relations (to be put instead of R) are: >, >=, =, <=, <. " +
    		"Exemplary composite filter: support >= 5 & length <= 2. " +
    		"White spaces are ignored, so the above filter is equivalent to support>=5&length<=2. " +
    		"Exemplary simple filter: confidence>0.5. " +
    		"If all rules should be considered (i.e., no filtering should be performed), then rule filter input field should be left blank."
};

/**
 * <h3>Overview</h3>
 * Presents filter(s) and allows user to type new value.
 *
 * @name Filter
 * @constructor
 * @category Project
 * @subcategory Calculations
 * @param {Object} props
 * @param {Object} props.CircleHelperProps - Props applied to the {@link CircleHelper} element.
 * @param {Object} props.TextFieldProps - Props applied to the {@link CustomTextField} element.
 * @returns {React.ReactElement}
 */
function FilterSelector(props) {
    const { CircleHelperProps, TextFieldProps: { value, ...other } } = props;

    return (
        <div aria-label={"outer wrapper"} className={styles.OuterWrapper} style={props.style}>
            <CircleHelper
                title={
                    <p aria-label={"main"} style={{margin: 0, textAlign: "justify"}}>
                    	<b>Rule filter</b>
                        {tooltip.main}
                    </p>
                }
                WrapperProps={{
                    style: { marginRight: 16 }
                }}
                {...CircleHelperProps}
            />
            <div aria-label={"inner wrapper"} className={styles.InnerWrapper}>
                <CustomTextField
                    outsideLabel={"Choose rule filter"}
                    value={value}
                    {...other}
                />
            </div>
            <CustomTooltip
            	title={"Filter"}
	            WrapperProps={{
	                style: { marginLeft: 8 }
	            }}
            >
            </CustomTooltip>
        </div>
    )
}

FilterSelector.propTypes = {
    CircleHelperProps: PropTypes.object,
    TextFieldProps: PropTypes.shape({
        onChange: PropTypes.func,
        value: PropTypes.any,
    })
};

export default FilterSelector;
