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
import { MenuItem } from 'primereact';
import { buildModel } from './util/BuildModel';
import { translation } from './util/translation';

const urlParams = new Map(new URLSearchParams(window.location.search));

export const language = urlParams.get("language") || navigator.language || "en";

export const helpPath = urlParams.get("path") ? "path=/" + urlParams.get("path") : "path=/";

export enum ENDPOINTS {
  CONTENT = "api/content",
  TRANSLATION = "api/translation",
  SEARCH = "api/search"
}

/** This component renders the online-help page */
const OnlineHelp: FC = () => {
  /** The current active help-url */
  const [helpUrl, setHelpUrl] = useState<{ url: string, flag: boolean }>({ url: "", flag: false });

  /** A callback to update the help-url state */
  const setUrlCallback = (url?: string | undefined) => {
    if (url && url !== "search-remove") {
      setHelpUrl(prevState => ({ url: url, flag: !prevState.flag }))
    }
    else if (url === "search-remove") {
      setHelpUrl(prevState => ({ url: "", flag: !prevState.flag }))
    }
    else {
      setHelpUrl(prevState => ({ url: prevState.url, flag: !prevState.flag }));
    }
  }

  const [contentModel, setContentModel] = useState<MenuItem[] | undefined>(undefined)

  const [translationReady, setTranslationReady] = useState<boolean>(false);

  const helpReady = useMemo(() => contentModel && translationReady, [contentModel, translationReady])

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
            <span className='online-help-topbar-header'>Online-Help</span>
            <img className='online-help-topbar-logo' alt='company logo' src={process.env.PUBLIC_URL + '/assets/company.png'} />
          </div>
          <div className='online-help-menu-wrapper'>
            <HelpMenu key={'help-menu'} helpUrl={helpUrl} contentModel={contentModel} setUrlCallback={setUrlCallback} />
          </div>
        </div>
        <div className='online-help-content'>
          {helpUrl.url && helpUrl.url !== "" && <iframe title='help-content' style={{ width: "100%", height: "100%", border: "none", display: "block" }} src={'http://localhost:8085/onlineHelpServices' + helpUrl.url} />}
        </div>
      </div>
      :
      <Loadingscreen />
  );
}

export default OnlineHelp;
