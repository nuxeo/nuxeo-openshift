package org.nuxeo.openshift.library

import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import static groovy.test.GroovyAssert.assertEquals

class NuxeoPackageHelperTest extends GroovyTestCase {

    def nuxeo_package_helper
    def package_file
    def VERSION = "1.4.0-SNAPSHOT"
    def NUXEO_PACKAGE_DIR = "ps-estelle-package/target/ps-estelle-package-*.zip"
    def NUXEO_PACKAGE_VERSION_DIR = "ps-estelle-package/target/ps-estelle-package-" + VERSION + ".zip"
    def NUXEO_PACKAGE_REGEX = "*/target/*package-*.zip"
    def NUXEO_PACKAGE_VERSION_REGEX = "*/target/*package-" + VERSION + ".zip"
    def NUXEO_S2I_PATH = ".nuxeo-s2i"

    @Before
    void setUp() {
        nuxeo_package_helper = new NuxeoPackageHelper()
        nuxeo_package_helper.metaClass.echo = { message -> print(message + "\n") }
        nuxeo_package_helper.metaClass.stash = {}
        nuxeo_package_helper.metaClass.fileExists = { filePathString -> false }
        package_file = new File(NUXEO_PACKAGE_VERSION_DIR)
    }

    @Test
    void test_should_stash_package_without_nuxeo_s2i_file() {
        nuxeo_package_helper.metaClass.findFiles = { map ->
            ("glob" in map && map["glob"].contains(NUXEO_PACKAGE_REGEX)) ? [package_file] : []
        }
        def result = nuxeo_package_helper.stash("*")
        assertEquals  NUXEO_PACKAGE_REGEX, result
    }

    @Test
    void test_should_stash_package_with_nuxeo_s2i_file() {
        nuxeo_package_helper.metaClass.fileExists = { filePathString ->
            filePathString == NUXEO_S2I_PATH
        }
        nuxeo_package_helper.metaClass.findFiles = { map ->
            ("glob" in map && map["glob"].contains(NUXEO_PACKAGE_DIR)) ? [package_file] : []
        }
        nuxeo_package_helper.metaClass.readProperties = { map ->
            def props = {}
            if ("file" in map && map["file"] == NUXEO_S2I_PATH) {
                props["NUXEO_PACKAGE_DIR"] = NUXEO_PACKAGE_DIR
            }
            return props
        }
        def result = nuxeo_package_helper.stash("*")
        assertEquals  NUXEO_PACKAGE_DIR, result
    }


    @Test
    void test_shouldnt_stash_package_when_no_package_found() {
        nuxeo_package_helper.metaClass.findFiles = { _ -> [] }
        def result = nuxeo_package_helper.stash("*")
        assertEquals  null, result
    }

    @Test
    void test_should_stash_package_with_specific_version() {
        nuxeo_package_helper.metaClass.findFiles = { map ->
            ("glob" in map && map["glob"].contains(NUXEO_PACKAGE_VERSION_REGEX)) ? [package_file] : []
        }
        def result = nuxeo_package_helper.stash(VERSION)
        assertEquals  NUXEO_PACKAGE_VERSION_REGEX, result
    }

    @Test
    void test_shouldnt_stash_package_when_other_version() {
        nuxeo_package_helper.metaClass.findFiles = { map ->
            ("glob" in map && map["glob"].contains(NUXEO_PACKAGE_VERSION_REGEX)) ? [package_file] : []
        }
        def result = nuxeo_package_helper.stash("2.0.0-SNAPSHOT")
        assertEquals null, result
    }
}
