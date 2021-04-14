import React, { Component } from 'react'
import '../../styles/PathStyle.css'

class DisplayPath extends Component {
    constructor() {
        super()

    }

    render() {
        return (
            <div className = 'path-display-container'>
                <div className = 'path-block'>
                    <button type = 'button' className = 'path-btn' onClick = {this.props.getBackToparent}> &larr; Back to Parent Directory </button>
                </div>
            </div>
        )
    }
}

export default DisplayPath