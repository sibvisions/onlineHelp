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

import React, { FC, useEffect, useCallback } from "react";
import './HelpMenu.scss'
import { PanelMenu } from 'primereact/panelmenu';
import { MenuItem } from "primereact/menuitem";
import { IconField } from "primereact/iconfield";
import { InputIcon } from "primereact/inputicon";
import { InputText } from 'primereact/inputtext';
import { useState } from "react";
import { SearchItem } from "./Types";
import { baseUrl, sendRequest } from "./RequestService";
import { ListBox } from "primereact/listbox";
import { ENDPOINTS } from "./OnlineHelp";
import { translation } from "./util/Translation";

/** Interface for the Help-Menu */
interface IHelpMenu {
    helpUrl: { url: string, flag: boolean }
    setUrlCallback: (url: string | undefined) => void,
    contentModel: MenuItem[]|undefined
}

/**
 * This component renders a menu, which contains the help-items to display the help pages or to download files.
 * @param props - helpUrl: the current url to be displayed, setUrlCallback: a callback to set the url.
 */
const HelpMenu: FC<IHelpMenu> = (props) => {
    /** The text entered into the search-field */
    const [searchText, setSearchText] = useState<string>("");

    const [selectedSearchItem, setSelectedSearchItem] = useState<SearchItem|undefined>(undefined);

    /** The PrimeReact menu-model */
    const [menuModel, setModelModel] = useState<MenuItem[]>([]);

    const [listBoxModel, setListBoxModel] = useState<SearchItem[]>();

    /** Setting a classname if the item is active to display a blue text */
    useEffect(() => {
        for (let item of document.getElementsByClassName("item-has-url")) {
            item.classList.remove("item-active");

            if (item.classList.contains(props.helpUrl.url)) {
                item.classList.add("item-active");
            }
        }
    }, [props.helpUrl.url, props.helpUrl.flag]);

    const itemTemplate = (option: SearchItem) => {
        return (
            <div className="search-item">
                <img
                    className="search-item-icon"
                    alt={option.name}
                    src={baseUrl + option.icon} />
                <div className="search-item-text">{option.name}</div>
            </div>
        );
    }

    const handleSearchMode = (text: string) => {
        const menuElem = document.getElementById("online-help-menu");
        const listElem = document.getElementById("online-help-listbox");
        if (menuElem && listElem) {
            if (text) {
                menuElem.classList.add("search-mode-enabled");
                listElem.classList.add("search-mode-enabled");
            }
            else {
                menuElem.classList.remove("search-mode-enabled");
                listElem.classList.remove("search-mode-enabled");
            }
        }
    }

    const handleSendSearchText = useCallback((text: string) => {
        sendRequest(ENDPOINTS.SEARCH, "&term=" + text)
        .then((result) => {
            setListBoxModel(!text ? [] : result);
            handleSearchMode(text);
        });
    }, [searchText])

    return (
        <>
            <IconField iconPosition="left" className="search-wrapper p-icon-field-right">
                <InputIcon className="pi pi-search" />
                <InputText
                    id="search"
                    type="text"
                    value={searchText}
                    autoComplete="search"
                    placeholder={translation.get("Search")}
                    onChange={(event) => setSearchText(event.target.value) } 
                    onBlur={() => handleSendSearchText(searchText) }
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            handleSendSearchText(searchText);
                        }
                    }}
                />
                <InputIcon className="clear-icon pi pi-times" onClick={() => {
                    setSearchText("");
                    handleSendSearchText("")
                }} />
            </IconField>
            <PanelMenu id="online-help-menu" model={props.contentModel} multiple />
            <ListBox 
                id="online-help-listbox" 
                value={selectedSearchItem} 
                options={listBoxModel} 
                optionLabel="name" 
                itemTemplate={itemTemplate} 
                onChange={(e) => {
                    setSelectedSearchItem(e.value);
                    if (e.value) {
                        props.setUrlCallback(e.value.url);
                    }
                    else {
                        props.setUrlCallback("search-remove");
                    }
                }} />
        </>

    )
}
export default HelpMenu