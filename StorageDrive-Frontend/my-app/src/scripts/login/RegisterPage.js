import React, { Component } from 'react'
import '../../styles/Register.css'
import StorageMenu from './StorageMenu'

import {createBrowserHistory} from 'history';

const history = createBrowserHistory();

class RegisterPage extends Component {

    constructor() {
        super()

        this.state = {
            email : '',
            password : '',
            storageProvider : 'amazon-s3'
        }

        this.handleInputData = this.handleInputData.bind(this)
        this.handleRegistration = this.handleRegistration.bind(this)

    }

    handleInputData(event) {
        const {name, value} = event.target
        this.setState({
            [name] : event.target.value
        })
    }

    handleRegistration() {

        const payload = {
            email : this.state.email,
            password : this.state.password,
            storageProvider : this.state.storageProvider
        }

        console.log(payload)

        fetch('https://15.206.165.104:8080/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        }).then
        (response => {
            if (response.status === 200) {
                alert("Register Sucessfully")
                history.push('/')
                window.location.reload(false)
            }
        })
    }

    

    render() {
        return (

            <div className ="main">
                <p className ="sign" align="center">Register</p>

                <div className ="form1">
                    
                    <input className = "un " type="text" align="center" placeholder="User Name" name = 'email' onChange = {(e) => this.handleInputData(e)} value = {this.state.email}/>
                    <input className = "pass" type="password" align="center" placeholder="Password" name = 'password' onChange = { (e) => this.handleInputData(e)} value = {this.state.password}/>


                    <div className = 'storage-container'>
                        <div className ="container">
                            <select className="storage-list" name = 'storageProvider' value = {this.state.storageProvider} onChange = {(e) => this.handleInputData(e)}>
                                <option value="" data-display-text="Storage-providers">Amazon S3</option>
                                <option value="Azure">Azure</option>
                                <option value="Google-cloud-service">Google Cloud Service</option>
                            </select>
                        </div>
                    </div>
                    
                    <button className="submit" align="center" onClick = {this.handleRegistration}> Register</button>
                    
                    <p className="forgot" align="center"><a onClick = {this.props.changePage}> Login</a> </p>
                                
                  
                </div>
            </div>
        )
    }
}

export default RegisterPage