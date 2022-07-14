const baseUrl = "http://localhost:8085/onlineHelpServices/services/help/"

function timeoutRequest(promise: Promise<any>, ms: number) {
    return new Promise((resolve, reject) => {
        let timeoutId= setTimeout(() => {
            reject(new Error("timeOut"))
        }, ms);
        promise.then(res => {
                clearTimeout(timeoutId);
                resolve(res);
            },
            err => {
                clearTimeout(timeoutId);
                reject(err);
        });
    });
}

function buildReqOpts(request:any):RequestInit {
    if (request && request.upload) {
        return {
            method: 'GET',
            credentials:"include",
        };
    }
    else {
        return {
            method: 'GET',
            credentials:"include",
            headers: {
                contentType: "application/json; charset=ISO-8859-1"
            }
        };
    }
}

export function sendRequest(request: any, endpoint:string) {
    let promise = new Promise<any>((resolve) => {
        timeoutRequest(fetch(baseUrl + endpoint, buildReqOpts(request)), 10000).then((response:any) => resolve(response.json()))
    });
    return promise
}