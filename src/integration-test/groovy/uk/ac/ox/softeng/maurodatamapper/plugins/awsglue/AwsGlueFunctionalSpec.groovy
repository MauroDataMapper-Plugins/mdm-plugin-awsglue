/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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

import uk.ac.ox.softeng.maurodatamapper.test.functional.BaseFunctionalSpec

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import grails.testing.spock.RunOnce
import grails.util.BuildSettings
import groovy.util.logging.Slf4j
import spock.lang.Shared

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static io.micronaut.http.HttpStatus.OK

@Slf4j
@Integration
class AwsGlueFunctionalSpec extends BaseFunctionalSpec {

    AwsGlueDataModelImporterProviderService awsGlueDataModelImporterProviderService

    @Shared
    Path resourcesPath

    def setupSpec() {
        resourcesPath = Paths.get(BuildSettings.BASE_DIR.absolutePath, 'src', 'integration-test', 'resources').toAbsolutePath()
    }

    @Override
    String getResourcePath() {
        ''
    }

    byte[] loadTestFile(String filename) {
        Path testFilePath = resourcesPath.resolve("${filename}").toAbsolutePath()
        assert Files.exists(testFilePath)
        Files.readAllBytes(testFilePath)
    }

    void 'test importer parameters'() {
        given:
        String version = awsGlueDataModelImporterProviderService.version
        String expected = new String(loadTestFile('expectedImporterParameters.json'))
        String url = "importer/parameters/uk.ac.ox.softeng.maurodatamapper.plugins.awsglue/AwsGlueDataModelImporterProviderService/$version"

        when:
        GET(url, STRING_ARG)

        then:
        verifyJsonResponse OK, expected
    }

}
