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

// Create a Report in Firestore with provided data (Require all fields)
exports.createReportDocument = functions.https.onCall(async (data, context) => {

    // Grab user identifier to reference user
    const uid = context.auth.uid

    // perform function on the condition that uid is not undefined
    if (uid != undefined) {
        // Parse data sent to database from authenticated instance
        const dataField = JSON.parse(data);

        // Create and Populate a reoprt document with recieved data
        const reportDocument = await database.collection('reports').add({
            title: dataField.title,
            description: dataField.description,
            location: {
                latitude: parseFloat(dataField.location.latitude),
                longitude: parseFloat(dataField.location.longitude)
            },
            timestamp: new Date(Date.now())
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