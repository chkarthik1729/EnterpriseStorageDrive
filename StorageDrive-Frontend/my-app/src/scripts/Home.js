import React, { Component } from 'react'
import FilesSpace from './filesDisplay/FilesSpace'
import FolderView from './filesDisplay/FolderView'
import FileView from './filesDisplay/FileView'
import DisplayPath from './Path/DisplayPath'
import ProfileCard from './ProfileCard'
import VerticalBar from './verticalNavBar/VerticalBar'
import {withRouter} from 'react-router';
import {createBrowserHistory} from 'history';

const history = createBrowserHistory();

class Home extends Component {

    constructor(props) {
        super(props)
        this.state = {
            parentId : '',
            parentName : 'root',

            currId : '',
            currName : 'root',

            user : '',

            filesSpace : []
        }
    }

    componentDidMount() {
       fetch('https://15.206.165.104:8080/api/files', {
            method: 'GET',
            credentials: 'include',
            redirect : 'manual'
        })
        .then(response => {
            if (response.status !== 200) {
                history.push('/')
                window.location.reload(false)
            }
        }).catch(error => {
            history.push('/')
            window.location.reload(false)
            console.log(error);
        })

        this.setState(
            {
                parentId : '',
                parentName : '',
            }
        )

    }

    fetchFolderData(fileId) {
        console.log("fetching folder data..")
        this.fetchData('https://15.206.165.104:8080/api/files/' + fileId)
        this.updatePath(fileId)
    }
    
    loadData(data) {
        console.log('data received ' + data)
        console.log(data)
        var array = data.map((file) => file.directory ? 
        <FolderView file = {file}  key = {file.fileId}  refreshData = {this.refreshData.bind(this)} fetchFolderData = {() => this.fetchFolderData(file.fileId, file.fileName)} /> :
        <FileView file = {file} key = {file.fileId}  refreshData = {this.refreshData.bind(this)}/>)
        this.setState({filesSpace : array})
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

    refreshData() {
        this.fetchFolderData(this.state.parentId)
    }

    async getTabWith(path) {
        const url = 'https://15.206.165.104:8080/api/files/' + path

        await fetch(url, {
            method: 'GET',
            credentials: 'include'
        })
        .then(async response => await response.json())
        .then(data => this.loadData(data))
        .catch(error => {
            console.log(error);
        })


    }

    async getBackToparent() {
        var parentId = ''

        console.log("back to parent fired..")

        console.log("curr " + this.state.parentId)

        if (typeof this.state.parentId !== 'undefined' && this.state.parentId !== '' && this.state.parentId !== null) {

            const url = 'https://15.206.165.104:8080/api/files/' + this.state.parentId + '/details' 

            console.log(url)

            await fetch(url, {
                method: 'GET',
                credentials: 'include'
            })
            .then(async response => await response.json())
            .then(data => { 
                parentId = data.parentId
            })
            .catch(error => {
                console.log(error);
            })

            if (typeof parentId !== 'undefined' && parentId != '' && parentId !== null) {
                this.fetchFolderData(parentId)
            }
            else {
                this.updatePath(parentId)
            }
        }

    }
    
    render() {
        return (
            <div>
                <VerticalBar state = {this.state} refreshData = {this.refreshData.bind(this)} getTabWith = {this.getTabWith.bind(this)} fetchFolderData = {this.fetchFolderData.bind(this)} />
                <ProfileCard logOut = {this.props.logOut} user = {() => this.props.user}/>
                <DisplayPath getBackToparent = {this.getBackToparent.bind(this)}/>
                <FilesSpace state = {this.state} fetchFolderData = {this.fetchFolderData.bind(this)}/>
            </div>
        )
    }


    updatePath(path) {
        this.setState({ parentId : path })
    }

}

export default withRouter(Home)