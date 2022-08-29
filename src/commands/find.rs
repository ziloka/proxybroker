use std::fs::File;
use std::io::prelude::*;
use tokio::sync::mpsc::channel;
use clap::ArgMatches;
use tokio::sync::mpsc::error::TryRecvError;
use crate::services;

pub async fn find(sub_matches: &ArgMatches) {
  let mut file: Option<Result<File, std::io::Error>> = None;
  if sub_matches.is_present("file") {
    file = Some(File::create(sub_matches.get_one::<String>("file")
    .expect("Specify filename to write proxies to")));
  }
  let limit = sub_matches.get_one::<u64>("limit").unwrap();

  // https://medium.com/@polyglot_factotum/rust-concurrency-patterns-communicate-by-sharing-your-sender-re-visited-9d42e6dfecfa
  // https://dhghomon.github.io/easy_rust/Chapter_50.html
  // https://stackoverflow.com/questions/60577867/non-blocking-recv-on-an-async-channel
  // https://stackoverflow.com/questions/59447577/how-to-peek-on-channels-without-blocking-and-still-being-able-to-detect-hangup
  // https://doc.rust-lang.org/book/ch16-02-message-passing.html
  // https://www.reddit.com/r/learnrust/comments/iavolt/okay_to_drop_original_mpscsender_when_creating/

  let (unchecked_proxies_tx, mut unchecked_proxies_rx) = channel::<Vec<crate::services::collector::Proxy>>(100);
  let (checked_proxies_tx, mut checked_proxies_rx) = channel::<crate::services::checker::CheckProxyResponse>(100);
  services::collector::collect(unchecked_proxies_tx.clone());// if not cloned throws Disconnected Error, otherwise throws Empty
  let mut counter: u64 = 0;
 
  loop {
    match unchecked_proxies_rx.try_recv() {
      Ok(proxies) => {
        println!("{} proxies", proxies.len());
        services::checker::check(checked_proxies_tx.clone(), proxies);
      },
      Err(TryRecvError::Disconnected) => println!("Handle sender disconnected"),
      Err(TryRecvError::Empty) => {}
      // Err(TryRecvError::Empty) => println!("No data yet")
    }
    match checked_proxies_rx.try_recv() {
      Ok(proxy) => {
        counter+=1;
        println!("{}:{}", proxy.host, proxy.port);
        if counter >= *limit {
          std::process::exit(0);
        }
      },
      Err(TryRecvError::Disconnected) => println!("Handle sender disconnected"),
      Err(TryRecvError::Empty) => {}
      // Err(TryRecvError::Empty) => println!("No data yet")
    }
  }
}