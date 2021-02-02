/*
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.plugins.awsglue

import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportGroupConfig
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportParameterConfig
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.parameter.DataModelImporterProviderServiceParameters

class AwsGlueDataModelImporterProviderServiceParameters extends DataModelImporterProviderServiceParameters {

    @ImportParameterConfig(
        displayName = 'Region',
        description = 'AWS Region. If not specified then eu-west-2 will be used.',
        optional = true,
        order = 1,
        group = @ImportGroupConfig(
            name = 'AWS Connection',
            order = -1
        )
    )
    String regionName

    @ImportParameterConfig(
        displayName = 'Access Key ID',
        description = 'Your AWS Access Key ID',
        order = 2,
        group = @ImportGroupConfig(
            name = 'AWS Connection',
            order = -1
        )
    )
    String accessKeyId

    @ImportParameterConfig(
        displayName = 'Secret Access Key',
        description = 'Your AWS Secret Access Key',
        order = 3,
        group = @ImportGroupConfig(
            name = 'AWS Connection',
            order = -1
        )
    )
    String secretAccessKey    

}
