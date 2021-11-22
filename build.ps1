# https://www.digitalocean.com/community/tutorials/how-to-build-go-executables-for-multiple-platforms-on-ubuntu-16-04

# Run to compile for the following platforms: MacOS, Windows, Linux (The most commonly used operating systems)
# .\build.ps1 github.com/Ziloka/ProxyBroker

# https://blog.kowalczyk.info/article/vEja/embedding-build-number-in-go-executable.html
$BUILD_TIME = Get-Date -UFormat "%Y-%m-%d_%T"
$SHA_HASH = (git rev-parse HEAD).Trim()
$BUILD_VERSION = $args[1]

# https://www.red-gate.com/simple-talk/sysadmin/powershell/how-to-use-parameters-in-powershell/
$package = $args[0]

if ($package.length -eq 0) {
  Write-Output "usage: $($MyInvocation.MyCommand.Name) <package name>"
  exit 1
}
$package_split = $package.split('/')
$package_name = $package_split[$package_split.length - 1]

$platforms = @(
  'windows/amd64',
  'linux/amd64',
  'darwin/amd64'
)

for ($i=0; $i -le $platforms.Length-1; $i++) {
  $platform = $platforms[$i]
  $platform_split = $platform.split('/')
  $GOOS=$platform_split[0]
  $GOARCH=$platform_split[1]
  $output_name="$package_name-$GOOS-$GOARCH"
  Write-Output "Building $output_name"
  if($GOOS -eq "windows") {
    $output_name+=".exe"
  }
  # https://stackoverflow.com/questions/1420719/powershell-setting-an-environment-variable-for-a-single-command-only
  # https://stackoverflow.com/a/1422082
  $env:GOOS = $GOOS
  $env:GOARCH = $GOARCH
  go build -ldflags="-s -w -X main.SHA_HASH=$SHA_HASH -X main.BUILD_TIME=$BUILD_TIME -X main.BUILD_VERSION=$BUILD_VERSION" -o $output_name $package
  Remove-Item Env:\GOOS
  Remove-Item Env:\GOARCH
  # if ($? -ne 0) {
  #   Write-Output "An error has occurred! Aborting the script execution..."
  #   exit 1
  # }
}