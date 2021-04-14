# Enterprise Storage Drive

#### Crio Winter of Doing 2021 - Vicara T7
This project is developed as part of the externship we did with Crio.Do

Enterprise Storage Drive is a storage drive service like Google Drive. Our service is different from Google Drive in that it can integrate with multiple storage providers like Amazon S3, Azure Blob Storage or any other storage provider. This gives the flexibility to move between storage providers as needs and costs fluctuate. And, unlike Google Drive, the service can charge users according to usage as the underlying storage providers do the same.

#### Tech Stack:
**Backend:** Java SpringBoot, MongoDB to store user details and Redis to store Sessions

**Frontend:** HTML, CSS, React

This repository contains both the frontend and backend code. 
The backend is a rest service and frontend is a single page web app that works by calling the rest endpoints.

The backend contains two Maven projects: RestServer and HierarchicalStorageService, and the RestServer depends on HierarchicalStorageService. So, you need to build HierarchicalStorageService before RestServer.

The frontend code contains hard coded IP address of the backend, so you will also want to replace that with backend address.

The entire explanation of this project can be found [here](https://drive.google.com/file/d/1LqsVge8KNDzJL-1yl8qyLHL54H3GKp_7/view?usp=sharing).

[here]: https://drive.google.com/file/d/1LqsVge8KNDzJL-1yl8qyLHL54H3GKp_7/view?usp=sharing
API Documentation can be found [here](https://github.com/chkarthik1729/EnterpriseStorageDrive/wiki/API-Documentation)