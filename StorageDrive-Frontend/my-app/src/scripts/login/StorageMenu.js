import React, { Component } from 'react'
import '../../styles/Register.css'

class StorageMenu extends Component {

    render() {
        return (
            <div className ="container">
                <select className="storage-list">
                    <option value="" data-display-text="Storage-providers">Amazon S3</option>
                    <option value="Azure">Azure</option>
                    <option value="Google-cloud-service">Google Cloud Service</option>
                </select>
            </div>
        )
    }

}

export default StorageMenu