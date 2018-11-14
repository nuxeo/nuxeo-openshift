package org.nuxeo.openshift.library

import org.junit.Before
import org.junit.Test
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.util.HashMap
import java.util.Map
import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertNotNull
import static groovy.test.GroovyAssert.assertNull

class NuxeoPackageHelperTest extends GroovyTestCase {

    private NuxeoPackageHelper nuxeoPackageHelper
    private File packageFile
    private File marketplaceFile
    private File nuxeoS2iFile
    private File baseDir
    private File workspaceDir
    private Map<String, String> stash

    private matchesPackageFilePath(String glob) {
        FileSystem fileSystem = FileSystems.getDefault()
        PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + glob)
        Path packageFilePath = Paths.get(packageFile.getPath())
        return packageFile.exists() && pathMatcher.matches(packageFilePath)
    }

    private readPropertiesFromFile(String fileName) {
        def file = new File(workspaceDir.getPath() + "/" + fileName)
        Map props = new HashMap<String, String>()
        if (file.exists()) {
            def allLines = Files.readAllLines(Paths.get(file.getPath()))
            for (String line : allLines) {
                String[] parts = line.split("=")
                if (parts.size() == 2) {
                    props.put(parts[0], parts[1])
                }
            }
        }
        return props
    }

    private stash(String name, String includes) {
        stash.put("name", name)
        stash.put("includes", includes)
        print("Stash file '" + includes + "' as '" + name + "'\n")
    }

    private unstash(String name) {
        if (stash["name"].equals(name)) {
            stash.clear()
            print("Unstash '" + name + "'\n")
        }
    }

    private fileCopyOperation(String includes, String targetLocation) {
        def fileToCopy = new File(includes)
        def targetLocationDir = new File(baseDir.getPath() + "/" + targetLocation)
        if (fileToCopy.exists() && targetLocationDir.exists()) {
            def copiedFile = new File(targetLocationDir.getPath() + "/" + fileToCopy.getName())
            copiedFile.createNewFile()
        }
    }

    private fileRenameOperation(String source, String destination) {
        def sourceFile = new File(baseDir.getPath() + "/" + source)
        if (sourceFile.exists()) {
            def renamedFile = new File(baseDir.getPath() + "/" + destination)
            renamedFile.createNewFile()
            sourceFile.delete()
        }
    }

    @Before
    void setUp() {
        nuxeoPackageHelper = new NuxeoPackageHelper()
        stash = new HashMap<String, String>()

        // Mock Jenkins methods
        nuxeoPackageHelper.metaClass.echo = { message ->
            print(message + "\n")
        }
        nuxeoPackageHelper.metaClass.fileExists = { path ->
            nuxeoS2iFile.exists() && nuxeoS2iFile.getName().equals(path) ? true : false
        }
        nuxeoPackageHelper.metaClass.findFiles = { map ->
            "glob" in map && matchesPackageFilePath(map["glob"]) ? [packageFile] : []
        }
        nuxeoPackageHelper.metaClass.readProperties = { map ->
            "file" in map ? readPropertiesFromFile(map["file"]) : {}
        }
        nuxeoPackageHelper.metaClass.stash = { map ->
            "name" in map && "includes" in map ? stash(map["name"], map["includes"]) : false
        }
        nuxeoPackageHelper.metaClass.unstash = { map ->
            "name" in map ? unstash(map["name"]) : false
        }
        nuxeoPackageHelper.metaClass.fileCopyOperation = { map ->
            "includes" in map && "targetLocation" in map ? fileCopyOperation(map["includes"], map["targetLocation"]) : false
        }
        nuxeoPackageHelper.metaClass.fileRenameOperation = { map ->
            "source" in map && "destination" in map ? fileRenameOperation(map["source"], map["destination"]) : false
        }
        nuxeoPackageHelper.metaClass.fileOperations = {}

        // Set up directory structure
        baseDir = new File(System.getProperty("java.io.tmpdir"))
        def sourceDir = new File(baseDir, 'source')
        if (!sourceDir.exists()) {
            sourceDir.mkdir()
        }
        marketplaceFile = new File(sourceDir, 'marketplace.zip')
        if (marketplaceFile.exists()) {
            marketplaceFile.delete()
        }
        workspaceDir = new File(baseDir, 'ps-estelle')
        if (!workspaceDir.exists()) {
            workspaceDir.mkdir()
        }
        def packageDir = new File(workspaceDir, 'ps-estelle-package')
        if (!packageDir.exists()) {
            packageDir.mkdir()
        }
        def targetDir = new File(packageDir, 'target')
        if (!targetDir.exists()) {
            targetDir.mkdir()
        }
        packageFile = new File(targetDir, "ps-estelle-package-1.4.0-SNAPSHOT.zip")
        if (!packageFile.exists()) {
            packageFile.createNewFile()
        }
        nuxeoS2iFile = new File(workspaceDir, ".nuxeo-s2i")
        if (nuxeoS2iFile.exists()) {
            nuxeoS2iFile.delete()
        }
    }

    @Test
    void test_workspace_should_be_set() {
        assertEquals true, packageFile.exists()
        assertEquals "ps-estelle-package-1.4.0-SNAPSHOT.zip", packageFile.getName()
        assertEquals workspaceDir.getPath() + "/ps-estelle-package/target/ps-estelle-package-1.4.0-SNAPSHOT.zip", packageFile.getPath()
    }

    @Test
    void test_should_find_package_when_no_nuxeo_s2i_provided() {
        // Given no .nuxeo-s2i file is provided
        assertEquals false, nuxeoS2iFile.exists()

        // When looking for the nuxeo marketplace package file
        def result = nuxeoPackageHelper.find("*")

        // Then the package file is found
        assertNotNull result
        assertEquals packageFile.getPath(), result.path
    }

    @Test
    void test_should_find_package_when_nuxeo_s2i_provided() {
        // Given a .nuxeo-s2i file is provided
        nuxeoS2iFile.createNewFile()
        FileWriter writer = new FileWriter(nuxeoS2iFile)
        writer.write("NUXEO_PACKAGE_DIR=ps-estelle-package/target/ps-estelle-package-*.zip")
        writer.close()
        assertEquals true, nuxeoS2iFile.exists()

        // When looking for the nuxeo marketplace package file
        def result = nuxeoPackageHelper.find("*")

        // Then the package file is found
        assertNotNull result
        assertEquals packageFile.getPath(), result.path
    }

    @Test
    void test_should_find_package_when_looking_for_same_version() {
        // Given the same version is used
        def version = "1.4.0-SNAPSHOT"

        // When looking for the nuxeo marketplace package file
        def result = nuxeoPackageHelper.find(version)

        // Then the package file is found
        assertNotNull result
        assertEquals packageFile.getPath(), result.path
    }

    @Test
    void test_shouldnt_find_package_when_no_package() {
        // Given there is no nuxeo marketplace package file
        packageFile.delete()

        // When looking for it
        def result = nuxeoPackageHelper.find("*")

        // Then not finding the package file
        assertNull result
    }

    @Test
    void test_shouldnt_find_package_when_looking_for_different_version() {
        // Given a different version is used to
        def version = "3.2.0-SNAPSHOT"

        // When looking for the nuxeo marketplace package file
        def result = nuxeoPackageHelper.find(version)

        // Then not finding the package file
        assertNull result
    }

    @Test
    void test_should_stash_file() {
        // Given nothing is stashed
        stash.clear()
        assertEquals 0, stash.size()

        // When stashing the nuxeo marketplace package file
        nuxeoPackageHelper.stashContent("package", packageFile)

        // Then the package file is stashed
        assertEquals "package", stash.get("name")
        assertEquals packageFile.getPath(), stash.get("includes")
    }

    @Test
    void test_should_unstash_file() {
        // Given the nuxeo marketplace package file is stashed as "package"
        nuxeoPackageHelper.stashContent("package", packageFile)
        assertEquals "package", stash.get("name")
        assertEquals packageFile.getPath(), stash.get("includes")

        // When unstashing "package"
        nuxeoPackageHelper.unstashContent("package")

        // Then the package file is unstashed
        assertEquals 0, stash.size()
    }

    @Test
    void test_should_move_and_rename_file() {
        // Given the nuxeo marketplace package file is in its initial state
        assertEquals workspaceDir.getPath() + "/ps-estelle-package/target/ps-estelle-package-1.4.0-SNAPSHOT.zip", packageFile.getPath()
        assertFalse marketplaceFile.exists()

        // When moving and renaming it
        nuxeoPackageHelper.moveAndRenameFile(packageFile, "source", "marketplace.zip")

        // Then the package file is moved to source folder and renamed as "marketplace.zip"
        assertTrue marketplaceFile.exists()
    }

}
