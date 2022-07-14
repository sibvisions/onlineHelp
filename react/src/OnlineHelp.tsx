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

import React, { FC, useState } from 'react';
import './OnlineHelp.scss';
import HelpMenu from './HelpMenu';

const OnlineHelp: FC = () => {

  const [helpUrl, setHelpUrl] = useState("");

  const setUrlCallback = (url: string|undefined) => {
    if (url) {
      setHelpUrl(url)
    }
    else {
      setHelpUrl("");
    }
  }

  return (
    <div className='online-help-main'>
      <div className='online-help-frame'>
        <div className='online-help-topbar'>
          <span className='online-help-topbar-header'>Online-Help</span>
          <img className='online-help-topbar-logo' alt='company logo' src={process.env.PUBLIC_URL + '/assets/company.png'} />
        </div>
        <div className='online-help-menu-wrapper'>
          <HelpMenu key={'help-menu'} setUrlCallback={setUrlCallback} />
        </div>
      </div>
      <div className='online-help-content'>
        {helpUrl ? <iframe style={{ width: "100%", height: "100%", border: "none", display: "block" }} src={'http://localhost:8085/onlineHelpServices' + helpUrl} /> : <></>}
      </div>
    </div>
  );
}

export default OnlineHelp;
