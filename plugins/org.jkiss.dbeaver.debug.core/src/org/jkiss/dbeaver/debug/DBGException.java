/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 * Copyright (C) 2017-2018 Andrew Khitrin (ahitrin@gmail.com)
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jkiss.dbeaver.debug;

import org.jkiss.dbeaver.DBDatabaseException;
import org.jkiss.dbeaver.model.DBPDataSource;

@SuppressWarnings("serial")
public class DBGException extends DBDatabaseException {

    public DBGException(String message, Throwable e) {
        super(message, e);
    }

    public DBGException(String message) {
        super(message);
    }

    public DBGException(Throwable cause, DBPDataSource dataSource) {
        super(cause, dataSource);
    }

    public DBGException(String message, Throwable cause, DBPDataSource dataSource) {
        super(message, cause, dataSource);
    }
}
