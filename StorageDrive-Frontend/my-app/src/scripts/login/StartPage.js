import React, { Component } from 'react'
import {Route, Switch} from 'react-router-dom'
import LoginPage from './LoginPage'
import RegisterPage from './RegisterPage'
import Home from '../Home'
import {BrowserRouter} from 'react-router-dom';
import {createBrowserHistory} from 'history';


const history = createBrowserHistory();


class StartPage extends Component {

    constructor() {
        super()

        this.state = {
            currentPage : 'login',
            user : ''
        }

        this.changePage = this.changePage.bind(this)
        this.handleAuth = this.handleAuth.bind(this)
    }

    changePage() {
        this.setState(prevState => {
            return {
                currentPage : prevState.currentPage === 'login' ? 'register' : 'login',
                user : prevState.user
            }
        }) 
    }


    handleAuth(userName) {
        this.setState( (prevState) => {return {currentPage : prevState.currentPage, user : userName}})

        history.push('/home')
        window.location.reload(false);
    }

    handleLogOut() {
        history.push('/')
        window.location.reload(false);
    }

    render() {

        return (

            <Switch>

                <Route exact path = "/">
                    {
                        this.state.currentPage === "login" ?
                        <LoginPage isAuthenticated = {this.state.isAuthenticated} changePage = {this.changePage} handleAuth = {this.handleAuth}/> :
                        <RegisterPage isAuthenticated = {this.state.isAuthenticated} changePage = {this.changePage} handleAuth = {this.handleAuth}/>
                    }
                </Route>

                <Route exact path = "/home">
                    <Home isAuthenticated = {this.state.isAuthenticated} logOut = {this.handleLogOut} handleAuth = {this.handleAuth}/>
                    {console.log( this.state)}
                </Route>

            </Switch>
            
        )

    }
}

export default StartPage