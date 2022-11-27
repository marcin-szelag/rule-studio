import React from "react";
import PropTypes from "prop-types";
import { makeStyles } from "@material-ui/core/styles";
import CircleHelper from "../../../Utils/Feedback/CircleHelper";
import CustomTextField from "../../../Utils/Inputs/CustomTextField";
import CustomTooltip from "../../../Utils/DataDisplay/CustomTooltip";
import styles from "./styles/Calculations.module.css";

const tooltip = {
    main: 	" is used to filter generated rules using their characteristics. " +
    		"It is possible to supply many filtering conditions concerning different rule characteristics. " +
    		"Particular filtering conditions have to be separated using &. " +
    		"Each filtering condition has to be in the form: characteristic R threshold. ",
    characteristics: " are: support, strength, confidence, coverage-factor, coverage, negative-coverage, length, " +
    		"epsilon, epsilon', F, A, Z, L, c1, S (the last six concerning confirmation measures). ",
    relations: " (to be put instead of R) are: >, >=, =, <=, <. ",
    example1: ": support >= 5 & length <= 2. " +
    		"White spaces are ignored, so the above filter is equivalent to support>=5&length<=2. ",
    example2: ": confidence>0.5. ",
    nofilter: " (i.e., not to perform rule filtering), rule filter input field should be left blank."
};

const useStyles = makeStyles({
    maxWidth: {
        maxWidth: 360
    },
    paragraph: {
        margin: 0,
        textAlign: "justify"
    }
}, {name: "MultiRow"});

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
    const classes = useStyles();

    return (
        <div aria-label={"outer wrapper"} className={styles.OuterWrapper} style={props.style}>
            <CircleHelper
            	multiRow={true}
                title={
            		<React.Fragment>
	                    <p aria-label={"main"} className={classes.paragraph}>
	                    	<b>Rule filter</b>
	                        {tooltip.main}
	                    </p>
	                    <p aria-label={"filter-characteristics"} className={classes.paragraph}>
	                		<b>Allowed values of characteristics</b>
	                		{tooltip.characteristics}
	                	</p>
	                	<p aria-label={"filter-relations"} className={classes.paragraph}>
	            			<b>Allowed relations</b>
	            			{tooltip.relations}
	            		</p>
	            		<p aria-label={"filter-example1"} className={classes.paragraph}>
	        				<b>Exemplary composite filter</b>
	        				{tooltip.example1}
	        			</p>
	        			<p aria-label={"filter-example2"} className={classes.paragraph}>
	    					<b>Exemplary simple filter</b>
	    					{tooltip.example2}
	    				</p>
	    				<p aria-label={"filter-blank"} className={classes.paragraph}>
							<b>To consider all induced rules</b>
							{tooltip.nofilter}
						</p>
					</React.Fragment>
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
