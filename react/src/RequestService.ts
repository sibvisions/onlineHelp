/* Copyright 2022 SIB Visions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import { helpPath } from "./OnlineHelp";

/** The base-url for server requests */
export const baseUrl = "http://localhost:8085/onlineHelpServices/"

/**
 * Returns a promise which times out and throws an error and displays dialog after given ms
 * @param promise - the promise
 * @param ms - the ms to wait before a timeout
 */
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

/**
 * Builds a request to send to the server
 * @returns - a request to send to the server
 */
function buildReqOpts():RequestInit {
    // if (request) {
    //     return {
    //         method: 'POST',
    //         body: JSON.stringify(request),
    //         credentials:"include",
    //     };
    // }
    // else {
        return {
            method: 'GET',
            credentials:"include",
            headers: {
                contentType: "application/json; charset=ISO-8859-1"
            }
        };
    //}
}

/**
 * Sends a request to the server
 * @param endpoint - the endpoint to send the request to
 * @param option - the additional information to send in the endpoint
 */
export function sendRequest(endpoint:string, option: string) {
    let promise = new Promise<any>((resolve) => {
        timeoutRequest(fetch(baseUrl + "services/help/" + endpoint + "?" + helpPath + option, buildReqOpts()), 10000).then((response:any) => resolve(response.json()))
    });
    return promise
}