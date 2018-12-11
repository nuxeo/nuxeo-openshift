package org.nuxeo.openshift.library

def stash_nuxeo_package(version) {
  def nuxeoPackageFile = find(version)
  stashContent("package", nuxeoPackageFile)
}

def unstash_nuxeo_package(version, dirName) {
  unstashContent("package")
  def nuxeoPackageFile = find(version)
  moveAndRenameFile(nuxeoPackageFile, dirName, "marketplace.zip")
}

def find(version) {
  def package_paths = []
  def package_files = []
  if (fileExists(".nuxeo-s2i")) {
    def props = readProperties file:".nuxeo-s2i"
    package_paths.push(props["NUXEO_PACKAGE_DIR"].replaceAll("\\*", version))
  }
  package_paths.push("*/target/*package-${version}.zip")
  package_paths.push("*/target/*marketplace-${version}.zip")
  for (int i = 0; i < package_paths.size(); i++) {
    def package_path = (String) package_paths[i]
    package_files = findFiles(glob: "**/${package_path}")
    if (package_files.size() != 0) {
      return package_files[0]
    }
  }
  if (package_files.size() == 0) {
    echo "WARNING: Marketplace package zip file not found."
  }
  return null
}

def set_build_directory(dirName) {
  if (fileExists(dirName)) {
    sh "rm -rf ${dirName}"
  }
  sh "mkdir -p ${dirName}"
}

protected stashContent(stashName, includes) {
  stash name:stashName, includes:"${includes}"
}

protected unstashContent(stashName) {
  unstash name:stashName
}

protected moveAndRenameFile(file, targetDirName, targetFileName) {
  fileOperations([
      fileCopyOperation(
        includes: file.getPath(),
        targetLocation: targetDirName,
        flattenFiles: true,
      ),
      fileRenameOperation(
        source: targetDirName + "/" + file.getName(),
        destination: targetDirName + "/" + targetFileName
      )
  ])
}

return this
