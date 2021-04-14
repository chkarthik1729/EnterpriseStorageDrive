import React, {Component} from 'react'
import '../../styles/Details.css'

class Details extends Component {

    constructor() {
        super()

        this.formatToDateTimeString = this.formatToDateTimeString.bind(this)
    }

    formatToDateTimeString(timestamp) {
        var date = new Date(timestamp);
        var hours = date.getHours();
        var minutes = "0" + date.getMinutes();
        var seconds = "0" + date.getSeconds();
        return hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
    }

    
    render() {
        return (
            <div>
                <div className ="modal-container" id="close">
                    <div className ="modal">
                    
                        <p>File Name : {this.props.state.fileName} </p>
                        <p>Path : {this.props.state.filePath}</p>
                        <p>Size : {this.props.state.length / (1024)} KB </p>
                        <p>Created At : {this.formatToDateTimeString(this.props.state.createdAt)}</p>
                        <p>Last Modified At: {this.formatToDateTimeString(this.props.state.lastModified)}</p>

                    <a href="#open" className ="link-2"></a>

                    </div>
                </div>


            </div>
        )
    }
}

export default Details