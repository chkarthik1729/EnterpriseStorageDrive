import { node } from 'prop-types'
import React, { Component } from 'react'
import '../../styles/VerticalBar.css'

class VerticalBar extends Component {

    constructor() {
        super()

        this.createFolder = this.createFolder.bind(this)
    }

    async createFolder(state) {

    
        var name = prompt("Enter folder Name", "untitled")

        if (name == null) {
            alert("Folder name should not be empty")
        }


        else {
            const url = 'https://15.206.165.104:8080/api/files/' + state.parentId

            var bodyData = {
                folderName : name
            }

            await fetch(url, {
                method : 'POST',
                credentials : 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body : JSON.stringify(bodyData)
            })
            .then(response => {
                if (response.status !== 200) {
                    alert("something wrong with request")
                }
            })
            .catch(
                error => alert("something went wrong in creating folder")
            )

            this.props.refreshData()
        }
    }

    async uploadFile(state, event) {

        const url = 'https://15.206.165.104:8080/api/files/upload/' + state.parentId
        
        
        var file = event.target.files[0]
        
        var fileName = file.name

        const formData  = new FormData();

        formData.append("file", file, fileName)

       await fetch(url, {
            method : 'POST',
            credentials : 'include',
            body : formData
        })
        .then(response => {
            if (response.status !== 200) {
                alert("failed to upload")
            }
        })
        .catch(
            error => alert(error)
        )

        this.props.refreshData()
    }

    render() {
        return (
            <div>
                <div className = 'vertical-nav-bar'>
                    <h2 style = {{fontSize : '25px'}}> Vicara Drive </h2>
                    <ul className = 'vertical-menu-list'>
                        <li onClick = {() => this.props.fetchFolderData('')}> Home </li>
                        <li onClick = { () => this.createFolder(this.props.state)}> Create Folder </li>
                        <li className = 'file-input'> 
                            <input type="file" id="file" className="file" onChange = {(e) => this.uploadFile(this.props.state, e)}/>
                            <label> Upload File </label>  </li>
                        <li onClick = {() => {this.props.getTabWith('favourites')}}> Favourites </li>
                        <li onClick = {() => {this.props.getTabWith('shared-with-me')}}> Shared With Me </li>
                    </ul>
                </div>
            </div>
        )
    }
}

export default VerticalBar