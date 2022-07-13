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

import React, { FC, useRef } from "react";
import { MenuItem } from "primereact/menuitem";
import { useState } from "react";
import { HelpItem, HelpItemRoot, rawItems } from "./RawItems";

interface HelpMenuItem extends MenuItem {
    id: number
}

const HelpMenu: FC = () => {
    const modelMap = useRef<Map<string, number>>(new Map())

    const buildModel = (rawItems: Array<HelpItem|HelpItemRoot>, currentModelState:HelpMenuItem[]): HelpMenuItem[] => {
        const primeMenu = [...currentModelState];

        const itemIsNotRootItem = (item: HelpItem | HelpItemRoot): item is HelpItem => {
            return (item as HelpItem).parentID !== undefined;
        }

        const getPathToItem = (id: number) => {
            const pathArray = [];

            const startId = id;

            while (id !== -1) {
                if (id !== startId) {
                    pathArray.push(id);
                }
                
                id = modelMap.current.get(id.toString()) as number;
            }
            return pathArray.reverse();
        }

        rawItems.forEach(rawItem => {
            //console.log(rawItem)
            if (itemIsNotRootItem(rawItem)) {
                if (rawItem.id) {
                    modelMap.current.set(rawItem.id.toString(), rawItem.parentID);
                }
            }
        });

        return primeMenu
    }

    const [model, setModel] = useState<HelpMenuItem[]>(buildModel(rawItems, []))

    return (
        <div>
            test
        </div>
    )
}
export default HelpMenu