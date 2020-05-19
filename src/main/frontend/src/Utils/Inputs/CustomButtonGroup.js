import React from "react";
import PropTypes from "prop-types";
import { makeStyles } from "@material-ui/core/styles";
import CustomPopper from "../Surfaces/CustomPopper";
import CustomTooltip from "../DataDisplay/CustomTooltip";
import StyledButton from "./StyledButton";
import ButtonGroup from "@material-ui/core/ButtonGroup";
import ClickAwayListener from "@material-ui/core/ClickAwayListener";
import Grow from "@material-ui/core/Grow";
import MenuList from "@material-ui/core/MenuList";
import MenuItem from "@material-ui/core/MenuItem";
import Popper from "@material-ui/core/Popper";
import ArrowDropDownIcon from "@material-ui/icons/ArrowDropDown";

// To get unblurred tooltip text in Google Chrome
const disableGPUOptions = {
    modifiers: {
        computeStyle: {
            enabled: true,
            gpuAcceleration: false
        }
    }
};

const useStyles = makeStyles(theme => ({
    left: {
        borderRight: "1px solid",
        borderRightColor: theme.palette.text.default,
        '& .MuiButton-root': {
            borderRadius: "4px 0 0 4px",
            height: "100%",
            minWidth: 36,
            padding: "6px 0"
        }
    },
    right: {
        '& .MuiButton-root': {
            borderRadius: "0 4px 4px 0",
            height: "100%"
        }
    }
}), {name: "ButtonWrapper"});

function ButtonWrapper(props) {
    const {children, placement} = props;
    const classes = useStyles();

    return (
        <div className={classes[placement]}>
            {children}
        </div>
    )
}

ButtonWrapper.propTypes = {
    children: PropTypes.element.isRequired,
    placement: PropTypes.oneOf(["left", "right"]).isRequired,
};

class CustomButtonGroup extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            open: false
        };

        this.anchorRef = React.createRef();
    }

    onToggleButtonClick = () => {
        this.setState(prevState => ({
            open: !prevState.open
        }));
    };

    onPopperClose = (event) => {
        if (this.anchorRef.current && this.anchorRef.current.contains(event.target)) {
            return;
        }

        this.setState({
            open: false,
        });
    };

    onMenuItemClick = (event, index) => {
        this.setState({
            open: false
        }, () => {
            this.props.onActionSelected(index);
        });
    };

    render() {
        const { open } = this.state;
        const { children, disableGPU, options, tooltips, selected, WrapperProps } = this.props;
        const childrenArray = React.Children.toArray(children);

        return (
            <div aria-label={"split button wrapper"} {...WrapperProps}>
                <ButtonGroup aria-label={"split button"} ref={this.anchorRef}>
                    <ButtonWrapper placement={"left"}>
                        <CustomTooltip
                            disableMaxWidth={true}
                            enterDelay={1000}
                            enterNextDelay={1000}
                            title={"Open menu"}
                            WrapperProps={{
                                style: { height: "100%" }
                            }}
                        >
                            <StyledButton
                                aria-controls={open ? 'split-button-menu' : undefined}
                                aria-expanded={open ? true : undefined}
                                aria-label={"select classification method"}
                                aria-haspopup={"menu"}
                                disableElevation={true}
                                onClick={this.onToggleButtonClick}
                                themeVariant={"primary"}
                                variant={"contained"}
                            >
                                <ArrowDropDownIcon />
                            </StyledButton>
                        </CustomTooltip>
                    </ButtonWrapper>
                    <ButtonWrapper placement={"right"}>
                        <CustomTooltip
                            disableMaxWidth={true}
                            title={Array.isArray(tooltips) ? tooltips[selected] : tooltips}
                            WrapperProps={{
                                style: { height: "100%" }
                            }}
                        >
                            {childrenArray[selected]}
                        </CustomTooltip>
                    </ButtonWrapper>
                </ButtonGroup>
                <Popper
                    anchorEl={this.anchorRef.current}
                    aria-label={'split-menu-button'}
                    disablePortal={true}
                    open={open}
                    popperOptions={disableGPU ? disableGPUOptions : undefined}
                    role={undefined}
                    transition={true}
                >
                    {({TransitionProps, placement}) => (
                        <Grow
                            {...TransitionProps}
                            style={{
                                transformOrigin: placement === 'bottom' ? 'center-top' : 'center-bottom',
                            }}
                        > 
                            <CustomPopper style={{marginTop: "1%"}}>
                                <ClickAwayListener onClickAway={this.onPopperClose}>
                                    <MenuList>
                                        {options.map((option, index) => (
                                            <MenuItem
                                                key={index}
                                                onClick={event => this.onMenuItemClick(event, index)}
                                                selected={selected === index}
                                            >
                                                {option}
                                            </MenuItem>
                                        ))}
                                    </MenuList>
                                </ClickAwayListener>
                            </CustomPopper>
                        </Grow>
                    )}
                </Popper>
            </div>
        )
    }
}

CustomButtonGroup.propTypes = {
    children: PropTypes.arrayOf(PropTypes.node).isRequired,
    disableGPU: PropTypes.bool,
    options: PropTypes.arrayOf(PropTypes.string).isRequired,
    selected: PropTypes.number.isRequired,
    onActionSelected: PropTypes.func,
    tooltips: PropTypes.oneOfType([
        PropTypes.arrayOf(PropTypes.node),
        PropTypes.node
    ]).isRequired,
    WrapperProps: PropTypes.object
};

CustomButtonGroup.defaultProps = {
    disableGPU: true
};

export default CustomButtonGroup;
