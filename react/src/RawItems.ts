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

export type HelpItem = {
    id?: number
    type: "folder"|"file"|"download"
    name: string
    icon?: string
    url?: string
    parentID: number
}

export type HelpItemRoot = {
    id: -1
    name: "ROOT"
}

export const rawItems: Array<HelpItem|HelpItemRoot> = [
    {
        "id": -1,
        "name": "ROOT"
    }, 
    {
        "id": 1,
        "type": "folder",
        "name": "General",
        "icon": "/images/tree/general.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/01_General/index.html",
        "parentID": -1
    }, 
    {
        "id": 2,
        "type": "folder",
        "name": "Application",
        "icon": "/images/tree/application.png",
        "parentID": 1
    }, 
    {
        "type": "file",
        "name": "Contacts",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/01_General/01_Application/Contacts.html",
        "parentID": 2
    }, 
    {
        "id": 3,
        "type": "folder",
        "name": "Userinterface",
        "icon": "/images/tree/folder.png",
        "parentID": 1
    }, 
    {
        "type": "file",
        "name": "ß älapalöma & html",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/01_General/02_Userinterface/01_%df%20%e4lapal%f6ma%20%26%20html.html",
        "parentID": 3
    }, 
    {
        "type": "file",
        "name": "ö",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/01_General/02_Userinterface/02_%f6.html",
        "parentID": 3
    }, 
    {
        "type": "file",
        "name": "&",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/01_General/02_Userinterface/%26.html",
        "parentID": 3
    }, 
    {
        "type": "file",
        "name": "A without sort prefix",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/01_General/02_Userinterface/A%20without%20sort%20prefix.html",
        "parentID": 3
    }, 
    {
        "type": "file",
        "name": "Spaces 1 2 3",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/01_General/02_Userinterface/Spaces%201%202%203.html",
        "parentID": 3
    }, 
    {
        "id": 4,
        "type": "folder",
        "name": "Application",
        "icon": "/images/tree/application.png",
        "parentID": -1
    }, 
    {
        "id": 5,
        "type": "folder",
        "name": "Master data",
        "icon": "/images/tree/folder.png",
        "parentID": 4
    }, 
    {
        "type": "file",
        "name": "Contacts",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/02_Application/01_Master%20data/Contacts$com.sibvisions.apps.showcase.frames.ContactsFrame.html",
        "parentID": 5
    }, 
    {
        "type": "file",
        "name": "MyCompany",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/02_Application/01_Master%20data/MyCompany$demo.screens.MyCompanyWorkScreen.html",
        "parentID": 5
    }, 
    {
        "type": "file",
        "name": "MyCompanyDetail",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/02_Application/01_Master%20data/MyCompanyDetail$demo.screens.MyCompanyDetailWorkScreen.html",
        "parentID": 5
    }, 
    {
        "type": "file",
        "name": "MyStamm",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/02_Application/01_Master%20data/MyStamm$demo.screens.stamm.MyStammWorkScreen.html",
        "parentID": 5
    }, 
    {
        "id": 6,
        "type": "folder",
        "name": "Documents",
        "icon": "/images/tree/documents.png",
        "parentID": -1
    }, 
    {
        "type": "download",
        "name": "&&&",
        "icon": "/images/tree/doc.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/03_Documents/%26%26%26.doc",
        "parentID": 6
    }, 
    {
        "type": "download",
        "name": "Införmationdocument",
        "icon": "/images/tree/doc.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/03_Documents/Inf%f6rmationdocument.doc",
        "parentID": 6
    }, 
    {
        "type": "download",
        "name": "references",
        "icon": "/images/tree/pdf.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/03_Documents/references.pdf",
        "parentID": 6
    }, 
    {
        "type": "download",
        "name": "€o - Copy",
        "icon": "/images/tree/doc.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/03_Documents/%3fo%20-%20Copy.doc",
        "parentID": 6
    }, 
    {
        "type": "file",
        "name": "System overview",
        "icon": "/images/tree/html.png",
        "url": "//tools/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp2/wtpwebapps/onlineHelpServices/structure/System%20overview.html",
        "parentID": -1
    }
]
  