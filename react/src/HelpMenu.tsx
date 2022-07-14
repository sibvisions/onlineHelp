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
import { useState } from "react";
import { HelpItem, HelpItemRoot } from "./HelpItem";
import { sendRequest } from "./RequestService";

interface IHelpMenu {
    setUrlCallback: (url: string|undefined) => void
}

const HelpMenu: FC<IHelpMenu> = (props) => {
    const modelMap = useRef<Map<string, number>>(new Map());

    const buildModel = (rawItems: Array<HelpItem|HelpItemRoot>, currentModelState:MenuItem[]): MenuItem[] => {
        const primeMenu = [...currentModelState];

        const itemIsNotRootItem = (item: HelpItem | HelpItemRoot): item is HelpItem => {
            return (item as HelpItem).parentID !== undefined;
        }

        const getPathToItem = (id: number|undefined, parentID: number) => {
            const pathArray = [];

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
            return pathArray.reverse();
        }

        const downloadFile = () => {
            console.log('downloading file')
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
                        '--iconWidth': '16px',
                        '--iconHeight': '16px',
                        '--iconImage': 'url(http://localhost:8085/onlineHelpServices/' + rawItem.icon + ')',
                    } : undefined,
                    items: rawItem.id ? [] : undefined,
                    className: rawItem.icon ? "custom-menu-icon" : "",
                    command: rawItem.url ? () => props.setUrlCallback(rawItem.url) : rawItem.type === "download" ? () => downloadFile() : undefined
                }

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

    const [model, setModel] = useState<MenuItem[]>([]);

    useEffect(() => {
        sendRequest({}, "api/content?path=/").then((result) => setModel(buildModel(result, model)));
    }, []);

    return (
        <PanelMenu model={model} multiple />
    )
}
export default HelpMenu