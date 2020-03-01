import React, {Component} from 'react';
import PropTypes from 'prop-types';
import RuleWorkBody from "../../RuleWorkComponents/Surfaces/RuleWorkBody";
import RuleWorkTextField from "../../RuleWorkComponents/Inputs/RuleWorkTextField";
import StyledContent from "./containers/StyledContent";
import StyledDivider from "./data-display/StyledDivider";
import StyledFooter from "./containers/StyledFooter";
import StyledHeader from "./containers/StyledHeader";
import StyledPaper from "./surfaces/StyledPaper";
import StyledSwitch from "./inputs/StyledSwitch";
import Button from "@material-ui/core/Button";
import Collapse from "@material-ui/core/Collapse";
import FileSelectZone from "./inputs/FileSelectZone";
import "./Import.css";
import StyledCollapsibleDivider from "./data-display/StyledCollapsibleDivider";

class Import extends Component{
    constructor(props) {
        super(props);

        this.state = {
            checked: false,
            expand: false,
            name: "new project",
            files: [],
        };
    }

    onProjectNameChange = (event) => {
        this.setState({
            name: event.target.value,
        });
    };

    onCheckboxChange = () => {
        this.setState( prevState => ({
            checked: !prevState.checked,
        }), () => {
            if (!this.state.checked) {
                this.setState({
                    expand: false,
                });
            }
        });
    };

    onExpandClick = () => {
        this.setState(prevState => ({
            expand: !prevState.expand,
        }));
    };

    onInputChange = (message) => {
        let newFiles = this.state.files;
        for (let i = 0; i <  newFiles.length; i++) {
            if (newFiles[i].type === message.type) {
                newFiles.splice(i, 1);
                break;
            }
        }
        this.setState({
            files: [...newFiles, message],
        });
    };

    onInputDelete = (message) => {
        let newFiles = this.state.files;
        for (let i = 0; i <  newFiles.length; i++) {
            if (newFiles[i].type === message.type) {
                newFiles.splice(i, 1);
                this.setState({
                    files: newFiles,
                });
                break;
            }
        }
    };

    onAcceptButtonClick = () => {
        const projectName = this.state.name;
        let projectFiles = [];
        if (this.state.checked) {
            projectFiles = this.state.files;
        }

        this.props.onFilesAccepted(projectName, projectFiles);
    };

    render() {
        const {checked, expand, name} = this.state;

        return (
            <RuleWorkBody id={"rule-work-import"}>
                <StyledPaper elevation={6} square={true}>
                    <StyledHeader>
                        <RuleWorkTextField
                            label={"Project name:"}
                            defaultValue={name}
                            onChange={this.onProjectNameChange}
                        />
                    </StyledHeader>
                    <StyledDivider flexItem={true} />
                    <StyledSwitch
                        label={"Create project with metadata"}
                        checked={checked}
                        onChange={this.onCheckboxChange}
                    />
                    <Collapse in={checked} unmountOnExit={true}>
                        <StyledContent>
                            <FileSelectZone
                                variant={"metadata"}
                                accept={".json"}
                                onInputChange={this.onInputChange}
                                onInputDelete={this.onInputDelete}
                            />
                            <StyledCollapsibleDivider
                                onClick={this.onExpandClick}
                                expanded={expand}
                            />
                            <Collapse in={expand} unmountOnExit={true}>
                                <StyledContent>
                                    <FileSelectZone
                                        variant={"data"}
                                        accept={".json,.csv"}
                                        onInputChange={this.onInputChange}
                                        onInputDelete={this.onInputDelete}
                                    />
                                    <FileSelectZone
                                        variant={"rules"}
                                        accept={".xml"}
                                        onInputChange={this.onInputChange}
                                        onInputDelete={this.onInputDelete}
                                    />
                                </StyledContent>
                            </Collapse>
                        </StyledContent>
                    </Collapse>
                    <StyledDivider flexItem={true} hidden={!expand} />
                    <StyledFooter >
                        <Button
                            variant={"outlined"}
                            color={"inherit"}
                            onClick={this.onAcceptButtonClick}
                        >
                            Accept
                        </Button>
                        <Button
                            variant={"outlined"}
                            color={"secondary"}
                        >
                            Clear
                        </Button>
                    </StyledFooter>
                </StyledPaper>
            </RuleWorkBody>
        );
    }
}

Import.propTypes = {
    onFilesAccepted: PropTypes.func.isRequired,
};

export default Import;