import React, {Component} from 'react'
import '../styles/ProfileCard.css'
import ProfileView from './ProfileView'
import LogOut from './signOut/LogOut'

class ProfileCard extends Component {

    constructor() {
        super()

        this.state = {
            display : false,
            randomColor : '#' + Math.floor(Math.random()*16777215).toString(16),
            userName : ''
        }

        this.displayProfileCard = this.displayProfileCard.bind(this)
    }

    displayProfileCard() {
        this.setState(prevState => {
                return {
                    display : !prevState.display
                }
            }
        )
            
    }

   render() {
        
        return (
            <div className = 'profile-container'>
                <button type = "button" className = "logout-btn" onClick = {this.props.logOut}> Sign Out </button>        
            </div>
        )
   }
   
}

export default ProfileCard