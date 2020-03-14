import React, { Component } from 'react'

class PutUnionsWithSingleLimitingDecision extends Component {
    constructor(props) {
        super(props);

        this.state = {
            id_projektu: '66f23be2-0595-40b9-aca1-fcc5f9b5ffc2',
            consistencyThreshold: 0
        }
    }

    handleIdChange = (event) => {
        this.setState({
            id_projektu: event.target.value
        })
    }

    handleConsistencyThresholdChange = (event) => {
        this.setState({
            consistencyThreshold: event.target.value
        })
    }

    putUnionsWithSingleLimitingDecision = (event) => {
        event.preventDefault();

        var link = `http://localhost:8080/projects/${this.state.id_projektu}/unions?consistencyThreshold=${this.state.consistencyThreshold}`;

        console.log(link)

        fetch(link, {
            method: 'PUT'
        }).then(response => {
            console.log(response)
            if(response.status === 200) {
                response.json().then(result => {
                    console.log("Otrzymane unie:")
                    console.log(result)
                }).catch(err => {
                    console.log(err)
                })
            } else if(response.status === 404) {
                response.json().then(result => {
                    console.log("Błąd 404.")
                    console.log(result.message)
                }).catch(err => {
                    console.log(err)
                })
            } else {
                response.json().then(result => {
                    console.log("Wynik dzialania response.json():")
                    console.log(result)
                }).catch(err => {
                    console.log(err)
                })
            }
        }).catch(err => {
            console.log(err)
        })
    }

    render() {
        return (
            <div>
                id->
                <input type='text' value={this.state.id_projektu} onChange={this.handleIdChange} />
                consistencyThreshold->
                <input type='text' value={this.state.consistencyThreshold} onChange={this.handleConsistencyThresholdChange} />
                <button onClick={this.putUnionsWithSingleLimitingDecision}>putUnionsWithSingleLimitingDecision</button>
            </div>
        )
    }
}

export default PutUnionsWithSingleLimitingDecision