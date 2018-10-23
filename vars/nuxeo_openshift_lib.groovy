def stash_package(version) {
  def package_paths = []
  def package_files = []
  if (fileExists(".nuxeo-s2i")) {
    def props = readProperties file: ".nuxeo-s2i"
    package_paths.push(props["NUXEO_PACKAGE_DIR"].replaceAll("\\*", version))
  }
  package_paths.push("*/target/*package-${version}.zip")
  package_paths.push("*/target/*marketplace-${version}.zip")
  package_paths.each { package_path ->
    package_files = findFiles(glob: "**/${package_path}")
    if (package_files.length != 0) {
      env.PACKAGE_PATH_REGEX = package_path
      stash name:"package", includes:"${package_files[0]}"
      return
    }
  }
  if (package_files.length == 0) {
    echo "WARNING: Marketplace package zip file not found."
  }
}

def unstash_package() {
  unstash name: "package"
  def package_files = findFiles(glob: "**/${env.PACKAGE_PATH_REGEX}")
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
