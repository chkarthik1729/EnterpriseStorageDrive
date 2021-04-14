import React from 'react'
import DisplayPath from './Path/DisplayPath'
import ProfileCard from './ProfileCard'
import VerticalBar from './verticalNavBar/VerticalBar'
import FilesSpace from './filesDisplay/FilesSpace.js'

function RedirectPage() {
    
    return (
        <div> 
            <VerticalBar />
            <ProfileCard />
            <DisplayPath />
            <FilesSpace />
        </div>
    )    

}

export default RedirectPage