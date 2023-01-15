use crate::services::collector::Proxy;
use serde::Deserialize;
use tokio::sync::mpsc::Sender;

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
            if let Ok(response) = client.get("http://httpbin.org/ip").send().await {
                if let Ok(body) = response.json::<HttpBinResponse>().await {
                    sender
                        .try_send(CheckProxyResponse {
                            alive: body.origin.eq(&proxy.host),
                            host: proxy.host,
                            port: proxy.port,
                        })
                        .ok();
                }
            }
        }
        Err(e) => println!("Cannot build client: {}", e),
    }
}

pub fn check(sender: Sender<CheckProxyResponse>, proxies: Vec<Proxy>) {
    for proxy in proxies {
        tokio::task::spawn(send_proxy_request(sender.clone(), proxy));
    }
}
