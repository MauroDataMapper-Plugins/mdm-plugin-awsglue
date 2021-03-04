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

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiInternalException
import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiNotYetImplementedException
import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiUnauthorizedException
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.DataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.awsglue.importer.parameter.AwsGlueDataModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.security.User

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.glue.GlueClient
import software.amazon.awssdk.services.glue.GlueClientBuilder
import software.amazon.awssdk.services.glue.model.GetDatabasesRequest
import software.amazon.awssdk.services.glue.model.GetDatabasesResponse
import software.amazon.awssdk.services.glue.model.GetTablesRequest
import software.amazon.awssdk.services.glue.model.GetTablesResponse
import software.amazon.awssdk.services.glue.model.GlueException

@Slf4j
class AwsGlueDataModelImporterProviderService
    extends DataModelImporterProviderService<AwsGlueDataModelImporterProviderServiceParameters> {

    @Autowired
    DataModelService dataModelService

    @Override
    String getDisplayName() {
        'AWS Glue Importer'
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    Boolean allowsExtraMetadataKeys() {
        true
    }

    @Override
    List<DataModel> importModels(User currentUser, AwsGlueDataModelImporterProviderServiceParameters params) {
        if (!currentUser) throw new ApiUnauthorizedException('GLUEIP01', 'User must be logged in to import model')
        log.debug("importDataModels")

        String namespace = "uk.ac.ox.softeng.maurodatamapper.plugins.awsglue"

        List<String> includeOnly = []

        if (params.schemaNames) {
            includeOnly = params.schemaNames.split(',') as List<String>
        }

        List<DataModel> imported = []

        StaticCredentialsProvider staticCredentialsProvider =
            StaticCredentialsProvider.create(AwsBasicCredentials.create(params.accessKeyId, params.secretAccessKey))

        //If no region provided then use eu-west-2
        GlueClientBuilder glueClientBuilder = GlueClient.builder().region(Region.of(params.regionName ?: 'eu-west-2')).
            credentialsProvider(staticCredentialsProvider)

        GlueClient glueClient = glueClientBuilder.build()

        try {
            GetDatabasesResponse response = glueClient.getDatabases(GetDatabasesRequest.builder().build())

            response.databaseList().each {database ->
                log.debug("AWS database ${database.name()}")
                if (!includeOnly || includeOnly.contains(database.name())) {
                    log.debug("importDataModel ${database.name()}")
                    DataModel dataModel = new DataModel(label: database.name(), type: DataModelType.DATA_ASSET)

                    //Add metadata
                    database.parameters().each {param ->
                        Metadata metadata = new Metadata(namespace: namespace, key: param.key, value: param.value)
                        dataModel.addToMetadata(metadata)
                    }

                    Map<String, DataType> dataTypes = [:]
                    GetTablesResponse getTablesResponse = glueClient.getTables(GetTablesRequest.builder().databaseName(database.name()).build())
                    getTablesResponse.tableList().each {table ->
                        DataClass dataClass = new DataClass(label: table.name())
                        table.parameters().each {param ->
                            dataClass.addToMetadata(new Metadata(namespace: namespace, key: param.key, value: param.value))
                        }

                        table.storageDescriptor().columns().each {column ->
                            DataType columnDataType = dataTypes[column.type()]
                            if (!columnDataType) {
                                columnDataType = new PrimitiveType(label: column.type())
                                dataTypes[column.type()] = columnDataType
                                dataModel.addToDataTypes(columnDataType)
                            }
                            DataElement dataElement = new DataElement(label: column.name(), dataType: columnDataType)

                            column.parameters().each {param ->
                                dataElement.addToMetadata(new Metadata(namespace: namespace, key: param.key, value: param.value))
                            }

                            dataClass.addToDataElements(dataElement)
                        }

                        dataModel.addToDataClasses(dataClass)
                    }

                    dataModelService.checkImportedDataModelAssociations(currentUser, dataModel)
                    imported += dataModel
                }
            }
                
            glueClient.close()
        }
        catch (GlueException ex) {
            throw new ApiInternalException('GLUEIP02', "${ex.message}")
        }

        imported.collect {updateImportedModelFromParameters(it, params, imported.size() > 1)}
        imported
    }

    @Override
    DataModel importModel(User currentUser, AwsGlueDataModelImporterProviderServiceParameters params) {
        log.debug("importDataModel")
        throw new ApiNotYetImplementedException('AGDMIPSXX', 'importModel')
    }

    @Override
    Boolean canImportMultipleDomains() {
        true
    }
}
