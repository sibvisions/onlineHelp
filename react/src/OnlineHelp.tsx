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

import React, { FC, useLayoutEffect, useState, useMemo } from 'react';
import './OnlineHelp.scss';
import HelpMenu from './HelpMenu';
import { sendRequest } from './RequestService';
import Loadingscreen from './Loadingscreen';
import { Button} from 'primereact/button';
import { MenuItem } from 'primereact/menuitem'
import { buildModel } from './util/BuildModel';
import { translation } from './util/Translation';
import { useRef } from 'react';

const urlParams = new Map(new URLSearchParams(window.location.search));

export const language = urlParams.get("language") || navigator.language || "en";

export const helpPath = urlParams.get("path") ? "path=/" + urlParams.get("path") : "path=/";

export let homeUrl: { url:string } = { url: "" };

export enum ENDPOINTS {
  CONTENT = "api/content",
  TRANSLATION = "api/translation",
  SEARCH = "api/search"
}

/** This component renders the online-help page */
const OnlineHelp: FC = () => {
  /** The current active help-url */
  const [helpUrl, setHelpUrl] = useState<{ url: string, flag: boolean }>({ url: "", flag: false });

  const [contentModel, setContentModel] = useState<MenuItem[] | undefined>(undefined)

  const [translationReady, setTranslationReady] = useState<boolean>(false);

  const helpReady = useMemo(() => contentModel && translationReady, [contentModel, translationReady]);

  const helpHistory = useRef<{ urlArr: string[], activeIndex: number  }>({ urlArr: [], activeIndex: -1 });

  /** A callback to update the help-url state */
  const setUrlCallback = (url?: string, withHistory?: boolean) => {
    if (url && url !== "search-remove") {
      setHelpUrl(prevState => ({ url: url, flag: !prevState.flag }));
      if (!withHistory) {
        if (helpHistory.current.activeIndex !== helpHistory.current.urlArr.length - 1) {
          helpHistory.current.urlArr = helpHistory.current.urlArr.slice(0, helpHistory.current.activeIndex + 1)
          helpHistory.current.activeIndex = helpHistory.current.urlArr.length - 1;
        }

        helpHistory.current.urlArr.push(url);
        helpHistory.current.activeIndex++;
      }
    }
    else if (url === "search-remove") {
      setHelpUrl(prevState => ({ url: "", flag: !prevState.flag }))
    }
    else {
      setHelpUrl(prevState => ({ url: prevState.url, flag: !prevState.flag }));
    }
  }



  useLayoutEffect(() => {
    sendRequest(ENDPOINTS.CONTENT, "")
      .then((result) => {
        setContentModel(buildModel(result, contentModel || [], setUrlCallback));
      });
    sendRequest(ENDPOINTS.TRANSLATION, "&language=" + language)
      .then((result) => {
        if (result.asProperties) {
          const translationKeys = Object.keys(result.asProperties);
          const translationValues = Object.values(result.asProperties) as string[];

          for (let i = 0; i < translationKeys.length; i++) {
            translation.set(translationKeys[i], translationValues[i]);

            if (translationKeys[i].includes("help system")) {
              document.title = translationValues[i];
            }
          }
        }
      })
      .then(() => setTranslationReady(true))
  }, []);

  return (
    helpReady ?
      <div className='online-help-main'>
        <div className='online-help-frame'>
          <div className='online-help-topbar'>
            <div className='online-help-topbar-header'>
              <span className='online-help-topbar-header-top'>{translation.get("You are in the help system of APPLICATION.")}</span>
              <span className='online-help-topbar-header-bottom'>{translation.get("The table of contents supplies an overview of all topics.")}</span>
            </div>
            <img className='online-help-topbar-logo' alt='company logo' src={'/assets/company.png'} />
          </div>
          <div className='online-help-menu-wrapper'>
            <HelpMenu key={'help-menu'} helpUrl={helpUrl} contentModel={contentModel} setUrlCallback={setUrlCallback} />
          </div>
        </div>
        <div className='online-help-center'>
          <div className='online-help-button-bar'>
            { homeUrl.url && 
            <Button 
              icon="pi pi-home" 
              onClick={() => {
                setUrlCallback(homeUrl.url, false)
              }}
              tooltip={translation.get("Home")}
              tooltipOptions={{ style: { opacity: "0.85" }, position:"bottom", mouseTrack: true, mouseTrackTop: 30 }} />
            }
            <Button 
              icon="pi pi-arrow-left" 
              disabled={helpHistory.current.urlArr.length === 0 || helpHistory.current.activeIndex === 0}
              onClick={() => {
                helpHistory.current.activeIndex--;
                setUrlCallback(helpHistory.current.urlArr[helpHistory.current.activeIndex], true);
              }}
              tooltip={translation.get("Previous")}
              tooltipOptions={{ style: { opacity: "0.85" }, position:"bottom", mouseTrack: true, mouseTrackTop: 30 }} />
            <Button 
              icon="pi pi-arrow-right" 
              disabled={helpHistory.current.urlArr.length === 0 || helpHistory.current.activeIndex === helpHistory.current.urlArr.length -1}
              onClick={() => {
                helpHistory.current.activeIndex++;
                setUrlCallback(helpHistory.current.urlArr[helpHistory.current.activeIndex], true);
              }}
              tooltip={translation.get("Next")}
              tooltipOptions={{ style: { opacity: "0.85" }, position:"bottom", mouseTrack: true, mouseTrackTop: 30 }} />
            <Button
              icon="pi pi-print"
              onClick={() => {
                if (helpUrl.url) {
                  window.print();
                }
              }}
              tooltip={translation.get("Print")}
              tooltipOptions={{ style: { opacity: "0.85" }, position:"bottom", mouseTrack: true, mouseTrackTop: 30 }} />
          </div>
          <div id='test' className='online-help-content'>
            {(helpUrl.url || homeUrl.url) && <iframe title='help-content' id="help-content" name="help-content" style={{ width: "100%", height: "100%", border: "none", display: "block" }} src={'http://localhost:8080/onlineHelpServices' + (helpUrl.url ? helpUrl.url : homeUrl.url)} />}
          </div>
        </div>

      </div>
      :
      <Loadingscreen />
  );
}

export default OnlineHelp;
