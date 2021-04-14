import React, { Component } from 'react'
import FolderView from './FolderView'
import FileView from './FileView'
import '../../styles/FileSpace.css'
import DisplayPath from '../Path/DisplayPath'

class FilesSpace extends Component {

    constructor() {
        super()

        this.state = {
            files : [],
            url : 'https://15.206.165.104:8080/api/files'
        }

        this.fetchData = this.fetchData.bind(this)
        this.loadData = this.loadData.bind(this)
    }

    fetchFolderData(fileId, fileName) {
        console.log("fetching folder data..")
        this.fetchData('https://15.206.165.104:8080/api/files/' + fileId)
        this.props.updatePath(fileId, fileName)
    }
    

    async fetchData(url) {
        await fetch(url, {
            method: 'GET',
            credentials: 'include'
        })
        .then(async response => await response.json())
        .then(data => this.loadData(data.children))
        .catch(error => {
            console.log(error);
        })
    }

    componentDidMount() {
        this.props.fetchFolderData('', 'root')
    }


    loadData(data) {

        // change favourite icon
        var array = data.map((file) => {
            file.directory ?
            <FolderView file = {file}  key = {file.fileId} fetchFolderData = {() => this.fetchFolderData(file.fileId, file.fileName)} /> : 
            <FileView file = {file} key = {file.fileId}/> }
            
        )
        

        this.setState({files : array})
    }

    render() {
        return (
            <div>
                <div className = 'files-space'>{ this.props.state.filesSpace }</div>
            </div>

        )
    }
   
}

export default FilesSpace