package org.nuxeo.openshift.library

def stash(String version) {
    def package_paths = []
    def package_files = []
    if (fileExists(".nuxeo-s2i")) {
      def props = readProperties file: ".nuxeo-s2i"
      package_paths.push(props["NUXEO_PACKAGE_DIR"].replaceAll("\\*", version))
    }
    package_paths.push("*/target/*package-${version}.zip")
    package_paths.push("*/target/*marketplace-${version}.zip")
    for (int i = 0; i < package_paths.size(); i++) {
      def package_path = (String) package_paths[i]
      package_files = findFiles(glob: "**/${package_path}")
      if (package_files.size() != 0) {
        stash name:"package", includes:"${package_files[0]}"
        return package_path
      }
    }
    if (package_files.size() == 0) {
      echo "WARNING: Marketplace package zip file not found."
    }
    return null
}

def unstash(String package_path) {
    if (package_path == null) {
      echo "WARNING: No marketplace package zip file was found."
      return
    }
    unstash name: "package"
    def package_files = findFiles(glob: "**/${package_path}")
    if (package_files.length == 0) {
      echo "WARNING: Marketplace package zip file not found."
      return
    }
    fileOperations([
      fileCopyOperation(
        includes: "${package_files[0].path}",
        targetLocation: "source",
        flattenFiles: true,
      ),
      fileRenameOperation(
        source: "source/${package_files[0].name}",
        destination: "source/marketplace.zip"
      )
    ])
}

return this
