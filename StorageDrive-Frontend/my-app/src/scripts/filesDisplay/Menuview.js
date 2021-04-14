import React, {Component} from 'react'
import Details from './Details'
import '../../styles/MenuCard.css'
import '../../styles/Details.css'

class MenuView extends Component {

    constructor() {
        super()

        this.state = {
            details : {}
        }
        this.deleteObject = this.deleteObject.bind(this)
        this.renameObject = this.renameObject.bind(this)
        this.downloadFile = this.downloadFile.bind(this)
        this.fetchDetails = this.fetchDetails.bind(this)
        this.shareFile = this.shareFile.bind(this)

    }

    async renameObject(file) {
        this.props.menuVisibility()

        var newName = prompt("Enter new file Name", file.fileName)

        if (newName === '') {
            alert("file Name should not be empty")
        }
        else {
            const url = 'https://15.206.165.104:8080/api/files/' + file.fileId + '/details'

            var bodyData = {
                fileName : newName
            }
           
            await fetch(url, {
                method : 'PATCH',
                credentials : 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body : JSON.stringify(bodyData)
            })
            .then(response => {
                if (response.status == 400) {
                    alert('Bad Request! Already Exists')
                }
                else if (response.status === 500) {
                    alert("Internal server Exception")
                }
                else if (response.status === 403) {
                    alert("You don't have permissions")
                }
            })
            .catch(
                error => alert("something went wrong in Renaming")
            )

            this.props.refreshData()
        }
        
    }

    async deleteObject(file) {
        const url = 'https://15.206.165.104:8080/api/files/' + file.fileId

        await fetch(url, {
            method : 'DELETE',
            credentials : 'include'
        }).then(response => {
            if (response.status === 403) {
                alert("You don't have permissions to delete")
            }
            if (response.status !== 200) {
                alert("failed to delete " + file.fileName)
            }

        })
        .catch(error => alert("Error occured while deleting"))

        this.props.refreshData()

    }

    async fetchDetails(file) {
        
        const url = 'https://15.206.165.104:8080/api/files/' + file.fileId + '/details' 

        await fetch(url, {
            method: 'GET',
            credentials: 'include'
        })
        .then(async response => await response.json())
        .then(data => this.setState({details : data}))
        .catch(error => {
            console.log(error);
        })

        console.log(this.state.details)
    }

    async downloadFile(file) {
        const url = 'https://15.206.165.104:8080/api/files/' + file.fileId
        window.open(url)
    }

    async shareFile(file, access) {
        this.props.menuVisibility()

        const url = 'https://15.206.165.104:8080/api/files/' + file.fileId + '/permissions'

        var email = prompt("Enter User Name to share", 'user')

        if (email === null) {
            alert("email should not be empty")
        }

        var bodyData = {}
        bodyData[email] = access

        fetch(url, {
            method : 'PATCH',
            credentials : 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body : JSON.stringify(bodyData)
        })
        .then(response => {
             if (response.status === 200) {
                 alert("Successfully Shared")
             }
        })
    }

    render() {
        return (
            <div className = 'menu-container'>
                <button type = 'button' className = 'open-btn' onClick = {() => {
                    this.props.file.directory ? this.downloadFile(this.props.file) : this.downloadFile(this.props.file)
                }

                }> {this.props.file.directory ? 'Open' : 'Download' }</button>
                <br/>
                <button type = 'button' className = 'details-btn' onClick = {() => {this.deleteObject(this.props.file)}}>Delete</button>
                <br/>
                <button type = 'button' className = 'rename-btn' onClick = {() => {this.renameObject(this.props.file)}}>Rename</button>
                <br/>
                <button type = 'button' className = 'share-read-btn' onClick = {() => {this.shareFile(this.props.file, 'Read')}}>Share : Read</button>
                <br/>
                <button type = 'button' className = 'share-write-btn' onClick = {() => {this.shareFile(this.props.file, 'Write')}}>Share : Write </button>
                <br/>
                <button type = 'button'  onClick = {() => this.fetchDetails(this.props.file)}> 
                     <a href="#close" className="link-1" id="open">Details</a>
                     <Details state = {this.state.details}/>    
                </button>


            </div>
        )
    }
   
}

export default MenuView