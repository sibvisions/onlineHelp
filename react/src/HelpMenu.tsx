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

import React, { FC, useEffect, useRef } from "react";
import { PanelMenu } from 'primereact/panelmenu';
import { MenuItem } from "primereact/menuitem";
import { InputText } from 'primereact/inputtext';
import { useState } from "react";
import { HelpItem, HelpItemRoot } from "./HelpItem";
import { baseUrl, sendRequest } from "./RequestService";

/** Interface for the Help-Menu */
interface IHelpMenu {
    helpUrl: {url: string, flag: boolean}
    setUrlCallback: (url: string|undefined) => void
}

/**
 * This component renders a menu, which contains the help-items to display the help pages or to download files.
 * @param props - helpUrl: the current url to be displayed, setUrlCallback: a callback to set the url.
 */
const HelpMenu: FC<IHelpMenu> = (props) => {
    /** A flat map of all help-items which contains the help-item-id as key and the parent-id as value to find the correct item in menu-building */
    const modelMap = useRef<Map<string, number>>(new Map());

    /** The text entered into the search-field */
    const [searchText, setSearchText] = useState<string>("");

    /**
     * Returns the current item-model so it can be used by a PrimeReact menu
     * @param rawItems - an array of the help-items
     * @param currentModelState - the current model-state
     */
    const buildModel = (rawItems: Array<HelpItem|HelpItemRoot>, currentModelState:MenuItem[]): MenuItem[] => {
        const primeMenu = [...currentModelState];

        /**
         * Typescript type-identifier to check if the help-item is a root-item or not
         * @param item - the help-item to check
         */
        const itemIsNotRootItem = (item: HelpItem | HelpItemRoot): item is HelpItem => {
            return (item as HelpItem).parentID !== undefined;
        }

        /**
         * Returns the path to the help-item in the menu as array, to find it in the menu
         * @param id - the id of the item
         * @param parentID - the parent id of the item
         */
        const getPathToItem = (id: number|undefined, parentID: number) => {
            const pathArray = [];

            // Go from search-item-level to top level
            while (id !== -1) {
                if (id === undefined) {
                    id = parentID;
                }
                else {
                    id = modelMap.current.get(id.toString()) as number;
                }

                if (id !== -1) {
                    pathArray.push(id)
                }
            }
            // reverse it because top-level needs to be first
            return pathArray.reverse();
        }

        rawItems.forEach(rawItem => {
            if (itemIsNotRootItem(rawItem)) {
                if (rawItem.id) {
                    modelMap.current.set(rawItem.id.toString(), rawItem.parentID);
                }

                const path = getPathToItem(rawItem.id, rawItem.parentID);

                const menuItem:MenuItem = {
                    id: rawItem.id?.toString() || undefined,
                    label: rawItem.name,
                    icon: rawItem.icon,
                    style: rawItem.icon ? {
                        '--iconWidth': '20px',
                        '--iconHeight': '20px',
                        '--iconImage': 'url(' + baseUrl + rawItem.icon + ')',
                    } : undefined,
                    items: rawItem.id ? [] : undefined,
                    className: rawItem.url ? rawItem.url + " item-has-url" : "" + " " +  rawItem.icon ? "custom-menu-icon" : "",
                    command: () => props.setUrlCallback(rawItem.url)
                }

                // If there is a path, go through it to find the correct spot for the help-item, else just add it to root
                if (path.length) {
                    let menuIterator: MenuItem[] | undefined = primeMenu;

                    for (let i = 0; i < path.length; i++) {
                        if (menuIterator) {
                            menuIterator = menuIterator.find(menuItem => menuItem.id === path[i].toString())?.items as MenuItem[] | undefined;
                        }
                    }
                    menuIterator?.push(menuItem)
                }
                else {
                    primeMenu.push(menuItem);
                }
            }
            else {
                modelMap.current.set("-1", -1);
            }
        })

        return primeMenu
    }

    /** The PrimeReact menu-model */
    const [model, setModel] = useState<MenuItem[]>([]);

    /** Initially fetching the help-items and building the model */
    useEffect(() => {
        sendRequest({}, "api/content?path=/")
        .then((result) => setModel(buildModel(result, model)));
    }, []);

    /** Setting a classname if the item is active to display a blue text */
    useEffect(() => {
        for (let item of document.getElementsByClassName("item-has-url")) {
            item.classList.remove("item-active");

            if (item.classList.contains(props.helpUrl.url)) {
                item.classList.add("item-active");
            }
        }
    }, [props.helpUrl.url, props.helpUrl.flag]);

    return (
        <>
            <span className="p-input-icon-left search-wrapper">
                <i className="pi pi-search" />
                <InputText value={searchText} onChange={(event) => setSearchText(event.target.value)} placeholder="Search" />
            </span>
            <PanelMenu model={model} multiple />
        </>
        
    )
}
export default HelpMenu