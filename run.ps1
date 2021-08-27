# Collect proxies
# gradlew run --args="find --types http HTTPS --coutries US --limit 10 --outfile ./proxies.txt"

# Validate proxies

# Example Usage: .\run.ps1 proxies.txt
# Proxies must be http proxies
 $content = Get-Content -Path $args[0];
 $proxyArr = $content.Split("\n");
 for($i=0;$i -lt $proxyArr.length;$i++){
    $proxy = $proxyArr[$i];
    # Default -MaximumRedirection is 5
    # https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/invoke-webrequest?view=powershell-7.1
    try {
        $response = Invoke-WebRequest -Proxy "http://$proxy" -Uri "http://httpbin.org/ip?json";
        $StatusCode = $response.StatusCode;
        Write-Output = "Proxy: $proxy StatusCode: $StatusCode";

    } catch {
        $StatusCode = $_.Exception.Response.StatusCode.value__;
        Write-Output = "Proxy: $proxy Status: $StatusCode";
    }

 }