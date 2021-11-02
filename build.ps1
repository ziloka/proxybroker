# https://www.red-gate.com/simple-talk/sysadmin/powershell/how-to-use-parameters-in-powershell/
$package = $args[0]

if ($package.length -gt 0) {
  echo "usage: $args[0] <package name>"
  exit 1
}
$package_split = $package.split('/')
$package_name = $package_split[$package_split.length - 1]

platforms=["windows/amd64" "linux/amd64", "darwin/amd64"]

for ($platform in $platforms) {
  $platform_split = $platform.split('/')
  $GOOS=$platform_split[0]
  $GOARCH=$platform_split[1]
  ouput_name=$package_name"-"$GOOS"-"$GOARCH
  echo "Building $ouput_name"
  go build -o $ouput_name
  if ($? -ne 0) {
    echo "Error building $ouput_name"
    exit 1
  }
}