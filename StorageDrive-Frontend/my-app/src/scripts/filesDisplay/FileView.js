import React, { Component } from 'react'
import floderLogo from './icons/1.png'
import fileLogo from './icons/9.png'
import floderLogo2 from './icons/4.png'
import fileMenuImage from './icons/dot.png'
import favIcon from './icons/fav-icon.png'
import favIconBf from './icons/fav-icon-bf.png'
import MenuView from '../filesDisplay/Menuview'

import '../../styles/FileSpace.css'
    
class FolderView extends Component {

    constructor() {
        super()

        this.state = {
            isFavourite : false,
            icon : favIconBf,
            menuVisibility : false
        }

        this.changeFavourite = this.changeFavourite.bind(this)
        this.showMenu = this.showMenu.bind(this)
    }


    async changeFavourite(file) {

        const url = 'https://15.206.165.104:8080/api/files/' + file.fileId + '/details'

        var bodyData = {
            favourite : !this.state.isFavourite
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
            if (response.status !== 200) {
                alert("failed adding to favourite")
            }
            else {
                this.setState( prevState => {
                    return {
                        isFavourite : !prevState.isFavourite,
                        icon : prevState.icon === favIcon ? favIconBf : favIcon,
                    }
                }) 
            }
        })
    }

    showMenu() {
        this.setState(prevState => {
            return {
                menuVisibility : !prevState.menuVisibility
            }
        })
    }

    render() {

        const logo = this.state.isDirectory ? floderLogo : fileLogo

        return (    
            <div className = 'file-view'>

                <div className = 'icons-view'>
                    <img src = {fileLogo} className = 'file-icon' alt = {floderLogo2}/> 
                    <img src = {this.state.icon} className = 'fav-icon' alt = {favIconBf} onClick = {() => this.changeFavourite(this.props.file)}/>

                </div>

                <div className = 'file-view-menu'>
                    <p className = 'file-name'> {this.props.file.fileName} </p>
                    <img src = {fileMenuImage} alt = {fileMenuImage} className = 'menu-icon' onClick = {this.showMenu}/>
                    {this.state.menuVisibility ? <MenuView file = {this.props.file} refreshData = {this.props.refreshData} menuVisibility = {() => this.showMenu}/> : null}
                </div>

            </div>
        )
    }
}

export default FolderView
