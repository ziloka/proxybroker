use crate::services::collector::Proxy;
use tokio::sync::mpsc::Sender;
use serde::Deserialize;

#[derive(Deserialize)]
struct HttpBinResponse {
    origin: String,
}

pub struct CheckProxyResponse {
    pub alive: bool,
    pub host: String,
    pub port: u16,
}

/**
 * checks if it is possible to use proxy
 */
async fn send_proxy_request(sender: Sender<CheckProxyResponse>, proxy: Proxy) {
    match reqwest::Client::builder()
        .proxy(
            reqwest::Proxy::http(format!("http://{}:{}", proxy.host, proxy.port))
                .expect("Cannot build proxy"),
        )
        .build()
    {
        Ok(client) => {
            match client.get("http://httpbin.org/ip").send().await {
                Ok(response) => {
                    match response.json::<HttpBinResponse>().await {
                        Ok(body) => {
                            match sender.try_send(CheckProxyResponse {
                                alive: body.origin.eq(&proxy.host),
                                host: proxy.host,
                                port: proxy.port,
                            }) {
                                Ok(_) => {}
                                Err(err) => println!("failed to send proxy through channel: {}", err)
                            }
                        }
                        Err(_) => {} // Err(err) => println!("Could not get httpbin body: {}", err)
                    }
                }
                Err(_) => {} // Err(err) => println!("Cannot get a response: {}", err)
            }
        }
        Err(e) => println!("Cannot build client: {}", e),
    }
}

// async fn send_proxy_request(
//   sender: Sender<CheckProxyResponse>,
//   proxy: Proxy,
// ) -> Result<(), Box<dyn std::error::Error>> {
//   let client = reqwest::Client::builder()
//       .proxy(reqwest::Proxy::http(format!(
//           "http://{}:{}",
//           proxy.host, proxy.port
//       ))?)
//       .build()?;

//   let response = client.get("http://httpbin.org/ip").send().await?;
//   let body = response.json::<HttpBinResponse>().await?;
//   sender.try_send(CheckProxyResponse {
//       alive: body.origin.eq(&proxy.host),
//       host: proxy.host,
//       port: proxy.port,
//   });
//   Ok(())
// }

pub fn check(sender: Sender<CheckProxyResponse>, proxies: Vec<Proxy>) {
    for proxy in proxies {
        tokio::task::spawn(send_proxy_request(sender.clone(), proxy));
    }
}
