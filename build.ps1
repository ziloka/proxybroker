# https://www.red-gate.com/simple-talk/sysadmin/powershell/how-to-use-parameters-in-powershell/
$package = $args[0]

if ($package.length -gt 0) {
	echo "usage: $args[0] <package name>"
	exit 1
}
$package_split = $package.split('/')
$package_name = $package_split[$package_split.length - 1]

platforms=["windows/amd64" "linux/amd64", "darwin/amd64"]