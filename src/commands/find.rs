use futures::StreamExt;
use crate::services;

pub async fn find() {
  let mut vecs_of_unchecked_proxies = services::collector::collect();
  while let Some(unchecked_proxies) = vecs_of_unchecked_proxies.next().await {
    let mut list_of_checked_proxies = services::checker::check(unchecked_proxies);
    while let Some(proxy) = list_of_checked_proxies.next().await {
      match proxy {
        Ok(proxy) => {
          if proxy.alive {
            println!("{}:{}", proxy.host, proxy.port);
          }
        },
        Err(_err) => {}
      }
    }
  }
}