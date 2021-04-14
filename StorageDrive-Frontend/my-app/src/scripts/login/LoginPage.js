import React, { Component } from 'react'
import '../../styles/Login.css'
import {withRouter} from 'react-router'
import {createBrowserHistory} from 'history';


const history = createBrowserHistory();


class LoginPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            email : '',
            password : ''
        }

        this.handleInputData = this.handleInputData.bind(this)
        this.handleAthentication = this.handleAthentication.bind(this)

    }

    handleInputData(event) {
        const {name, value} = event.target
        this.setState({
            [name] : event.target.value
        })
    }

    componentDidMount() {
        /*fetch('https://15.206.165.104:8080/api/files', {
            method: 'GET',
            credentials: 'include',
            redirect : 'manual'
        })
        .then(response => {
            if (response.status === 200) {
               history.push('/home')
               window.location.reload(false)
            }
        }).catch(error => {
            console.log(error);
        })*/
    }


    handleAthentication(e) {

        e.preventDefault();
        console.log("authentication...")

        var encodedStr = "Basic " + btoa(this.state.email + ":" + this.state.password)

        var myHeaders = new Headers();
        myHeaders.append("Authorization", encodedStr);

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            redirect: 'follow',
            credentials: "include"
        };

        fetch("https://15.206.165.104:8080/api/sign-in", requestOptions)
        .then(async response => {
            if (response.status === 200) {
                console.log("auth sucess")
            }
            else {
                alert("Authentication Failed! Try Again")
            }
            return response.text();
        })
        .then(data => {
                this.props.handleAuth(data)
            }
        )
        .catch(error => console.log('error', error));
    }
    

    render() {
        return (

            <div className ="main">
                <p className ="sign" align="center">Sign in</p>

                <form className ="form1">
                    <input className = "un " type="text" align="center" placeholder="User Name" name = 'email' onChange = {(e) => this.handleInputData(e)} value = {this.state.email}/>
                    <input className = "pass" type="password" align="center" placeholder="Password" name = 'password' onChange = { (e) => this.handleInputData(e)} value = {this.state.password}/>

                    <button className ="submit" align="center"  onClick = {(e) => this.handleAthentication(e)}> Sign in </button>
                    
                    <p className="forgot" align="center"><a  onClick = {this.props.changePage}> New User </a> </p>
                       
                </form>
                  
            </div>

             
        )
    }
}

export default withRouter(LoginPage)