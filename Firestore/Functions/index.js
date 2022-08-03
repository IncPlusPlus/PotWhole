const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const database = admin.firestore();

// Adds new user to firestore
exports.createAuthenticatedUserDocument = functions.auth.user().onCreate(async (user) => {

    // Add new document for new user (document id of uid) to users collection
    await database.collection('users').doc(user.uid).set({
        email: user.email,
    });

    // return the status of the function
    return {
        returnStatus: 'New authenticated user has been added to users collection',
    };
});

exports.updateUserDocument = functions.https.onCall(async (data, context) => {

    const uid = context.auth.uid

    if (uid != undefined) {
        const dataField = JSON.parse(data);

        const documentGrab = await database.collection('users').doc(uid).get();

        await database.collection('users').doc(uid.toString()).set({
            email: documentGrab.data().email,
            username: dataField.username,
            //profilePicture: dataField.profilePicture,
        });

        return {
            returnStatus: 'Users information has been updated.'
        };
        
    } else {
        return {
            returnStatus: 'Functions failed to execute: User is not authenticated'
        }
    }
});

exports.getUserDocument = functions.https.onCall(async (data, context) => {

    // Grab user identifier to reference user
    const uid = context.auth.uid

    // perform function on the condition that uid is not undefined
    if (uid != undefined) {
        //Parse data sent to database from application instance
        const dataField = JSON.parse(data);

        // Retrive the document with the uid identifier
        const documentGrab = await database.collection('users').doc(dataField.uid).get();

        // Build the return data, including relevant information
        userData = {
            username: documentGrab.data().username,
        };

        // return the status of the function and relevant report data
        return {
            returnStatus: 'Existing report information from uid document has been returned to requesting user',
            report: userData
        };

    } else {
        // return the status of the function that did not execute
        return {
            returnStatus: 'Functions failed to execute: User is not authenticated'
        }
    }
});

exports.getUserReports = functions.https.onCall(async (data, context) => {
    const uid = context.auth.uid

    if (uid != undefined) {
        const snapshot = await database.collection('users').doc(uid.toString()).collection('userCreatedReports').get();

        const documents = snapshot.docs.map(doc => doc.data().ref._path.segments[1]);

        const array = [];

        for (const document of documents) {

            const documentGrab = await database.collection('reports').doc(document.toString()).get();

            array.push({
                title: documentGrab.data().title,
                description: documentGrab.data().description,
                originalReporterUid: documentGrab.data().originalReporterUid,
                originalReporterUsername: documentGrab.data().originalReporterUsername,
                image: documentGrab.data().image,
                location: documentGrab.data().location,
                timestamp: documentGrab.data().timestamp
            });
        }

        return array;

    } else {
            // return the status of the function that did not execute
            return {
                returnStatus: 'Functions failed to execute: User is not authenticated'
            }
    }
});

// Create a Report in Firestore with provided data (Require all fields)
exports.createReportDocument = functions.https.onCall(async (data, context) => {

    // Grab user identifier to reference user
    const uid = context.auth.uid

    // perform function on the condition that uid is not undefined
    if (uid != undefined) {
        // Parse data sent to database from authenticated instance
        const dataField = JSON.parse(data);

        console.log(uid);
        const documentGrab = await database.collection('users').doc(uid).get();

        // Create and Populate a reoprt document with recieved data
        const reportDocument = await database.collection('reports').add({
            title: dataField.title,
            description: dataField.description,
            image: dataField.image,
            location: {
                latitude: parseFloat(dataField.location.latitude),
                longitude: parseFloat(dataField.location.longitude)
            },
            originalReporterUid: uid,
            originalReporterUsername: documentGrab.data().username,
            timestamp: dataField.timestamp,
        });

        // Define the path to the original report document from a users sub collection
        const referencePath = 'reports/' + reportDocument.id;

        // Create sub collection and reference reports sumbitted by the specified user
        await database.collection('users').doc(uid.toString()).collection('userCreatedReports').add({
            ref: database.doc(referencePath)
        });

        //return the status of the function and the rid of newly created report
        return {
            returnStatus: 'New reoprt has been added to the reports collection',
            rid: reportDocument.id
        };
    } else {
        // return the status of the function that did not execute
        return {
            returnStatus: 'Functions failed to execute: User is not authenticated'
        }
    }
});

// Returns Report data for provided rid
exports.getReportDocument = functions.https.onCall(async (data, context) => {

    // Grab user identifier to reference user
    const uid = context.auth.uid

    // perform function on the condition that uid is not undefined
    if (uid != undefined) {
        //Parse data sent to database from application instance
        const dataField = JSON.parse(data);

        // Retrive the document with the rid identifier
        const documentGrab = await database.collection('reports').doc(dataField.rid).get();

        // Build the return data, including relevant information
        reportData = {
            title: documentGrab.data().title,
            description: documentGrab.data().description,
            originalReporterUid: documentGrab.data().originalReporterUid,
            originalReporterUsername: documentGrab.data().username,
            image: documentGrab.data().image,
            location: documentGrab.data().location,
            timestamp: documentGrab.data().timestamp
        };

        // return the status of the function and relevant report data
        return {
            returnStatus: 'Existing report information from rid document has been returned to requesting user',
            report: reportData
        };
    } else {
        // return the status of the function that did not execute
        return {
            returnStatus: 'Functions failed to execute: User is not authenticated'
        }
    }
});

exports.getAllReportDocuments = functions.https.onCall(async (data, context) => {
    const snapshot = await database.collection('reports').get();

    return snapshot.docs.map(doc => doc.data());
});